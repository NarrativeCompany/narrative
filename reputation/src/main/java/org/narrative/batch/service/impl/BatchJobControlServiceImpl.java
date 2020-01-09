package org.narrative.batch.service.impl;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.narrative.batch.config.BatchProperties;
import org.narrative.batch.model.BatchJobControlEntity;
import org.narrative.batch.model.BatchJobCtlHistEntity;
import org.narrative.batch.repository.BatchJobControlHistoryRepository;
import org.narrative.batch.repository.BatchJobControlRepository;
import org.narrative.batch.service.BatchJobControlService;
import org.narrative.shared.util.NetworkUtil;
import org.springframework.batch.core.BatchStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Transactional
public class BatchJobControlServiceImpl implements BatchJobControlService {
    private final BatchJobControlRepository controlRepository;
    private final BatchJobControlHistoryRepository historyRepository;
    private final BatchProperties batchProperties;
    private final TransactionTemplate transactionTemplate;

    public BatchJobControlServiceImpl(BatchJobControlRepository controlRepository, BatchJobControlHistoryRepository historyRepository, BatchProperties batchProperties, TransactionTemplate transactionTemplate) {
        this.controlRepository = controlRepository;
        this.historyRepository = historyRepository;
        this.batchProperties = batchProperties;
        this.transactionTemplate = transactionTemplate;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isSingletonJobExecutionCandidate(String jobName) {
        Optional<BatchJobControlEntity> entityOpt = controlRepository.findById(jobName);

        BatchJobControlEntity entity = entityOpt.orElse(null);
        return isSingletonJobExecutionCandidate(jobName, entity, Instant.now());
    }

    @Override
    public Instant findLastJobExecutionStartInstant(String jobName) {
        Optional<BatchJobControlEntity> jobControlEntityOpt = controlRepository.findById(jobName);
        if (jobControlEntityOpt.isPresent()) {
            return jobControlEntityOpt.get().getStartTime();
        } else {
            return Instant.EPOCH;
        }
    }

    @Override
    public Instant findLastSuccessfulJobExecutionStartInstant(String jobName) {
        Optional<BatchJobControlEntity> jobControlEntityOpt = controlRepository.findById(jobName);
        // Default to epoch if the last job was not successful
        if (jobControlEntityOpt.isPresent() && BatchStatus.COMPLETED.equals(jobControlEntityOpt.get().getStatus())) {
            return jobControlEntityOpt.get().getStartTime();
        } else {
            return Instant.EPOCH;
        }
    }

    @VisibleForTesting
    boolean isSingletonJobExecutionCandidate(String jobName, BatchJobControlEntity entity, Instant now) {
        boolean res;
        if (entity == null) {
            res = true;
        } else {
            boolean isRestartableStatus = !entity.getStatus().isRunning();
            boolean isJobDurationExpired = entity.getStatus().isRunning() &&
                    isRunningJobDurationExpired(entity, now);

            res = (isRestartableStatus || isJobDurationExpired);
        }

        log.trace("Job {} is execution candidate: {}", jobName, res);

        return res;
    }

    @Transactional(propagation = Propagation.NEVER)
    @Override
    public boolean acquireOwnershipOfSingletonJob(String jobName, long jobId, long jobInstanceId, long jobExecutionId) {
        boolean res = true;
        Instant now = Instant.now();

        Optional<BatchJobControlEntity> entityOpt = controlRepository.findById(jobName);

        // If there is an entity, this isn't the first job execution
        if (entityOpt.isPresent()) {
            BatchJobControlEntity entity = entityOpt.get();

            // Can we lock for this job?
            if (isSingletonJobExecutionCandidate(jobName, entity, now)) {
                entity.setJobId(jobId);
                entity.setJobInstanceId(jobInstanceId);
                entity.setJobExecutionId(jobExecutionId);
                entity.setHost(NetworkUtil.getHostName());
                entity.setStartTime(now);
                entity.setEndTime(null);
                entity.setStatus(BatchStatus.STARTED);

                // Optimistic lock will fail if another node gets here first
                try {
                    transactionTemplate.execute(status -> controlRepository.saveAndFlush(entity));
                } catch (ObjectOptimisticLockingFailureException e) {
                    res = false;
                    log.debug("Could not take ownership of job {} - another node gained ownership before this node", jobName);
                }
            } else {
                res = false;
            }
        } else {
            // If we can insert, we own the job otherwise another node beat us to it
            BatchJobControlEntity entity = buildNewControlEntity(jobName, jobId, jobInstanceId, jobExecutionId, now);
            try {
                transactionTemplate.execute(status -> controlRepository.saveAndFlush(entity));
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
                res = false;
                log.debug("Could not take initial ownership of job {} - another node gained ownership before this node", jobName);
            }
        }

        if (res) {
            log.info("Node now has ownership of job {}", jobName);
        }

        return  res;
    }

    @VisibleForTesting
    BatchJobControlEntity buildNewControlEntity(String jobName, long jobId, long jobInstanceId, long jobExecutionId, Instant now) {
        return BatchJobControlEntity.builder()
                .jobName(jobName)
                .jobId(jobId)
                .jobInstanceId(jobInstanceId)
                .jobExecutionId(jobExecutionId)
                .host(NetworkUtil.getHostName())
                .startTime(now)
                .status(BatchStatus.STARTED)
                .build();
    }

    private boolean isRunningJobDurationExpired(BatchJobControlEntity batchJobControlEntity, Instant now) {
        return  batchJobControlEntity.getStartTime().plus(batchProperties.getMaxJobDuration()).isBefore(now);
    }

    @Override
    public void completeOwnedSingletonJob(String jobName, BatchStatus jobStatus) {
        Instant now = Instant.now();

        // Update the singleton row
        BatchJobControlEntity entity = controlRepository.getOne(jobName);
        entity.setEndTime(now);
        entity.setStatus(jobStatus);

        controlRepository.save(entity);

        log.info("Node completed owned job:{} id:{} instance:{} status:{}", jobName, entity.getJobId(), entity.getJobInstanceId(), jobStatus);

        // Push into the history
        BatchJobCtlHistEntity histEntity = BatchJobCtlHistEntity.builder()
                .id(BatchJobCtlHistEntity.BatchJobCtlHistId.builder()
                        .jobName(entity.getJobName())
                        .jobId(entity.getJobId())
                        .jobInstanceId(entity.getJobInstanceId())
                        .jobExecutionId(entity.getJobExecutionId())
                        .build()
                )
                .host(entity.getHost())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .status(entity.getStatus())
                .build();

        historyRepository.save(histEntity);
    }
}
