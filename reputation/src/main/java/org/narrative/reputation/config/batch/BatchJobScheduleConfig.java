package org.narrative.reputation.config.batch;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.narrative.batch.util.BatchJobHelper;
import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.service.EphemeralDataCleanupService;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Configuration
public class BatchJobScheduleConfig {
    private final BatchJobHelper batchJobHelper;
    private final Job qualityOfFollowersJob;
    private final Job historyRollupJob;
    private final ReputationProperties reputationProperties;
    private final TaskScheduler taskScheduler;
    private final EphemeralDataCleanupService ephemeralDataCleanupService;

    public BatchJobScheduleConfig(BatchJobHelper batchJobHelper,
                                  @Qualifier("qualityOfFollowersJob") Job qualityOfFollowersJob,
                                  @Qualifier("historyRollupJob") Job historyRollupJob,
                                  ReputationProperties reputationProperties,
                                  @Qualifier("taskScheduler") TaskScheduler taskScheduler,
                                  EphemeralDataCleanupService ephemeralDataCleanupService) {
        this.batchJobHelper = batchJobHelper;
        this.qualityOfFollowersJob = qualityOfFollowersJob;
        this.historyRollupJob = historyRollupJob;
        this.reputationProperties = reputationProperties;
        this.taskScheduler = taskScheduler;
        this.ephemeralDataCleanupService = ephemeralDataCleanupService;
    }

    /**
     * Schedule reputation batch jobs - stagger initial execution by a random offset
     */
    @Bean
    boolean batchJobScheduleInit() {
        if (reputationProperties.isBatchJobSchedulingEnabled()) {
            // Try to execute each batch job n times per interval in case one of the executions fails due to a transient error condition
            long jobIntervalDivisor = 2L;

            // qualityOfFollowersJob
            Instant initialExecInstant = batchJobHelper.buildRandomOffsetStartInstant(Instant.now(), reputationProperties.getBatchJob());
            Duration interval = reputationProperties.getQualityOfFollowersJobProperties().getJobInterval().dividedBy(jobIntervalDivisor);
            taskScheduler.scheduleWithFixedDelay(() -> batchJobHelper.launchJob(qualityOfFollowersJob, log),
                    initialExecInstant,
                    interval
            );
            logJobConfig(qualityOfFollowersJob, initialExecInstant, interval);

            // dailyHistoryRollupJob
            initialExecInstant = batchJobHelper.buildRandomOffsetStartInstant(initialExecInstant, reputationProperties.getBatchJob());
            interval = reputationProperties.getDailyHistoryRollupJobProperties().getIntervalJobProperties().getJobInterval().dividedBy(jobIntervalDivisor);
            taskScheduler.scheduleWithFixedDelay(() -> batchJobHelper.launchJob(historyRollupJob, log),
                    initialExecInstant,
                    interval
            );
            logJobConfig(historyRollupJob, initialExecInstant, interval);
        } else {
            log.warn("ReputationProperties.isBatchJobSchedulingEnabled() == false so not scheduling batch jobs.  This should only happen in dev environments!!!");
        }

        return true;
    }

    /**
     * Schedule purge jobs - stagger initial execution by a random offset
     */
    @Bean
    boolean purgeJobScheduleInit() {
        if (reputationProperties.isBatchJobSchedulingEnabled()) {
            // Purge old dedup data
            Instant initialExecInstant = batchJobHelper.buildRandomOffsetStartInstant(Instant.now(), reputationProperties.getBatchJob());
            Duration interval = reputationProperties.getDataRetention().getEventDedup().getJobInterval();
            taskScheduler.scheduleWithFixedDelay(() -> {
                        try{
                            ephemeralDataCleanupService.purgeEventDedupTable();
                        } catch(Exception e) {
                            log.error("Error encountered during dedup data cleanup", e);
                        }
                    },
                    initialExecInstant,
                    interval
            );
            log.info("Purge job scheduled for event dedup data - will execute at {} with an interval of {}", initialExecInstant, DurationFormatUtils.formatDurationWords(interval.toMillis(), false, false));

            // Purge old batch job control history data
            initialExecInstant = batchJobHelper.buildRandomOffsetStartInstant(initialExecInstant, reputationProperties.getBatchJob());
            interval = reputationProperties.getDataRetention().getBatchJobControlHistory().getJobInterval();
            taskScheduler.scheduleWithFixedDelay(() -> {
                        try{
                            ephemeralDataCleanupService.purgeBatchJobControlHistoryTable();
                        } catch(Exception e) {
                            log.error("Error encountered during batch job control history data cleanup", e);
                        }
                    },
                    initialExecInstant,
                    interval
            );
            log.info("Purge job scheduled for batch job control history data - will execute at {} with an interval of {}", initialExecInstant, DurationFormatUtils.formatDurationWords(interval.toMillis(), false, false));

            // Purge old Spring Batch metadata
            initialExecInstant = batchJobHelper.buildRandomOffsetStartInstant(initialExecInstant, reputationProperties.getBatchJob());
            interval = reputationProperties.getDataRetention().getSpringBatchMetadata().getJobInterval();
            taskScheduler.scheduleWithFixedDelay(() -> {
                        try{
                            ephemeralDataCleanupService.purgeSpringBatchJobData();
                        } catch(Exception e) {
                            log.error("Error encountered during Spring Batch metadata cleanup", e);
                        }
                    },
                    initialExecInstant,
                    interval
            );
            log.info("Purge job scheduled for Spring Batch metadata - will execute at {} with an interval of {}", initialExecInstant, DurationFormatUtils.formatDurationWords(interval.toMillis(), false, false));
        } else {
            log.warn("ReputationProperties.isBatchJobSchedulingEnabled() == false so not scheduling purge jobs.  This should only happen in dev environments!!!");
        }

        return true;
    }

    private void logJobConfig(Job job, Instant startInstant, Duration interval){
        log.info("Job {} scheduled to execute at {} with an interval of {}", job.getName(), startInstant, DurationFormatUtils.formatDurationWords(interval.toMillis(), false, false));
    }
}
