package org.narrative.reputation.util.datagen

import org.apache.commons.lang3.RandomUtils
import org.narrative.reputation.model.entity.ContentQualityEntity

import java.time.Instant

class ContentQualGen {
    static ContentQualityEntity buildContentQualEntity(long userOid,
                                                         UUID lastEventId,
                                                         Double contentLikePoints,
                                                         Double contentDislikePoints,
                                                         Double commentLikePoints,
                                                         Double commentDislikePoints) {
        return ContentQualityEntity.builder()
                .userOid(userOid)
                .lastEventId(lastEventId == null ? UUID.randomUUID() : lastEventId)
                .lastEventTimestamp(Instant.now())
                .contentLikePoints(contentLikePoints == null ? RandomUtils.nextDouble(5.0, 50.0) : contentLikePoints)
                .contentDislikePoints(contentDislikePoints == null ? RandomUtils.nextDouble(5.0, 50.0) : contentDislikePoints)
                .commentLikePoints(commentLikePoints == null ? RandomUtils.nextDouble(5.0, 50.0) : commentLikePoints)
                .commentDislikePoints(commentDislikePoints == null ? RandomUtils.nextDouble(5.0, 50.0) : commentDislikePoints)
                .build()
    }

    static List<ContentQualityEntity> buildContentQualList(count, startIndex = 1) {
        def res = []
        for (def i = 0; i < count; i++) {
            res.add(buildContentQualEntity(i + startIndex, null, null, null, null, null))
        }
        res
    }
}
