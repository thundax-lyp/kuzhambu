package com.thundax.kuzhambu.ai.application.scenario.configure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class KnowledgeGraphExtractionExecutorConfigurationTest {

    @Test
    void saturatedExecutorShouldRejectInsteadOfRunningProviderCallOnSubmitterThread() {
        ThreadPoolTaskExecutor executor =
                new KnowledgeGraphExtractionExecutorConfiguration().knowledgeGraphExtractionTaskExecutor(1, 1, 1);

        assertThat(executor.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.AbortPolicy.class);

        executor.shutdown();
    }
}
