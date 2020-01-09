package org.narrative.reputation.repository;

import org.narrative.reputation.model.entity.ConductStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ConductStatusRepository extends JpaRepository<ConductStatusEntity, UUID> {

    List<ConductStatusEntity> findByUserOidAndEventTimestampAfter(long userOid, Instant timestamp);
    List<ConductStatusEntity> findByUserOid(long userOid);

    @Modifying
    int deleteByEventTimestampBefore(Instant minConductStatusEventTimestamp);
}
