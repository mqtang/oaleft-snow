package com.oaleft.snow.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author tangcheng
 */
@ConfigurationProperties(prefix = "oaleft.snow")
public class SnowProperties {
    /**
     * Snow日志模式
     */
    private Mode mode = Mode.OFF;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    enum Mode {
        /**
         * 关闭Snow日志.
         */
        OFF,
        /**
         * 打印SQL到System.out.
         */
        CONSOLE,
        /**
         * 打印SQL到日志文件
         */
        LOG
    }
}
// 2023/2/27 8:57, oaleft-snow