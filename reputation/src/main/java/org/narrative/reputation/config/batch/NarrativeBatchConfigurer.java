package org.narrative.reputation.config.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.autoconfigure.batch.JpaBatchConfigurer;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.core.task.TaskExecutor;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Slf4j
public class NarrativeBatchConfigurer extends JpaBatchConfigurer {
    private final TaskExecutor taskExecutor;

    protected NarrativeBatchConfigurer(BatchProperties properties, DataSource dataSource, TransactionManagerCustomizers transactionManagerCustomizers, EntityManagerFactory entityManagerFactory, TaskExecutor taskExecutor) {
        super(properties, dataSource, transactionManagerCustomizers, entityManagerFactory);
        this.taskExecutor = taskExecutor;
        log.info("Using batch configurer {}", NarrativeBatchConfigurer.class.getSimpleName());
    }

    @Override
    protected JobLauncher createJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(getJobRepository());
        jobLauncher.setTaskExecutor(taskExecutor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
}
