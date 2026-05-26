package com.thundax.kuzhambu.common.security.authorization;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.permission.PermissionAuthorizationService;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.ReflectionUtils;

public class HasPermissionBeanPostProcessor implements BeanPostProcessor, Ordered {

    private final PermissionAuthorizationService permissionAuthorizationService;

    public HasPermissionBeanPostProcessor(PermissionAuthorizationService permissionAuthorizationService) {
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = AopUtils.getTargetClass(bean);
        if (beanClass == null || !hasPermissionAnnotation(beanClass)) {
            return bean;
        }

        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(new HasPermissionMethodInterceptor(permissionAuthorizationService));
        return proxyFactory.getProxy(beanClass.getClassLoader());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private boolean hasPermissionAnnotation(Class<?> beanClass) {
        if (AnnotationUtils.findAnnotation(beanClass, HasPermission.class) != null) {
            return true;
        }
        final boolean[] found = new boolean[] {false};
        ReflectionUtils.doWithMethods(beanClass, method -> {
            if (AnnotationUtils.findAnnotation(method, HasPermission.class) != null) {
                found[0] = true;
            }
        });
        return found[0];
    }

    private static class HasPermissionMethodInterceptor implements MethodInterceptor {

        private final PermissionAuthorizationService permissionAuthorizationService;

        HasPermissionMethodInterceptor(PermissionAuthorizationService permissionAuthorizationService) {
            this.permissionAuthorizationService = permissionAuthorizationService;
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            HasPermission annotation = findHasPermissionAnnotation(invocation);
            if (annotation == null || permissionAuthorizationService.isPermittedAny(annotation.value())) {
                return invocation.proceed();
            }
            throw new AccessDeniedException("Access denied for permission " + String.join(",", annotation.value()));
        }

        private HasPermission findHasPermissionAnnotation(MethodInvocation invocation) {
            Class<?> targetClass = invocation.getThis() != null
                    ? AopUtils.getTargetClass(invocation.getThis())
                    : invocation.getMethod().getDeclaringClass();
            Method specificMethod = AopUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
            HasPermission annotation = AnnotationUtils.findAnnotation(specificMethod, HasPermission.class);
            return annotation != null ? annotation : AnnotationUtils.findAnnotation(targetClass, HasPermission.class);
        }
    }
}
