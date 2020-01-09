package org.narrative.reputation.service;

import org.narrative.shared.event.reputation.ConsensusChangedEvent;
import org.narrative.shared.event.reputation.RatingEvent;

public interface RatingCorrelationService {
    RatingEvent updateRatingCorrelationWithRatingEvent(RatingEvent ratingEvent);
    ConsensusChangedEvent updateRatingCorrelationWithRatingConsensusChangedEvent(ConsensusChangedEvent consensusChangedEvent);
    double getRatingCorrelationScoreForUser(long userOid);

}
