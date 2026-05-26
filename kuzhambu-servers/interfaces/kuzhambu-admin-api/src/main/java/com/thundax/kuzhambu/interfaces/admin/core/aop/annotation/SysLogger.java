package com.thundax.kuzhambu.interfaces.admin.core.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SysLogger {

    /**
     * 标题
     *
     * @return 标题
     */
    String value() default "";

    /**
     * 归属模块
     *
     * @return 归属模块
     */
    String[] module() default {};

    /**
     * 分类
     *
     * @return 分类
     */
    String category() default "";
}
