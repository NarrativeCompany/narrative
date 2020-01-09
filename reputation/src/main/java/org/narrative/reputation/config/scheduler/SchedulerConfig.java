package org.narrative.reputation.config.scheduler;

import org.narrative.reputation.config.ReputationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {
    /**
     * Shared task executor to use for @Scheduled methods
     */
    @Primary
    @Bean
    public ThreadPoolTaskScheduler taskScheduler(ReputationProperties reputationProperties) {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(reputationProperties.getSchedulerExecutorMaxThreads());
        executor.setThreadNamePrefix("default_scheduler_thread");
        executor.initialize();
        return executor;
    }
}
