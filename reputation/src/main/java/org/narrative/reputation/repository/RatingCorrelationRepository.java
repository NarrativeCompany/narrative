package org.narrative.reputation.repository;

import org.narrative.reputation.model.entity.RatingCorrelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingCorrelationRepository extends JpaRepository<RatingCorrelationEntity, Long> {
}
