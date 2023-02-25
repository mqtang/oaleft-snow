package com.oaleft.snow.format;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;

/**
 * @author tangcheng
 */
public class VerticalSQLFormatter implements SQLFormatter {
    @Override
    public String format(String sql) {
        return SqlFormatter.format(sql, FormatConfig.builder().build());
    }
}
// 2023/2/25 0:45, oaleft-snow