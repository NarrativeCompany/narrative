package org.narrative.reputation.repository;

import org.narrative.reputation.model.entity.ReputationHistoryEntity;
import org.narrative.reputation.model.entity.RollupPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface ReputationHistoryTestRepository extends JpaRepository<ReputationHistoryEntity, ReputationHistoryEntity> {
    @Transactional
    List<ReputationHistoryEntity> findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod rollupPeriod, LocalDate snapshotDate);
}
