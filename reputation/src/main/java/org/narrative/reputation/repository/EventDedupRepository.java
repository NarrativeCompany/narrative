package org.narrative.reputation.repository;

import org.narrative.reputation.model.entity.EventDedupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Transactional
public interface EventDedupRepository extends JpaRepository<EventDedupEntity, UUID> {
    @Modifying
    @Query("update EventDedupEntity e set e.processed = true, e.processedTimestamp = ?2 where e.uuid = ?1")
    int markDedupAsProcessed(UUID uuid, Instant timeatamp);

    @Modifying
    int deleteByProcessedTrueAndLockTimestampLessThan(Instant minProcessedTimestamp);

    @Modifying
    int deleteByProcessedFalseAndLockTimestampLessThan(Instant minProcessedTimestamp);
}
