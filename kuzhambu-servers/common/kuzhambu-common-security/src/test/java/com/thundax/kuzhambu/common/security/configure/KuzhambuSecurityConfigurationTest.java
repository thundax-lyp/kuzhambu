package com.thundax.kuzhambu.common.security.configure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.authorization.HasPermissionBeanPostProcessor;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubject;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.common.security.permission.PermissionAuthorizationService;
import com.thundax.kuzhambu.common.security.permission.PermissionMatcher;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

public class KuzhambuSecurityConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KuzhambuSecurityConfiguration.class));

    @AfterEach
    public void tearDown() {
        KuzhambuContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldRegisterCommonSecurityBeans() {
        contextRunner.run(context -> {
            context.getBean(PermissionMatcher.class);
            context.getBean(PermissionAuthorizationService.class);
            context.getBean(HasPermissionBeanPostProcessor.class);
        });
    }

    @Test
    public void shouldAllowHasPermissionMethodWhenCurrentUserHasPermission() {
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "admin", KuzhambuSubjectType.ADMIN_USER, "Admin", "token-1", Arrays.asList("sys:user:view")));

        contextRunner
                .withUserConfiguration(HasPermissionServiceConfiguration.class)
                .run(context -> {
                    assertEquals(
                            "ok",
                            context.getBean(SampleHasPermissionService.class).view());
                });
    }

    @Test
    public void shouldDenyHasPermissionMethodWhenCurrentUserMissesPermission() {
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "admin", KuzhambuSubjectType.ADMIN_USER, "Admin", "token-1", Arrays.asList("sys:role:view")));

        contextRunner
                .withUserConfiguration(HasPermissionServiceConfiguration.class)
                .run(context -> assertThrows(
                        AccessDeniedException.class,
                        () -> context.getBean(SampleHasPermissionService.class).view()));
    }

    @Configuration
    static class HasPermissionServiceConfiguration {

        @Bean
        public SampleHasPermissionService sampleHasPermissionService() {
            return new SampleHasPermissionService();
        }
    }

    static class SampleHasPermissionService {

        @HasPermission("sys:user:view")
        public String view() {
            return "ok";
        }
    }
}
