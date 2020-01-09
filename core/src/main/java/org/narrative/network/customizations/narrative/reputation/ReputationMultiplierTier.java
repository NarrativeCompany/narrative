package org.narrative.network.customizations.narrative.reputation;

import com.google.common.annotations.VisibleForTesting;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.UserActivityBonus;
import org.narrative.shared.reputation.config.ReputationConstants;

import java.math.BigDecimal;

/**
 * Date: 2019-05-29
 * Time: 12:54
 *
 * @author brian
 */
public enum ReputationMultiplierTier {
    TIER_1(96,  new BigDecimal("0.01"), UserActivityBonus.TIER_1)
    ,TIER_2(90, new BigDecimal("0.009"), UserActivityBonus.TIER_2)
    ,TIER_3(85, new BigDecimal("0.008"), UserActivityBonus.TIER_3)
    ,TIER_4(80, new BigDecimal("0.006"))
    ,TIER_5(75, new BigDecimal("0.005"))
    ,TIER_6(70, new BigDecimal("0.004"))
    ,TIER_7(60, new BigDecimal("0.002"))
    ,TIER_8(0,  new BigDecimal("0.001"))
    ;

    private final int minScore;
    private final BigDecimal multiplier;
    private final UserActivityBonus activityBonus;

    ReputationMultiplierTier(int minScore, BigDecimal multiplier) {
        this(minScore, multiplier, UserActivityBonus.NONE);
    }

    ReputationMultiplierTier(int minScore, BigDecimal multiplier, UserActivityBonus activityBonus) {
        assert minScore <= ReputationConstants.MAX_REPUTATION_SCORE : "The minScore should never exceed 100.";
        assert minScore >= ReputationConstants.MIN_REPUTATION_SCORE : "The minScore should never be less than 0.";

        this.minScore = minScore;
        this.multiplier = multiplier;
        this.activityBonus = activityBonus;
    }

    @VisibleForTesting
    public int getMinScore() {
        return minScore;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public UserActivityBonus getActivityBonus() {
        return activityBonus;
    }

    public static ReputationMultiplierTier getForScore(int score) {
        if (score > ReputationConstants.MAX_REPUTATION_SCORE || score < ReputationConstants.MIN_REPUTATION_SCORE) {
            throw UnexpectedError.getRuntimeException("We expect all scores to be between 0 and 100!");
        }

        for (ReputationMultiplierTier tier : values()) {
            if (score >= tier.minScore) {
                return tier;
            }
        }

        throw UnexpectedError.getRuntimeException("Failed to identify tier value for score/" + score);
    }
}
