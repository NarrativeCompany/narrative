package org.narrative.reputation.model.entity;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * View backed entity that assembles all of the pieces of the reputation score.
 */
@Getter
@Entity
public class ConsolidatedCurReputationEntity {
    @Id
    private long userOid;
    private double qualityAnalysis;
    private boolean kycVerified;
    private int totalScore;
    private double commentLikePoints;
    private double commentDislikePoints;
    private double contentLikePoints;
    private double contentDislikePoints;
    private int contentRatingsReceivedCount;
    private double userQualityFollowerRatio;
    private double userQualityFollowerPctRank;
    private int ratingMajorityVoteCount;
    private int ratingTotalVoteCount;
    private int corrMajorityVoteCount;
    private int corrTotalVoteCount;
}
