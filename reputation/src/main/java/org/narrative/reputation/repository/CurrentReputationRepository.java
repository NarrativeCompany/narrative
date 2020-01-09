package org.narrative.reputation.repository;

import org.narrative.reputation.model.entity.CurrentReputationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CurrentReputationRepository extends JpaRepository<CurrentReputationEntity, Long> {
    /**
     * Calculate average reputation score for all users
     */
    @Query("select coalesce(avg(r.totalScore), 0) from CurrentReputationEntity r")
    double calculateMeanReputationScore();

    /**
     * Calculate “Total Quality Members” or “TQM”
     */
    @Query(nativeQuery = true, value = "select count(1) from CurrentReputation as r where r.totalScore >= :meanRepScore")
    long countByReputationScoreGreaterThanEqual(@Param("meanRepScore") double meanRepScore);
}
