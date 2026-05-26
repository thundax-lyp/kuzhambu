package com.thundax.kuzhambu.common.security.configure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@ConditionalOnMissingBean(MethodSecurityInterceptor.class)
@EnableMethodSecurity
public class KuzhambuMethodSecurityConfiguration {}
