package org.narrative.network.core.narrative.rewards.services.slices;

import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.rewards.UserActivityReward;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.List;

/**
 * Date: 2019-05-30
 * Time: 07:17
 *
 * @author brian
 */
public class UserActivityRewardSliceProcessor extends RewardSliceProcessorBase {
    private static final NetworkLogger logger = new NetworkLogger(UserActivityRewardSliceProcessor.class);

    public UserActivityRewardSliceProcessor(RewardPeriod period, NrveValue totalNrve) {
        super(period, RewardSlice.USER_ACTIVITY, totalNrve);
    }

    @Override
    protected NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected Object doMonitoredTask() {
        long incompleteCount = UserActivityReward.dao().getCountIncompleteRewardTransactionRefs(period);

        long totalPoints = UserActivityReward.dao().getTotalPointsForPeriod(period);

        if(logger.isInfoEnabled()) logger.info("Processing " + incompleteCount + " user activity rewards for " + period.getPeriod() + " with a total of " + totalPoints + " points.");

        // bl: distributing activity rewards is pretty easy at this point. just chunk through 500 users at a time
        chunkProcess(incompleteCount, true, period, new RewardSliceChunkProcessor(slice, 500) {
            @Override
            protected Integer doMonitoredTask() {
                List<UserActivityReward> userActivityRewards = UserActivityReward.dao().getIncompleteUserActivityRewards(period, chunkSize);
                for (UserActivityReward userActivityReward : userActivityRewards) {
                    NrveValue rewardAmount = RewardUtils.calculateNrveShare(nrveSlice, userActivityReward.getPoints(), totalPoints);
                    WalletTransaction transaction = createTransaction(userActivityReward.getUser().getWallet(), rewardAmount);
                    userActivityReward.setTransaction(transaction);
                }
                return userActivityRewards.size();
            }
        });

        return null;
    }
}
