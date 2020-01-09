package org.narrative.reputation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.narrative.batch.repository.BatchJobControlHistoryRepository;
import org.narrative.batch.service.BatchJobMetadataPurgeService;
import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.repository.ConductStatusRepository;
import org.narrative.reputation.repository.EventDedupRepository;
import org.narrative.reputation.service.EphemeralDataCleanupService;
import org.narrative.shared.spring.metrics.TimedService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@TimedService(percentiles = {0.8, 0.9, 0.99})
@Transactional
public class EphemeralDataCleanupServiceImpl implements EphemeralDataCleanupService {
    private final ReputationProperties retentionProperties;
    private final EventDedupRepository eventDedupRepository;
    private final BatchJobControlHistoryRepository batchJobControlHistoryRepository;
    private final BatchJobMetadataPurgeService batchJobMetadataPurgeService;
    private final ConductStatusRepository conductStatusRepository;

    public EphemeralDataCleanupServiceImpl(ReputationProperties retentionProperties, EventDedupRepository eventDedupRepository, BatchJobControlHistoryRepository batchJobControlHistoryRepository, BatchJobMetadataPurgeService batchJobMetadataPurgeService, ConductStatusRepository conductStatusRepository) {
        this.retentionProperties = retentionProperties;
        this.eventDedupRepository = eventDedupRepository;
        this.batchJobControlHistoryRepository = batchJobControlHistoryRepository;
        this.batchJobMetadataPurgeService = batchJobMetadataPurgeService;
        this.conductStatusRepository = conductStatusRepository;
    }

    private void logResults(String methodName, Instant deleteSuccessInstant, Instant deleteErroredInstant, int successDeleteCount, int erroredDeleteCount) {
        log.info("{} - purged successful older than {} - {} deleted", methodName, deleteSuccessInstant, successDeleteCount);
        log.info("{} - purged errored older than {} - {} deleted", methodName, deleteErroredInstant, erroredDeleteCount);
    }

    Instant getNow() {
        return Instant.now();
    }

    @Override
    public void purgeEventDedupTable() {
        Instant now = getNow();
        Instant successPurgeInstant = now.minus(retentionProperties.getDataRetention().getEventDedup().getRetentionDuration());
        Instant erroredPurgeInstant = now.minus(retentionProperties.getDataRetention().getEventDedup().getErroredRetentionDuration());

        int count = eventDedupRepository.deleteByProcessedTrueAndLockTimestampLessThan(successPurgeInstant);
        int errCount = eventDedupRepository.deleteByProcessedFalseAndLockTimestampLessThan(erroredPurgeInstant);
        logResults("purgeEventDedupTable", successPurgeInstant, erroredPurgeInstant, count, errCount);
    }

    @Override
    public void purgeBatchJobControlHistoryTable() {
        Instant now = getNow();
        Instant successPurgeInstant = now.minus(retentionProperties.getDataRetention().getBatchJobControlHistory().getRetentionDuration());
        Instant erroredPurgeInstant = now.minus(retentionProperties.getDataRetention().getBatchJobControlHistory().getErroredRetentionDuration());

        int count = batchJobControlHistoryRepository.deleteByStatusIsAndEndTimeLessThan(BatchStatus.COMPLETED, successPurgeInstant);
        int errCount = batchJobControlHistoryRepository.deleteByStatusIsNotAndEndTimeLessThan(BatchStatus.COMPLETED, erroredPurgeInstant);
        logResults("purgeBatchJobControlHistoryTable", successPurgeInstant, erroredPurgeInstant, count, errCount);
    }

    @Override
    public void purgeSpringBatchJobData() {
        Instant now = getNow();
        Instant successPurgeInstant = now.minus(retentionProperties.getDataRetention().getSpringBatchMetadata().getRetentionDuration());
        Instant erroredPurgeInstant = now.minus(retentionProperties.getDataRetention().getSpringBatchMetadata().getErroredRetentionDuration());

        int count = batchJobMetadataPurgeService.purgeJobMetadata(successPurgeInstant, false);
        int errCount = batchJobMetadataPurgeService.purgeJobMetadata(erroredPurgeInstant, true);
        logResults("purgeSpringBatchJobData", successPurgeInstant, erroredPurgeInstant, count, errCount);
    }

    @Override
    public void purgeOldConductStatusEvents() {
        Instant now = getNow();
        Instant conductStatusEventPurgeInstant = now.minus(retentionProperties.getDataRetention().getConductStatusEvent().getRetentionDuration());

        int count = conductStatusRepository.deleteByEventTimestampBefore(conductStatusEventPurgeInstant);
        log.info("purgeOldConductStatusEvents - purged events older than {} - {} events deleted", conductStatusEventPurgeInstant, count);
    }
}
