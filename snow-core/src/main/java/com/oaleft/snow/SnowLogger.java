package com.oaleft.snow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author tangcheng
 */
@Documented
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SnowLogger {
    /**
     * @return 是否忽略
     */
    boolean ignore() default false;
}
// 2023/2/24 22:19, oaleft-snow