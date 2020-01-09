package org.narrative.reputation.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Transient;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CurrentReputationEntity extends AbstractEventReputationEntity {
    private double qualityAnalysis;
    private boolean kycVerified;

    /**
     * Instant when user became KYC verified
     */
    private Instant kycVerifiedTimestamp;

    /**
     * Instant when negative conduct status expires
     */
    private Instant negativeConductExpirationTimestamp;

    /**
     * Instant when negative conduct status started
     */
    private Instant negativeConductStartTimestamp;

    /**
     * Aggregate reputation score
     */
    private int totalScore;

    @Builder
    public CurrentReputationEntity(long userOid, Instant lastUpdated, UUID lastEventId, Instant lastEventTimestamp, double qualityAnalysis, boolean kycVerified, Instant kycVerifiedTimestamp, Instant negativeConductExpirationTimestamp, Instant negativeConductStartTimestamp, int totalScore) {
        super(userOid, lastUpdated, lastEventId, lastEventTimestamp);
        this.qualityAnalysis = qualityAnalysis;
        this.kycVerified = kycVerified;
        this.kycVerifiedTimestamp = kycVerifiedTimestamp;
        this.negativeConductExpirationTimestamp = negativeConductExpirationTimestamp;
        this.negativeConductStartTimestamp = negativeConductStartTimestamp;
        this.totalScore = totalScore;
    }

    @Transient
    public boolean isConductNegative(){
        return this.negativeConductExpirationTimestamp != null && this.negativeConductExpirationTimestamp.isAfter(Instant.now());
    }
}
