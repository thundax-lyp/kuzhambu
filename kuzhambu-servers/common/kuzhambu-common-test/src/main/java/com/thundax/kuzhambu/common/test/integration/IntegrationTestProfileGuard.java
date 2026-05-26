package com.thundax.kuzhambu.common.test.integration;

import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

public final class IntegrationTestProfileGuard {

    public static final String ENABLED_PROPERTY = "kuzhambu.integration-test.enabled";

    private IntegrationTestProfileGuard() {}

    public static void assertEnabled(Environment environment) {
        Assert.notNull(environment, "environment must not be null");
        if (!Boolean.parseBoolean(environment.getProperty(ENABLED_PROPERTY, "false"))) {
            throw new IllegalStateException(
                    "Integration test profile is not enabled. Set " + ENABLED_PROPERTY + "=true before running ITs.");
        }
    }
}
