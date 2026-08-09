package com.thundax.kuzhambu.classics.application.publication.configure;

import java.time.Clock;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableConfigurationProperties(ClassicsPublicationProperties.class)
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

    @Bean
    public Clock classicsPublicationClock() {
        return Clock.systemUTC();
    }
}
