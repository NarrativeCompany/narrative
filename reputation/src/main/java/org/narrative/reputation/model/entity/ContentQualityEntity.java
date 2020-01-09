package org.narrative.reputation.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContentQualityEntity extends AbstractEventReputationEntity {
    private double contentLikePoints;
    private double contentDislikePoints;
    private double commentLikePoints;
    private double commentDislikePoints;
    private int contentRatingsReceivedCount;

    @Builder
    public ContentQualityEntity(long userOid, Instant lastUpdated, UUID lastEventId, Instant lastEventTimestamp, double contentLikePoints, double contentDislikePoints, double commentLikePoints, double commentDislikePoints, int contentRatingsReceivedCount) {
        super(userOid, lastUpdated, lastEventId, lastEventTimestamp);
        this.contentLikePoints = contentLikePoints;
        this.contentDislikePoints = contentDislikePoints;
        this.commentLikePoints = commentLikePoints;
        this.commentDislikePoints = commentDislikePoints;
        this.contentRatingsReceivedCount = contentRatingsReceivedCount;
    }
}
