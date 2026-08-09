package com.thundax.kuzhambu.discovery.interfaces.configure;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class DiscoveryQaStreamExecutorConfiguration {

    public static final String QA_STREAM_EXECUTOR = "discoveryQaStreamExecutor";

    @Bean(name = QA_STREAM_EXECUTOR)
    public ThreadPoolTaskExecutor discoveryQaStreamExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("discovery-qa-stream-");
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setAwaitTerminationSeconds(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
