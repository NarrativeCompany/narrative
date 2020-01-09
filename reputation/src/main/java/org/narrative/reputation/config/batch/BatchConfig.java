package org.narrative.reputation.config.batch;

import org.narrative.batch.repository.BatchJobControlHistoryRepository;
import org.narrative.batch.repository.BatchJobControlRepository;
import org.narrative.batch.service.BatchJobControlService;
import org.narrative.batch.service.BatchJobMetadataPurgeService;
import org.narrative.batch.service.impl.BatchJobControlServiceImpl;
import org.narrative.batch.service.impl.BatchJobMetadataPurgeServiceImpl;
import org.narrative.batch.util.BatchJobHelper;
import org.narrative.reputation.config.ReputationProperties;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
public class BatchConfig {
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager platformTransactionManager) {
        TransactionTemplate txTemplate = new TransactionTemplate(platformTransactionManager);
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        return txTemplate;
    }

    /**
     * Shared task executor to use for Spring Batch job execution
     */
    @Bean
    public TaskExecutor batchTaskExecutor(ReputationProperties reputationProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(reputationProperties.getBatchJob().getMaxThreadPoolSize());
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("batch_executor_thread");
        return executor;
    }

    /**
     * Custom batch configurer so we can use our job launcher
     */
    @Bean
    public BatchConfigurer batchConfigurer(BatchProperties batchProperties,
                                           DataSource dataSource,
                                           TransactionManagerCustomizers transactionManagerCustomizers,
                                           EntityManagerFactory entityManagerFactory,
                                           @Qualifier("batchTaskExecutor") TaskExecutor taskScheduler,
                                           PlatformTransactionManager platformTransactionManager) {
        return new NarrativeBatchConfigurer(
                batchProperties,
                dataSource,
                transactionManagerCustomizers,
                entityManagerFactory,
                taskScheduler);
    }

    /**
     * Singleton job control service
     */
    @Bean
    public BatchJobControlService batchJobControlService(BatchJobControlRepository batchJobControlRepository,
                                                         BatchJobControlHistoryRepository batchJobControlHistoryRepository,
                                                         ReputationProperties reputationProperties,
                                                         TransactionTemplate transactionTemplate) {
        return new BatchJobControlServiceImpl(batchJobControlRepository, batchJobControlHistoryRepository, reputationProperties.getBatchJob(), transactionTemplate);
    }

    /**
     * Batch job builder helper
     */
    @Bean
    public BatchJobHelper batchJobBuilderHelper(BatchJobControlService batchJobControlService,
                                                JobLauncher jobLauncher) {
        return new BatchJobHelper(batchJobControlService, jobLauncher);
    }

    /**
     * Spring batch metadata cleanup service
     */
    @Bean
    public BatchJobMetadataPurgeService batchJobMetadataPurgeService(JdbcTemplate jdbcTemplate) {
        return new BatchJobMetadataPurgeServiceImpl(jdbcTemplate);
    }
}

