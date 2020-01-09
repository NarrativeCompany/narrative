package org.narrative.reputation.batch.historyrollup;

import lombok.Value;
import org.narrative.batch.service.BatchJobControlService;
import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.model.entity.RollupPeriod;
import org.narrative.reputation.repository.ReputationHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public abstract class HistoryStepShouldExecuteDecider implements JobExecutionDecider {
    public static final String CONTINUE_STEP = "continueStep";
    public static final String NEXT_STEP = "nextStep";
    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    protected final BatchJobControlService batchJobControlService;
    protected final ReputationHistoryRepository reputationHistoryRepository;
    protected final String stepName;
    protected final ReputationProperties.HistoryJobProperties historyJobProperties;
    protected final Logger log;

    public HistoryStepShouldExecuteDecider(BatchJobControlService batchJobControlService, ReputationHistoryRepository reputationHistoryRepository, String stepName, ReputationProperties.HistoryJobProperties historyJobProperties) {
        this.batchJobControlService = batchJobControlService;
        this.reputationHistoryRepository = reputationHistoryRepository;
        this.stepName = stepName;
        this.historyJobProperties = historyJobProperties;
        this.log = LoggerFactory.getLogger(getClass());
    }

    protected LocalDate buildZoneAdjustedLocalDate(Instant instant) {
        ZonedDateTime instantZoned = instant.atZone(historyJobProperties.getJobDecisionTimezone().toZoneId());

        // Convert to LocalDate for comparison
        return LocalDate.from(instantZoned);
    }

    protected Instant getNow() {
        return Instant.now();
    }

    protected LocalDate buildZoneAdjustedNowLocalDate() {
       return buildZoneAdjustedLocalDate(getNow());
    }

    protected LatestDataDate findLatestDataDateForRollupPeriod(RollupPeriod rollupPeriod) {
        LocalDate lastSuccessDate = reputationHistoryRepository.findMaxSnapshotDateForRollupPeriod(rollupPeriod);
        if (lastSuccessDate == null) {
            lastSuccessDate = LocalDate.MIN;
        }
        LocalDate zaLastSuccessDate = buildZoneAdjustedLocalDate(lastSuccessDate.atStartOfDay(UTC_ZONE_ID).toInstant());

        return new LatestDataDate(lastSuccessDate, zaLastSuccessDate);
    }

    @Value
    public static class LatestDataDate {
        private final LocalDate lastSuccessDate;
        private final LocalDate zoneAdjustedSuccessDate;
    }
}
