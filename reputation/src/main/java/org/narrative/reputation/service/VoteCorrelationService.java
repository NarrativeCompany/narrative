package org.narrative.reputation.service;

import org.narrative.shared.event.reputation.NegativeQualityEvent;
import org.narrative.shared.event.reputation.VoteEndedEvent;

public interface VoteCorrelationService {
    VoteEndedEvent updateVoteCorrelationWithEvent(VoteEndedEvent voteEndedEvent);
    NegativeQualityEvent updateVoteCorrelationForNegativeEvent(NegativeQualityEvent negativeQualityEvent);
    double getVoteCorrelationScoreForUser(long userOid);
}
