package com.thundax.kuzhambu.ai.application.scenario.configure;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Bounds graph extraction requests independently from interactive refinement.
 *
 * <p>Graph batches may contain thousands of materials. A bounded provider-facing executor prevents one
 * batch from exhausting the model provider's concurrent-stream allowance.
 */
@Configuration
public class KnowledgeGraphExtractionExecutorConfiguration {

    public static final String TASK_EXECUTOR = "knowledgeGraphExtractionTaskExecutor";

    @Bean(name = TASK_EXECUTOR)
    public ThreadPoolTaskExecutor knowledgeGraphExtractionTaskExecutor(
            @Value("${kuzhambu.ai.knowledge-graph.executor.core-size:2}") int coreSize,
            @Value("${kuzhambu.ai.knowledge-graph.executor.max-size:2}") int maxSize,
            @Value("${kuzhambu.ai.knowledge-graph.executor.queue-capacity:200}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("knowledge-graph-extraction-");
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
