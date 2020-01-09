package org.narrative.reputation.repository;

import org.narrative.reputation.model.entity.VoteCorrelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteCorrelationRepository extends JpaRepository<VoteCorrelationEntity, Long> {
}
