package com.thundax.kuzhambu.classics.application.publication;

import static org.assertj.core.api.Assertions.assertThat;

import com.thundax.kuzhambu.classics.application.publication.configure.ClassicsPublicationExecutorConfiguration;
import com.thundax.kuzhambu.classics.application.publication.configure.ClassicsPublicationProperties;
import java.time.Duration;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class ClassicsPublicationExecutorConfigurationTest {
    @Test
    void shouldExposeFixedRuntimeDefaults() {
        ClassicsPublicationProperties properties = new ClassicsPublicationProperties();

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getDispatchFixedDelay()).isEqualTo(Duration.ofSeconds(5));
        assertThat(properties.getSuccessReconcileFixedDelay()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.getFailureReconcileFixedDelay()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.getEsCleanupFixedDelay()).isEqualTo(Duration.ofSeconds(60));
        assertThat(properties.getFastgptCleanupFixedDelay()).isEqualTo(Duration.ofSeconds(60));
        assertThat(properties.getDispatchLease()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.getSliceLease()).isEqualTo(Duration.ofMinutes(10));
        assertThat(properties.getCleanupLease()).isEqualTo(Duration.ofMinutes(5));
        assertThat(properties.getRetryDelay()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.getClaimLimit()).isEqualTo(20);
        assertThat(properties.getExecutorCoreSize()).isEqualTo(2);
        assertThat(properties.getExecutorMaxSize()).isEqualTo(4);
        assertThat(properties.getExecutorQueueCapacity()).isEqualTo(100);
        assertThat(properties.getExecutorAwaitTermination()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void shouldUseBoundedAbortPolicyExecutor() {
        ClassicsPublicationProperties properties = new ClassicsPublicationProperties();
        ThreadPoolTaskExecutor executor =
                new ClassicsPublicationExecutorConfiguration().classicsPublicationTaskExecutor(properties);

        try {
            assertThat(ClassicsPublicationExecutorConfiguration.TASK_EXECUTOR)
                    .isEqualTo("classicsPublicationTaskExecutor");
            assertThat(executor.getThreadNamePrefix()).isEqualTo("classics-publication-");
            assertThat(executor.getCorePoolSize()).isEqualTo(2);
            assertThat(executor.getMaxPoolSize()).isEqualTo(4);
            assertThat(executor.getThreadPoolExecutor().getQueue().remainingCapacity())
                    .isEqualTo(100);
            assertThat(executor.getThreadPoolExecutor().getRejectedExecutionHandler())
                    .isInstanceOf(ThreadPoolExecutor.AbortPolicy.class);
        } finally {
            executor.shutdown();
        }
    }
}
