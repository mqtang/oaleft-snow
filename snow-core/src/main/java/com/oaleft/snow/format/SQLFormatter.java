package com.oaleft.snow.format;

/**
 * @author tangcheng
 */
public interface SQLFormatter {
    /**
     * 格式化sql
     *
     * @param sql SQL
     * @return 格式化后的SQL
     */
    String format(String sql);
}
// 2023/2/25 0:39, oaleft-snow