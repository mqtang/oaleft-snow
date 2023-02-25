package com.oaleft.snow.format;

/**
 * @author tangcheng
 */
public class CompactSQLFormatter implements SQLFormatter {
    private final SQLFormatter sqlFormatter;

    public CompactSQLFormatter(SQLFormatter sqlFormatter) {
        this.sqlFormatter = sqlFormatter;
    }

    @Override
    public String format(String sql) {
        return sqlFormatter.format(sql).replaceAll("\n", " ");
    }
}
// 2023/2/25 0:46, oaleft-snow