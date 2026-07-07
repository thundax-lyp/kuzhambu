package com.thundax.kuzhambu.operations.application.health.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.operations.application.health.configure.OperationsExternalHealthProbeConfiguration;
import com.thundax.kuzhambu.operations.application.health.configure.OperationsExternalHealthProbeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

class OperationsExternalHealthProbeConfigurationTest {

    @Test
    void configurationShouldEnableExternalHealthProbeProperties() {
        assertTrue(OperationsExternalHealthProbeConfiguration.class.isAnnotationPresent(Configuration.class));
        EnableConfigurationProperties annotation =
                OperationsExternalHealthProbeConfiguration.class.getAnnotation(EnableConfigurationProperties.class);

        assertTrue(java.util.Arrays.asList(annotation.value()).contains(OperationsExternalHealthProbeProperties.class));
    }
}
