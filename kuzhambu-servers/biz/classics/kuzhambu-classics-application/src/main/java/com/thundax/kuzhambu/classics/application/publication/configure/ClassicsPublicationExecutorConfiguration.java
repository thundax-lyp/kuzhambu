package com.thundax.kuzhambu.classics.application.publication.configure;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ClassicsPublicationExecutorConfiguration {
    public static final String TASK_EXECUTOR = "classicsPublicationTaskExecutor";

    @Bean(name = TASK_EXECUTOR)
    public ThreadPoolTaskExecutor classicsPublicationTaskExecutor(ClassicsPublicationProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("classics-publication-");
        executor.setCorePoolSize(properties.getExecutorCoreSize());
        executor.setMaxPoolSize(properties.getExecutorMaxSize());
        executor.setQueueCapacity(properties.getExecutorQueueCapacity());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(
                (int) properties.getExecutorAwaitTermination().toSeconds());
        executor.initialize();
        return executor;
    }
}
