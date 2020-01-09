package org.narrative.reputation.service;

import java.util.Set;

/**
 * Service for calculating total reputation score.
 */
public interface TotalReputationScoreCalculatorService {
    /**
     * Calculate total reputation for the specified users.
     *
     * @param userIdSet The user identifiers for which to calculate total reputation
     */
    void calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(Set<Long> userIdSet);
}
