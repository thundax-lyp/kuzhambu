package com.thundax.kuzhambu.system.application.audit.annotation;

import com.thundax.kuzhambu.system.domain.model.enums.AuditAction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    String type();

    String id();

    AuditAction action();

    String summary() default "";

    String condition() default "";

    boolean recordWhenUnchanged() default false;
}
