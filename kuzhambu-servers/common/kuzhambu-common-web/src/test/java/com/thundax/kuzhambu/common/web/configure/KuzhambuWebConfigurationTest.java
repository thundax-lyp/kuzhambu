package com.thundax.kuzhambu.common.web.configure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.common.web.advice.ApiResponseBodyAdvice;
import com.thundax.kuzhambu.common.web.context.DefaultKuzhambuContextResolver;
import com.thundax.kuzhambu.common.web.context.KuzhambuContextFilter;
import com.thundax.kuzhambu.common.web.context.KuzhambuContextResolver;
import com.thundax.kuzhambu.common.web.exception.DefaultExceptionTranslator;
import com.thundax.kuzhambu.common.web.exception.GlobalExceptionHandler;
import com.thundax.kuzhambu.common.web.i18n.I18nMessageResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

public class KuzhambuWebConfigurationTest {

    private final WebApplicationContextRunner contextRunner =
            new WebApplicationContextRunner().withConfiguration(AutoConfigurations.of(KuzhambuWebConfiguration.class));

    @Test
    public void shouldRegisterCommonWebBeans() {
        contextRunner.run(context -> {
            context.getBean(I18nMessageResolver.class);
            context.getBean(DefaultExceptionTranslator.class);
            context.getBean(GlobalExceptionHandler.class);
            context.getBean(ApiResponseBodyAdvice.class);
            context.getBean(KuzhambuContextResolver.class);
            context.getBean(KuzhambuContextFilter.class);
        });
    }

    @Test
    public void shouldRegisterContextFilterWithHighestPrecedence() {
        contextRunner.run(context -> {
            FilterRegistrationBean<?> registration = context.getBean(FilterRegistrationBean.class);

            assertEquals(Ordered.HIGHEST_PRECEDENCE, registration.getOrder());
        });
    }

    @Test
    public void shouldBackOffWhenCustomContextResolverExists() {
        contextRunner
                .withUserConfiguration(CustomContextResolverConfiguration.class)
                .run(context -> {
                    assertEquals(
                            1,
                            context.getBeansOfType(KuzhambuContextResolver.class)
                                    .size());
                });
    }

    @Configuration
    static class CustomContextResolverConfiguration {

        @Bean
        public KuzhambuContextResolver customKuzhambuContextResolver() {
            return new DefaultKuzhambuContextResolver();
        }
    }
}
