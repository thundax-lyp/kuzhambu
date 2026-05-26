package com.thundax.kuzhambu.common.oss.configure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class KuzhambuOssConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(KuzhambuOssConfiguration.class));

    @Test
    public void shouldCreateLocalObjectStorageClientByDefault() {
        contextRunner.run(context -> assertTrue(context.containsBean("localFileObjectStorageClient")));
    }

    @Test
    public void shouldRejectS3WhenRequiredConfigurationMissing() {
        contextRunner
                .withPropertyValues("kuzhambu.oss.type=s3")
                .run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
    }

    @Test
    public void shouldRejectUnsupportedOssType() {
        contextRunner
                .withPropertyValues("kuzhambu.oss.type=ftp")
                .run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable cause = throwable;
        while (cause != null) {
            if (causeType.isInstance(cause)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
