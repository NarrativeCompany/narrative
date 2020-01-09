package org.narrative.reputation.config;

import lombok.Data;
import org.narrative.batch.config.BatchProperties;
import org.narrative.batch.config.DataRetentionPolicy;
import org.narrative.batch.config.IntervalJobProperties;
import org.narrative.reputation.config.integration.SpringIntegrationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "narrative.reputation")
public class ReputationProperties {
    /**
     * Spring batch properties for Reputation
     */
    private BatchProperties batchJob = new BatchProperties();

    /**
     * Spring instegration properties for Reputation
     */
    private SpringIntegrationProperties si = new SpringIntegrationProperties();

    /**
     * Shared Spring executor that is used for @Scheduled methods
     */
    @NotNull
    private Integer schedulerExecutorMaxThreads = 10;

    /**
     * Should batch jobs be scheduled?  This is a switch for disabling job scheduling for integ tests
     */
    private boolean batchJobSchedulingEnabled = true;

    @NotNull
    private int userQualityFollowersRatioChunkSize = 500;

    /**
     * qualityOfFollowersJob properties
     */
    private IntervalJobProperties qualityOfFollowersJobProperties = new IntervalJobProperties();

    /**
     * dailyHistoryRollupJob properties
     */
    private HistoryJobProperties dailyHistoryRollupJobProperties = new HistoryJobProperties();

    /**
     * dailyHistoryRollupJob properties
     */
    private HistoryJobProperties weeklyHistoryRollupJobProperties = new HistoryJobProperties();

    /**
     * dailyHistoryRollupJob properties
     */
    private HistoryJobProperties monthlyHistoryRollupJobProperties = new HistoryJobProperties();

    /**
     * First day of week
     */
    @NotNull
    private DayOfWeek firstDayOfWeek = DayOfWeek.SUNDAY;

    /**
     * Data retention properties
     */
    private EphemeralDataRetentionProperties dataRetention = new EphemeralDataRetentionProperties();

    @Data
    @Validated
    public static class HistoryJobProperties {
        /**
         * Interval properties for history rollup job
         */
        private IntervalJobProperties intervalJobProperties = new IntervalJobProperties();
        /**
         * Time zone used when making decisions about when to roll up data for history
         */
        @NotNull
        private TimeZone jobDecisionTimezone = TimeZone.getTimeZone("UTC");
        /**
         * Processing chunk size
         */
        @NotNull
        private int chunkSize = 5000;
    }

    @Data
    @Validated
    public static class EphemeralDataRetentionProperties {
        /**
         * Event de duplication retention policy.
         */
        private DataRetentionPolicy eventDedup = new DataRetentionPolicy(
                Duration.of(12, ChronoUnit.HOURS), // Interval
                Duration.of(1, ChronoUnit.DAYS),   // Successful retention
                Duration.of(90, ChronoUnit.DAYS)   // Failed retention
        );
        /**
         * Batch job control history retention policy.
         */
        private DataRetentionPolicy batchJobControlHistory = new DataRetentionPolicy(
                Duration.of(12, ChronoUnit.HOURS),
                Duration.of(90, ChronoUnit.DAYS),
                Duration.of(365, ChronoUnit.DAYS)
        );
        /**
         * Spring Batch metadata retention policy.
         */
        private DataRetentionPolicy springBatchMetadata = new DataRetentionPolicy(
                Duration.of(24, ChronoUnit.HOURS),
                Duration.of(30, ChronoUnit.DAYS),
                Duration.of(90, ChronoUnit.DAYS)
        );
        /**
         * Conduct Status Event retention policy.
         */
        private DataRetentionPolicy conductStatusEvent = new DataRetentionPolicy(
                Duration.of(1, ChronoUnit.HOURS),
                Duration.of(180, ChronoUnit.DAYS),
                Duration.of(365, ChronoUnit.DAYS)
        );
    }
}
