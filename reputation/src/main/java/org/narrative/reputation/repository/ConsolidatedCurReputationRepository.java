package org.narrative.reputation.repository;

import org.narrative.reputation.model.entity.ConsolidatedCurReputationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsolidatedCurReputationRepository extends JpaRepository<ConsolidatedCurReputationEntity, Long> {
}
