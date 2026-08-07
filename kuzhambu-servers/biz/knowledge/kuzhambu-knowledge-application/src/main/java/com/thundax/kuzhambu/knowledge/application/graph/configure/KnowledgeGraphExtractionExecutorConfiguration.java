package com.thundax.kuzhambu.knowledge.application.graph.configure;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class KnowledgeGraphExtractionExecutorConfiguration {

    public static final String TASK_EXECUTOR = "knowledgeGraphExtractionTaskExecutor";

    @Bean(name = TASK_EXECUTOR)
    public ThreadPoolTaskExecutor knowledgeGraphExtractionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("knowledge-graph-extraction-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
