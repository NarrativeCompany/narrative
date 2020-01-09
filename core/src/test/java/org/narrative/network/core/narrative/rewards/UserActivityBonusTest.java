package org.narrative.network.core.narrative.rewards;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 2019-05-15
 * Time: 20:11
 *
 * @author jonmark
 */
class UserActivityBonusTest {

    // Test names should follow the standard: MethodName_StateUnderTest_ExpectedBehavior
    @Test
    void getBonus_Descending_HasDescendingPercentages() {
        BigDecimal previousBonus = null;
        for (UserActivityBonus bonus : UserActivityBonus.values()) {
            BigDecimal currentBonus = bonus.getBonusMultiplier();
            if(previousBonus!=null) {
                assertTrue(previousBonus.compareTo(currentBonus) > 0);
            }
            // bl: all bonus multipliers should be greater than 1, but less than 2
            if(currentBonus!=null) {
                assertTrue(currentBonus.compareTo(BigDecimal.ONE) > 0);
                assertTrue(currentBonus.compareTo(BigDecimal.valueOf(2)) < 0);
            }

            previousBonus = currentBonus;
        }
    }
}