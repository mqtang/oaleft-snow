package com.oaleft.snow;

import com.mysql.cj.PreparedQuery;
import com.mysql.cj.Query;
import com.mysql.cj.jdbc.ClientPreparedStatement;
import com.oaleft.snow.format.HibernateSQLFormatter;
import com.oaleft.snow.format.SQLFormatter;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.jdbc.PreparedStatementLogger;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tangcheng
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "parameterize", args = {Statement.class}),
})
public final class SnowSQLInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(SnowSQLInterceptor.class);

    private static final String LOGGER_FORMAT = "「SNOW_SQL_LOGGER」 =>" + System.lineSeparator() + "%s";

    // org.apache.ibatis.executor.statement.RoutingStatementHandler.delegate
    private static final Field statementHandlerField;
    // org.apache.ibatis.executor.statement.BaseStatementHandler.configuration
    private static final Field configurationField;
    // org.apache.ibatis.executor.statement.BaseStatementHandler.mappedStatement
    private static final Field mappedStatementField;

    private final Map<String, Class<?>> msIdMapperMap = new ConcurrentHashMap<>();
    private final Map<String, Method> msIdMethodMap = new ConcurrentHashMap<>();

    private final SQLFormatter sqlFormatter = new HibernateSQLFormatter();

    private final SnowConfiguration configuration;

    public SnowSQLInterceptor(SnowConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object rtn = invocation.proceed();
        try {
            if (!configuration.isDisabled())
                prepareAndLogSql(invocation);
        } catch (Exception ex) {
            logger.error("SnowSQLInterceptor ::error", ex);
        }
        return rtn;
    }

    /**
     * 反射获取相关数据并记录SQL
     *
     * @param invocation Invocation
     * @throws Exception 异常
     */
    private void prepareAndLogSql(Invocation invocation) throws Exception {
        BaseStatementHandler bsh = queryBaseStatementHandler(invocation.getTarget());
        Configuration configuration = (Configuration) configurationField.get(bsh);
        MappedStatement mappedStatement = (MappedStatement) mappedStatementField.get(bsh);
        if (configuration == null || mappedStatement == null) {
            return;
        }
        logSql(mappedStatement, configuration, invocation);
    }

    /**
     * 打印SQL日志
     *
     * @param ms            org.apache.ibatis.mapping.MappedStatement
     * @param configuration org.apache.ibatis.session.Configuration
     * @param invocation    org.apache.ibatis.plugin.Invocation
     * @throws Exception 异常
     */
    private void logSql(MappedStatement ms, Configuration configuration, Invocation invocation)
            throws Exception {
        final String msId = ms.getId();
        final Class<?> mapperClass = queryMapper(msId, configuration);
        final String methodName = msId.substring(mapperClass.getName().length() + 1);
        final Method method = queryMapperMethod(msId, methodName, mapperClass);

        final Log log = ms.getStatementLog();
        if (method == null) {
            debug(log, msId, "Mapper Method not found, MappedStatement.id = ~"
                    , msId);
            return;
        }
        if (!shouldLogSql(mapperClass, method)) {
            debug(log, msId, "ignore mapper = ~, method = ~"
                    , mapperClass.getName(), method.getName());
            return;
        }
        final Object arg0 = invocation.getArgs()[0];
        if (!(arg0 instanceof PreparedStatement)) {
            debug(log, msId, "statement is not a instance of ~"
                    , mapperClass.getName());
            return;
        }

        PreparedStatement ps = (PreparedStatement) invocation.getArgs()[0];
        if (!Proxy.isProxyClass(ps.getClass())) {
            debug(log, msId, "statement is not a proxy, it is ~"
                    , mapperClass.getName());
            ClientPreparedStatement cps = ps.unwrap(ClientPreparedStatement.class);
            Query query = cps.getQuery();
            if (query instanceof PreparedQuery preparedQuery) {
                doLogSql(preparedQuery.asSql(), log, msId);
            }
            return;
        }

        final InvocationHandler handler = Proxy.getInvocationHandler(ps);
        if (handler instanceof PreparedStatementLogger psl) {
            PreparedStatement preparedStatement = psl.getPreparedStatement();
            ClientPreparedStatement cps = preparedStatement.unwrap(ClientPreparedStatement.class);
            Query query = cps.getQuery();
            if (query instanceof PreparedQuery preparedQuery) {
                doLogSql(preparedQuery.asSql(), log, msId);
            }
        }
    }

    /**
     * 判断是否需要打印SQL
     *
     * @param mapperClass MyBatis Mapper Interface
     * @param method      Mapper Method
     * @return 是否打印SQL
     */
    private boolean shouldLogSql(Class<?> mapperClass, Method method) {
        SnowLogger clzAnn = mapperClass.getAnnotation(SnowLogger.class);
        SnowLogger mAnn = method.getAnnotation(SnowLogger.class);
        // mapper class 没有
        if (clzAnn == null) {
            if (mAnn == null) {
                return false;
            }
            return !mAnn.ignore();
        }
        // mapper class 上有, method 上没有
        if (mAnn == null) {
            return !clzAnn.ignore();
        }
        // class 和 method 上都有
        return !(clzAnn.ignore() || mAnn.ignore());
    }

    /**
     * 获取 {@code BaseStatementHandler}
     *
     * @param target org.apache.ibatis.executor.statement.StatementHandler
     * @return org.apache.ibatis.executor.statement.BaseStatementHandler
     */
    private BaseStatementHandler queryBaseStatementHandler(final Object target)
            throws Exception {
        if (target instanceof RoutingStatementHandler) {
            return (BaseStatementHandler) statementHandlerField.get(target);
        } else {
            return (BaseStatementHandler) target;
        }
    }

    /**
     * 获取 mapper interface
     *
     * @param msId          org.apache.ibatis.mapping.MappedStatement#getId()
     * @param configuration org.apache.ibatis.session.Configuration
     * @return mapper interface
     */
    private Class<?> queryMapper(String msId, Configuration configuration) {
        if (msIdMapperMap.containsKey(msId)) {
            return msIdMapperMap.get(msId);
        }
        String idClzName = msId.substring(0, msId.lastIndexOf("."));
        MapperRegistry mapperRegistry = configuration.getMapperRegistry();
        Collection<Class<?>> mapperClzList = mapperRegistry.getMappers();
        Class<?> mapperClz = null;
        for (Class<?> clz : mapperClzList) {
            String clzName = clz.getName();
            if (clzName.equals(idClzName)) {
                mapperClz = clz;
                break;
            }
        }
        msIdMapperMap.putIfAbsent(msId, mapperClz);
        return mapperClz;
    }

    /**
     * 获取 mapper method
     *
     * @param msId        org.apache.ibatis.mapping.MappedStatement#getId()
     * @param methodName  mapper 接口里的方法名
     * @param mapperClass mapper interface
     * @return mapper 接口里的方法
     */
    private Method queryMapperMethod(String msId, String methodName, Class<?> mapperClass) {
        Method method = null;
        if (msIdMethodMap.containsKey(msId)) {
            return msIdMethodMap.get(msId);
        }
        Method[] methods = mapperClass.getDeclaredMethods();
        for (Method m : methods) {
            if (methodName.equals(m.getName())) {
                method = m;
                break;
            }
        }
        if (method == null) {
            return null;
        }
        msIdMethodMap.putIfAbsent(msId, method);
        return method;
    }

    /**
     * 记录SQL
     *
     * @param sql  sql
     * @param log  mybatis logger
     * @param msId mappedStatement id
     */
    private void doLogSql(String sql, Log log, String msId) {
        if (useSnowLogger()) {
            if (logger.isDebugEnabled())
                logger.debug(loggerFormat(msId, "[SQL] {}"), sqlFormatter.format(sql));
        } else {
            if (log.isDebugEnabled())
                log.debug(String.format(loggerFormat(msId, "[SQL] %s"), sqlFormatter.format(sql)));
        }
    }

    /**
     * debug method
     *
     * @param log    mybatis log
     * @param msId   mappedStatement Id
     * @param format log format
     * @param args   args
     */
    private void debug(Log log, String msId, String format, Object... args) {
        String slfj = "{}";
        String mlog = "%s";
        String lf = format.replaceAll("~", useSnowLogger() ? slfj : mlog);
        if (useSnowLogger()) {
            if (logger.isDebugEnabled())
                logger.debug(loggerFormat(msId, lf), args);
        } else {
            if (log.isDebugEnabled())
                log.debug(String.format(loggerFormat(msId, lf), args));
        }
    }

    private boolean useSnowLogger() {
        return false;
    }

    private static String loggerFormat(String msId, String msg) {
        if (msId == null || msId.isEmpty()) {
            return String.format(LOGGER_FORMAT, msg);
        }
        return String.format(LOGGER_FORMAT, msId + System.lineSeparator() + "-> " + msg);
    }

    static {
        Field[] bshFields = BaseStatementHandler.class.getDeclaredFields();
        Field cf = null;
        Field msf = null;
        for (Field f : bshFields) {
            if ("configuration".equals(f.getName())) {
                f.setAccessible(true);
                cf = f;
                continue;
            }
            if ("mappedStatement".equals(f.getName())) {
                f.setAccessible(true);
                msf = f;
            }
            if (cf != null && msf != null) {
                break;
            }
        }
        configurationField = cf;
        mappedStatementField = msf;
        Field[] rshFields = RoutingStatementHandler.class.getDeclaredFields();
        Field shf = null;
        for (Field f : rshFields) {
            if (f.getType() == StatementHandler.class) {
                f.setAccessible(true);
                shf = f;
                break;
            }
        }
        statementHandlerField = shf;
    }
}
// 2023/2/24 22:01, oaleft-snow