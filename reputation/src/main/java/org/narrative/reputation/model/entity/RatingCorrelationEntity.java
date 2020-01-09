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
public class RatingCorrelationEntity extends AbstractEventReputationEntity {
    private int majorityVoteCount;
    private int totalVoteCount;

    @Builder
    public RatingCorrelationEntity(long userOid, Instant lastUpdated, UUID lastEventId, Instant lastEventTimestamp, int majorityVoteCount, int totalVoteCount) {
        super(userOid, lastUpdated, lastEventId, lastEventTimestamp);
        this.majorityVoteCount = majorityVoteCount;
        this.totalVoteCount = totalVoteCount;
    }
}
