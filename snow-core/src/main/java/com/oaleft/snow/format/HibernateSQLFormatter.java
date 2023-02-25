package com.oaleft.snow.format;

import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.hibernate.engine.jdbc.internal.Formatter;

/**
 * @author tangcheng
 */
public class HibernateSQLFormatter implements SQLFormatter {
    private final Formatter formatter = new BasicFormatterImpl();

    @Override
    public String format(String sql) {
        return formatter.format(sql);
    }
}
// 2023/2/25 0:43, oaleft-snow