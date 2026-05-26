package com.thundax.kuzhambu.interfaces.admin.core.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.interfaces.admin.core.aop.annotation.SysLogger;
import com.thundax.kuzhambu.interfaces.admin.core.service.SysLogMessageService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@SuppressWarnings({"unchecked"})
public class SysLogPointcutAdvisor extends StaticMethodMatcherPointcutAdvisor {

    private static final Class<? extends Annotation>[] ANNOTATION_CLASSES = new Class[] {SysLogger.class};

    public SysLogPointcutAdvisor(SysLogMessageService sysLogMessageService, ObjectMapper objectMapper) {
        setAdvice(new SysLogMethodInterceptor(sysLogMessageService, objectMapper));
    }

    @Override
    public boolean matches(@NonNull Method method, @Nullable Class targetClass) {
        Method m = method;

        if (isAnnotationPresent(m)) {
            return true;
        }

        if (targetClass != null) {
            try {
                m = targetClass.getMethod(m.getName(), m.getParameterTypes());
                return isAnnotationPresent(m);

            } catch (NoSuchMethodException ignored) {
            }
        }

        return false;
    }

    private boolean isAnnotationPresent(Class<?> targetClazz) {
        for (Class<? extends Annotation> annClass : ANNOTATION_CLASSES) {
            Annotation a = AnnotationUtils.findAnnotation(targetClazz, annClass);
            if (a != null) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnnotationPresent(Method method) {
        for (Class<? extends Annotation> annClass : ANNOTATION_CLASSES) {
            Annotation a = AnnotationUtils.findAnnotation(method, annClass);
            if (a != null) {
                return true;
            }
        }
        return false;
    }
}
