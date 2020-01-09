package org.narrative.network.customizations.narrative.reputation;

import org.narrative.common.util.UnexpectedError;
import org.junit.jupiter.api.Test;
import org.narrative.shared.reputation.config.ReputationConstants;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 2019-05-29
 * Time: 12:59
 *
 * @author brian
 */
public class ReputationMultiplierTierTest {

    // Test names should follow the standard: MethodName_StateUnderTest_ExpectedBehavior
    @Test
    void getMultiplier_Descending_HasDescendingMultipliers() {
        BigDecimal previousMultiplier = null;
        for (ReputationMultiplierTier tier : ReputationMultiplierTier.values()) {
            BigDecimal currentMultiplier = tier.getMultiplier();
            if(previousMultiplier!=null) {
                assertTrue(previousMultiplier.compareTo(currentMultiplier) > 0);
            }
            // bl: all multipliers should be between 0 and 1, when multiplied by 100
            BigDecimal currentMultiplierReal = currentMultiplier.multiply(BigDecimal.valueOf(100));
            assertTrue(currentMultiplierReal.compareTo(BigDecimal.ZERO) > 0);
            assertTrue(currentMultiplierReal.compareTo(BigDecimal.ONE) <= 0);

            previousMultiplier = currentMultiplier;
        }
    }

    @Test
    void minValue_Descending_HasDescendingMinimums() {
        int previousMin = ReputationConstants.MAX_REPUTATION_SCORE;
        for (ReputationMultiplierTier tier : ReputationMultiplierTier.values()) {
            assertTrue(tier.getMinScore() < previousMin);
            previousMin = tier.getMinScore();
        }
    }

    @Test
    void getForScore_MinMaxEdges_ExpectedTiers() {
        // jw: ensure that all bonuses provide expected values at the edges of their range
        int previousMin = ReputationConstants.MAX_REPUTATION_SCORE+1;
        for (ReputationMultiplierTier tier : ReputationMultiplierTier.values()) {
            assertEquals(tier, ReputationMultiplierTier.getForScore(previousMin-1));
            assertEquals(tier, ReputationMultiplierTier.getForScore(tier.getMinScore()));
            previousMin = tier.getMinScore();
        }

        // bl: the minScore for the last tier should be 0!
        assertEquals(0, ReputationMultiplierTier.values()[ReputationMultiplierTier.values().length-1].getMinScore());
    }

    @Test
    void getForScore_Assertions_ExpectedErrors() {
        assertThrows(UnexpectedError.class, () -> ReputationMultiplierTier.getForScore(-1));
        assertThrows(UnexpectedError.class, () -> ReputationMultiplierTier.getForScore(101));
    }
}
