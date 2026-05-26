package com.thundax.kuzhambu.common.security.configure;

import com.thundax.kuzhambu.common.security.authorization.HasPermissionBeanPostProcessor;
import com.thundax.kuzhambu.common.security.permission.PermissionAuthorizationService;
import com.thundax.kuzhambu.common.security.permission.PermissionMatcher;
import com.thundax.kuzhambu.common.security.permission.PrefixPermissionMatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(KuzhambuMethodSecurityConfiguration.class)
public class KuzhambuSecurityConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PermissionMatcher permissionMatcher() {
        return new PrefixPermissionMatcher();
    }

    @Bean("permissionAuthorizationService")
    @ConditionalOnMissingBean(value = PermissionAuthorizationService.class, name = "permissionAuthorizationService")
    public PermissionAuthorizationService permissionAuthorizationService(PermissionMatcher permissionMatcher) {
        return new PermissionAuthorizationService(permissionMatcher);
    }

    @Bean
    @ConditionalOnBean(PermissionAuthorizationService.class)
    @ConditionalOnMissingBean
    public static HasPermissionBeanPostProcessor hasPermissionBeanPostProcessor(
            PermissionAuthorizationService permissionAuthorizationService) {
        return new HasPermissionBeanPostProcessor(permissionAuthorizationService);
    }
}
