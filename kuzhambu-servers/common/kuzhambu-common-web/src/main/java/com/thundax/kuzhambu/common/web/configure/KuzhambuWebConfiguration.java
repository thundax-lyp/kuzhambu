package com.thundax.kuzhambu.common.web.configure;

import com.thundax.kuzhambu.common.web.advice.ApiResponseBodyAdvice;
import com.thundax.kuzhambu.common.web.context.DefaultKuzhambuContextResolver;
import com.thundax.kuzhambu.common.web.context.KuzhambuContextFilter;
import com.thundax.kuzhambu.common.web.context.KuzhambuContextResolver;
import com.thundax.kuzhambu.common.web.exception.DefaultExceptionTranslator;
import com.thundax.kuzhambu.common.web.exception.ExceptionTranslator;
import com.thundax.kuzhambu.common.web.exception.GlobalExceptionHandler;
import com.thundax.kuzhambu.common.web.i18n.I18nMessageResolver;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class KuzhambuWebConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public I18nMessageResolver i18nMessageResolver(MessageSource messageSource) {
        return new I18nMessageResolver(messageSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(
            I18nMessageResolver i18nMessageResolver, List<ExceptionTranslator> exceptionTranslators) {
        return new GlobalExceptionHandler(i18nMessageResolver, exceptionTranslators);
    }

    @Bean
    @ConditionalOnMissingBean(DefaultExceptionTranslator.class)
    public DefaultExceptionTranslator defaultExceptionTranslator() {
        return new DefaultExceptionTranslator();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiResponseBodyAdvice apiResponseBodyAdvice() {
        return new ApiResponseBodyAdvice();
    }

    @Bean
    @ConditionalOnMissingBean
    public KuzhambuContextResolver kuzhambuContextResolver() {
        return new DefaultKuzhambuContextResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public KuzhambuContextFilter kuzhambuContextFilter(KuzhambuContextResolver kuzhambuContextResolver) {
        return new KuzhambuContextFilter(kuzhambuContextResolver);
    }

    @Bean
    @ConditionalOnMissingBean(name = "kuzhambuContextFilterRegistration")
    public FilterRegistrationBean<KuzhambuContextFilter> kuzhambuContextFilterRegistration(
            KuzhambuContextFilter kuzhambuContextFilter) {
        FilterRegistrationBean<KuzhambuContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(kuzhambuContextFilter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
