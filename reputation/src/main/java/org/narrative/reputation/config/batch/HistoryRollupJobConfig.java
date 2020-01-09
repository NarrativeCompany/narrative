package org.narrative.reputation.config.batch;

import lombok.extern.slf4j.Slf4j;
import org.narrative.batch.listener.LoggingStepListener;
import org.narrative.batch.util.BatchJobHelper;
import org.narrative.batch.util.JobShouldRunStep;
import org.narrative.reputation.batch.historyrollup.DailyHistoryRollupTasklet;
import org.narrative.reputation.batch.historyrollup.DailyHistoryShouldExecuteDecider;
import org.narrative.reputation.batch.historyrollup.MonthlyHistoryRollupTasklet;
import org.narrative.reputation.batch.historyrollup.MonthlyHistoryShouldExecuteDecider;
import org.narrative.reputation.batch.historyrollup.WeeklyHistoryRollupTasklet;
import org.narrative.reputation.batch.historyrollup.WeeklyHistoryShouldExecuteDecider;
import org.narrative.reputation.config.ReputationProperties;
import org.slf4j.event.Level;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import static org.narrative.batch.util.JobShouldRunStep.*;
import static org.narrative.reputation.batch.historyrollup.HistoryStepShouldExecuteDecider.*;

@Slf4j
@Configuration
public class HistoryRollupJobConfig {
    public static final String HISTORY_ROLLUP_JOB = "historyRollupJob";
    public static final String DAILY_HISTORY_ROLLUP_STEP = "dailyHistoryRollupStep";
    public static final String WEEKLY_HISTORY_ROLLUP_STEP = "weeklyHistoryRollupStep";
    public static final String MONTHLY_HISTORY_ROLLUP_STEP = "monthlyHistoryRollupStep";
    public static final String ALL_PATTERN = "*";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final BatchJobHelper batchJobHelper;
    private final ReputationProperties reputationProperties;
    private final PlatformTransactionManager platformTransactionManager;
    private final DefaultTransactionAttribute stepTransactionAttribute;

    public HistoryRollupJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, BatchJobHelper batchJobHelper, ReputationProperties reputationProperties, PlatformTransactionManager platformTransactionManager) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.batchJobHelper = batchJobHelper;
        this.reputationProperties = reputationProperties;
        this.platformTransactionManager = platformTransactionManager;
        this.stepTransactionAttribute = new DefaultTransactionAttribute();
        this.stepTransactionAttribute.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
    }

    /**
     * Set up the daily rollup job
     */
    @Bean
    public Job historyRollupJob(DailyHistoryShouldExecuteDecider dailyHistoryShouldExecuteDecider,
                                     @Qualifier("dailyHistoryRollupStep") Step dailyHistoryRollupSpec,
                                     WeeklyHistoryShouldExecuteDecider weeklyHistoryShouldExecuteDecider,
                                     @Qualifier("weeklyHistoryRollupStep") Step weeklyHistoryRollupStep,
                                     MonthlyHistoryShouldExecuteDecider monthlyHistoryShouldExecuteDecider,
                                     @Qualifier("monthlyHistoryRollupStep") Step monthlyHistoryRollupStep) {
        return jobBuilderFactory.get(HISTORY_ROLLUP_JOB)
                .listener(historyRollupJobExecutionListener())

                // Should this job run?
                .flow(historyRollupJobShouldRunStep())
                    .on(EXIT_JOB).end()

                // Should the daily steps run?
                .next(dailyHistoryShouldExecuteDecider)
                    .on(CONTINUE_STEP).to(dailyHistoryRollupSpec)

                // Next is the weekly steps
                .from(dailyHistoryShouldExecuteDecider).on(ALL_PATTERN)
                    .to(weeklyHistoryShouldExecuteDecider)
                    .on(CONTINUE_STEP).to(weeklyHistoryRollupStep)
                .from(dailyHistoryRollupSpec).on(ExitStatus.FAILED.getExitCode())
                    .fail()
                .from(dailyHistoryRollupSpec).on(ALL_PATTERN)
                    .to(weeklyHistoryShouldExecuteDecider)
                    .on(CONTINUE_STEP).to(weeklyHistoryRollupStep)

                // Next is the monthly steps
                .from(weeklyHistoryShouldExecuteDecider).on(ALL_PATTERN)
                    .to(monthlyHistoryShouldExecuteDecider)
                    .on(CONTINUE_STEP).to(monthlyHistoryRollupStep)
                .from(weeklyHistoryRollupStep).on(ExitStatus.FAILED.getExitCode())
                    .fail()
                .from(weeklyHistoryRollupStep).on(ALL_PATTERN)
                    .to(monthlyHistoryShouldExecuteDecider)
                    .on(CONTINUE_STEP).to(monthlyHistoryRollupStep)

                // Route to end
                .from(monthlyHistoryShouldExecuteDecider)
                    .on(ALL_PATTERN)
                    .end(BatchStatus.COMPLETED.name())
                .from(monthlyHistoryRollupStep).on(ExitStatus.FAILED.getExitCode())
                    .fail()
                .from(monthlyHistoryRollupStep)
                    .on(ALL_PATTERN)
                    .end(BatchStatus.COMPLETED.name())

                .end()

                .build();
    }

    @Bean
    public JobExecutionListener historyRollupJobExecutionListener() {
        return batchJobHelper.buildSingletonIntervalJobExecutionListener(
                HISTORY_ROLLUP_JOB,
                reputationProperties.getDailyHistoryRollupJobProperties().getIntervalJobProperties(),
                log);
    }

    @Bean
    public Step historyRollupJobShouldRunStep() {
        JobShouldRunStep jobShouldRunStep = new JobShouldRunStep(log);
        return stepBuilderFactory
                .get(jobShouldRunStep.buildStepName(HISTORY_ROLLUP_JOB))
                .tasklet(jobShouldRunStep)
                .build();
    }

    @Bean
    public Step dailyHistoryRollupStep(DailyHistoryRollupTasklet dailyHistoryRollupTasklet){
        return stepBuilderFactory.get(DAILY_HISTORY_ROLLUP_STEP)
                .transactionManager(platformTransactionManager)
                .listener(historyRollupJobLoggingStepListener())
                .tasklet(dailyHistoryRollupTasklet)
                .transactionAttribute(stepTransactionAttribute)
                .build();
    }

    @Bean
    public Step weeklyHistoryRollupStep(WeeklyHistoryRollupTasklet weeklyHistoryRollupTasklet){
        return stepBuilderFactory.get(WEEKLY_HISTORY_ROLLUP_STEP)
                .transactionManager(platformTransactionManager)
                .listener(historyRollupJobLoggingStepListener())
                .tasklet(weeklyHistoryRollupTasklet)
                .transactionAttribute(stepTransactionAttribute)
                .build();
    }

    @Bean
    public Step monthlyHistoryRollupStep(MonthlyHistoryRollupTasklet monthlyHistoryRollupTasklet){
        return stepBuilderFactory.get(MONTHLY_HISTORY_ROLLUP_STEP)
                .transactionManager(platformTransactionManager)
                .listener(historyRollupJobLoggingStepListener())
                .tasklet(monthlyHistoryRollupTasklet)
                .transactionAttribute(stepTransactionAttribute)
                .build();
    }

    @Bean
    public LoggingStepListener historyRollupJobLoggingStepListener() {
        return new LoggingStepListener(log, Level.INFO);
    }
}
