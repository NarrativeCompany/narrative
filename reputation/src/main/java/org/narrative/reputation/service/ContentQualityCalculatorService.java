package org.narrative.reputation.service;

import org.narrative.shared.event.reputation.LikeEvent;

public interface ContentQualityCalculatorService {
    LikeEvent updateContentQualityWithEvent(LikeEvent likeEvent);
    double getContentQualityScoreForUser(long userOid);

}
