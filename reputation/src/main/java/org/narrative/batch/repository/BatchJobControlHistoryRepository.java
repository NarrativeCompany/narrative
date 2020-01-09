package org.narrative.batch.repository;

import org.narrative.batch.model.BatchJobCtlHistEntity;
import org.springframework.batch.core.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Transactional
public interface BatchJobControlHistoryRepository extends JpaRepository<BatchJobCtlHistEntity, BatchJobCtlHistEntity.BatchJobCtlHistId> {
    @Modifying
    int deleteByStatusIsAndEndTimeLessThan(BatchStatus status, Instant deleteStartTime);
    @Modifying
    int deleteByStatusIsNotAndEndTimeLessThan(BatchStatus status, Instant deleteStartTime);
}
