package com.thundax.kuzhambu.operations.application.health.configure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    OperationsExternalHealthProbeProperties.class,
    OperationsHealthAlertPolicyProperties.class
})
public class OperationsExternalHealthProbeConfiguration {}
