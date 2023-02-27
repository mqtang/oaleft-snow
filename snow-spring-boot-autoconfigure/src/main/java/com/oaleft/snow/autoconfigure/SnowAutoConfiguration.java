package com.oaleft.snow.autoconfigure;

import com.oaleft.snow.SnowLogger;
import com.oaleft.snow.SnowSQLInterceptor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;
import java.util.Objects;

/**
 * @author tangcheng
 */
@AutoConfiguration
@ConditionalOnClass(value = {SnowLogger.class, SqlSession.class, MybatisAutoConfiguration.class})
@ConditionalOnBean(value = {SqlSessionFactory.class})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
@EnableConfigurationProperties(SnowProperties.class)
public class SnowAutoConfiguration implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SnowAutoConfiguration.class);

    private final SnowProperties snowProperties;
    private final List<SqlSessionFactory> sessionFactories;

    private final SnowSQLInterceptor snowSQLInterceptor = new SnowSQLInterceptor();

    public SnowAutoConfiguration(SnowProperties snowProperties, List<SqlSessionFactory> sessionFactories) {
        this.snowProperties = snowProperties;
        this.sessionFactories = sessionFactories;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (Objects.isNull(sessionFactories) || sessionFactories.isEmpty()) {
            return;
        }
        for (SqlSessionFactory factory : sessionFactories) {
            org.apache.ibatis.session.Configuration configuration = factory.getConfiguration();
            if (!configuration.getInterceptors().contains(snowSQLInterceptor)) {
                configuration.addInterceptor(snowSQLInterceptor);
            }
        }
    }
}
// 2023/2/27 8:56, oaleft-snow