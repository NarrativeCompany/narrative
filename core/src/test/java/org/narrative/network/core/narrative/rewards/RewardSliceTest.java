package org.narrative.network.core.narrative.rewards;

import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.customizations.narrative.NrveValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 2019-05-16
 * Time: 19:27
 *
 * @author jonmark
 */
class RewardSliceTest {

    // Test names should follow the standard: MethodName_StateUnderTest_ExpectedBehavior

    @Test
    void getCurrentPercent_SlicePercentage_TotalsOneHundred() {
        testPercentageTotal(RewardSlice::getCurrentPercent);
    }

    @Test
    void getFuturePercent_SlicePercentage_TotalsOneHundred() {
        testPercentageTotal(RewardSlice::getFuturePercent);
    }

    private static void testPercentageTotal(Function<RewardSlice, BigDecimal> percentageFunction) {
        BigDecimal total = BigDecimal.ZERO;
        for (RewardSlice slice : RewardSlice.values()) {
            total = total.add(percentageFunction.apply(slice));
        }

        assertEquals(total.compareTo(BigDecimal.ONE), 0);
    }

    @Test
    void RewardSlice_FullDistribution_SimulatedCurrentDistribution() {
        simulateDistribution(RewardSlice::getCurrentPercent);
    }

    @Test
    void RewardSlice_FullDistribution_SimulatedFutureDistribution() {
        simulateDistribution(RewardSlice::getFuturePercent);
    }

    private static void simulateDistribution(Function<RewardSlice, BigDecimal> percentageFunction) {
        EnumSet<RewardSlice> pointBasedSlices = EnumSet.of(
                RewardSlice.CONTENT_CREATORS,
                RewardSlice.USER_ACTIVITY,
                RewardSlice.NICHE_MODERATORS,
                RewardSlice.NICHE_OWNERS,
                RewardSlice.ELECTORATE
        );
        // jw: let's start with a million Nrve
        NrveValue totalRewards = new NrveValue(BigDecimal.valueOf(1000000));
        NrveValue distributedRewards = NrveValue.ZERO;
        long totalTransactions = 0;

        // jw: now, let's iterate over all of the slice types and process any of them
        for (RewardSlice slice : RewardSlice.values()) {
            BigDecimal percentage = percentageFunction.apply(slice);

            // jw: if the percentage is zero, skip this one because it is not supported yet.
            if (percentage.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Random random = new Random();
            NrveValue sliceRewards = RewardUtils.calculateNrveShare(percentage, totalRewards, RewardUtils.ROUNDING_MODE);
            if (pointBasedSlices.contains(slice)) {
                // jw: let's give this point based slice a pool of points between 1000 and 10000
                long totalPoints = 1000 + random.nextInt(9001);
                long pointsDistributed = 0;

                // jw: while we have yet to distribute all points, let's keep handing out rewards
                do {
                    // jw: let's give out between 10 and 100 points per cycle.
                    long points = 10 + random.nextInt(91);
                    // jw: let's ensure that we never exceed the totalPoints, otherwise this could throw off our values
                    points = Math.min(points, totalPoints - pointsDistributed);

                    NrveValue pointsReward = RewardUtils.calculateNrveShare(sliceRewards, points, totalPoints);

                    distributedRewards = distributedRewards.add(pointsReward);
                    totalTransactions++;

                    pointsDistributed += points;

                } while(pointsDistributed < totalPoints);
            } else {
                totalTransactions++;
                distributedRewards = distributedRewards.add(sliceRewards);
            }
        }

        // jw: now that we are done, we should never have distributed more rewards than the total (otherwise something is wrong with our utility functions).
        assertTrue(totalRewards.compareTo(distributedRewards) >= 0, "More rewards were distributed than we had to give.");
        // jw: this one is a bit trickier to understand, but the simple way to explain it is that since we distributed all values in pieces
        //     based on full slices or point distributions, the remainder should just be the fractional values that were left behind due to
        //     rounding during transaction calculation. Because of that the number of transactions we ran acts as a upper limit for the amount
        //     of neurons that should be left behind.
        assertTrue(totalRewards.subtract(distributedRewards).toNeurons() <= totalTransactions, "The amount of neurons left over should be less than or equal to the number of transactions we made.");
    }
}