package org.narrative.reputation.config.batch;

import lombok.extern.slf4j.Slf4j;
import org.narrative.batch.listener.LoggingStepListener;
import org.narrative.batch.util.BatchJobHelper;
import org.narrative.batch.util.JobShouldRunStep;
import org.narrative.reputation.batch.qualityfollowers.PctRankTasklet;
import org.narrative.reputation.batch.qualityfollowers.TQMTasklet;
import org.narrative.reputation.batch.qualityfollowers.UQFRTasklet;
import org.narrative.reputation.config.ReputationProperties;
import org.slf4j.event.Level;
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

@Slf4j
@Configuration
public class QualityOfFollowersJobConfig {
    public static final String QUALITY_OF_FOLLOWERS_JOB = "qualityOfFollowersJob";
    public static final String MEAN_REP_SCORE_AND_TQM_STEP = "meanRepScoreAndTQMStep";
    public static final String USER_QUALITY_FOLLOWER_RATIO_STEP = "userQualityFollowerRatioStep";
    public static final String USER_QUALITY_FOLLOWER_PCT_RANK_STEP = "userQualityFollowerPctRankStep";

    private final PlatformTransactionManager platformTransactionManager;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final BatchJobHelper batchJobHelper;
    private final ReputationProperties reputationProperties;
    private final DefaultTransactionAttribute stepTransactionAttribute;

    public QualityOfFollowersJobConfig(PlatformTransactionManager platformTransactionManager, JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, BatchJobHelper batchJobHelper, ReputationProperties reputationProperties) {
        this.platformTransactionManager = platformTransactionManager;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.batchJobHelper = batchJobHelper;
        this.reputationProperties = reputationProperties;
        this.stepTransactionAttribute = new DefaultTransactionAttribute();
        this.stepTransactionAttribute.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
    }

    /**
     * Set up the flow for the quality of followers batch job
     */
    @Bean
    public Job qualityOfFollowersJob(@Qualifier("meanRepScoreAndTQMStep") Step meanRepScoreAndTQMStep, @Qualifier("userQualityFollowerRatioStep") Step userQualityFollowerRatioStep, @Qualifier("userQualityFollowerPctRankStep") Step userQualityFollowerPctRankStep) {
        return jobBuilderFactory.get(QUALITY_OF_FOLLOWERS_JOB)
                .listener(qualityOfFollowersJobExecutionListener())
                .flow(qualityOfFollowersJobShouldRunStep())
                    .on(EXIT_JOB).end()
                .next(meanRepScoreAndTQMStep)
                .next(userQualityFollowerRatioStep)
                .next(userQualityFollowerPctRankStep)
                .end()
                .build();
    }

    @Bean
    public JobExecutionListener qualityOfFollowersJobExecutionListener() {
        return batchJobHelper.buildSingletonIntervalJobExecutionListener(QUALITY_OF_FOLLOWERS_JOB, reputationProperties.getQualityOfFollowersJobProperties(), log);
    }

    @Bean
    public LoggingStepListener qualityOfFollowersLoggingStepListener() {
        return new LoggingStepListener(log, Level.DEBUG);
    }

    @Bean
    public Step qualityOfFollowersJobShouldRunStep() {
        JobShouldRunStep jobShouldRunStep = new JobShouldRunStep(log);
        return stepBuilderFactory
                    .get(jobShouldRunStep.buildStepName(QUALITY_OF_FOLLOWERS_JOB))
                    .tasklet(jobShouldRunStep)
                    .build();
    }

    @Bean
    public Step meanRepScoreAndTQMStep(TQMTasklet tqmTasklet) {
        return stepBuilderFactory.get(MEAN_REP_SCORE_AND_TQM_STEP)
                .transactionManager(platformTransactionManager)
                .listener(qualityOfFollowersLoggingStepListener())
                .tasklet(tqmTasklet)
                .transactionAttribute(stepTransactionAttribute)
                .build();
    }

    @Bean
    public Step userQualityFollowerRatioStep(UQFRTasklet uqfrTasklet) {
        return stepBuilderFactory.get(USER_QUALITY_FOLLOWER_RATIO_STEP)
                .transactionManager(platformTransactionManager)
                .listener(qualityOfFollowersLoggingStepListener())
                .tasklet(uqfrTasklet)
                .transactionAttribute(stepTransactionAttribute)
                .build();
    }

    @Bean
    public Step userQualityFollowerPctRankStep(PctRankTasklet pctRankTasklet) {
        return stepBuilderFactory.get(USER_QUALITY_FOLLOWER_PCT_RANK_STEP)
                .transactionManager(platformTransactionManager)
                .listener(qualityOfFollowersLoggingStepListener())
                .tasklet(pctRankTasklet)
                .transactionAttribute(stepTransactionAttribute)
                .build();
    }
}
