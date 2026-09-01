package com.duoc.bancoxyz.batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchTaskExecutorConfig {

    @Bean
    public TaskExecutor batchTaskExecutor(BatchOptimizationConfig optimizationConfig) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(optimizationConfig.getCorePoolSize());
        executor.setMaxPoolSize(optimizationConfig.getMaxPoolSize());
        executor.setQueueCapacity(optimizationConfig.getQueueCapacity());
        executor.setThreadNamePrefix("bancoxyz-batch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(optimizationConfig.getAwaitTerminationSeconds());
        executor.initialize();
        return executor;
    }
}
