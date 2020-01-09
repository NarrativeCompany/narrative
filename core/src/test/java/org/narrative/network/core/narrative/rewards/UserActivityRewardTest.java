package org.narrative.network.core.narrative.rewards;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 2019-05-29
 * Time: 12:27
 *
 * @author brian
 */
public class UserActivityRewardTest {

    // Test names should follow the standard: MethodName_StateUnderTest_ExpectedBehavior
    @Test
    void applyBonus_NoBonus_ThrowsException() {
        UserActivityReward reward = new UserActivityReward();
        assertThrows(Throwable.class, () -> {
            reward.applyBonus(UserActivityBonus.NONE);
        });
    }

    @Test
    void applyBonus_Tier1Bonus_ValueCorrect() {
        UserActivityReward reward = new UserActivityReward();
        reward.setPoints(50);
        reward.applyBonus(UserActivityBonus.TIER_1);
        assertEquals(reward.getPoints(), 65);
    }

    @Test
    void applyBonus_Tier2Bonus_ValueCorrect() {
        UserActivityReward reward = new UserActivityReward();
        reward.setPoints(50);
        reward.applyBonus(UserActivityBonus.TIER_2);
        assertEquals(reward.getPoints(), 60);
    }

    @Test
    void applyBonus_Tier3Bonus_ValueCorrect() {
        UserActivityReward reward = new UserActivityReward();
        reward.setPoints(50);
        reward.applyBonus(UserActivityBonus.TIER_3);
        assertEquals(reward.getPoints(), 55);
    }
}
