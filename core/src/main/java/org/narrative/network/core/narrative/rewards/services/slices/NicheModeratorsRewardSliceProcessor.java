package org.narrative.network.core.narrative.rewards.services.slices;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.narrative.rewards.NicheContentReward;
import org.narrative.network.core.narrative.rewards.NicheModeratorReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.List;

/**
 * Date: 2019-05-30
 * Time: 07:17
 *
 * @author brian
 */
public class NicheModeratorsRewardSliceProcessor extends RewardSliceProcessorBase {
    private static final NetworkLogger logger = new NetworkLogger(NicheModeratorsRewardSliceProcessor.class);

    public NicheModeratorsRewardSliceProcessor(RewardPeriod period, NrveValue totalNrve) {
        super(period, RewardSlice.NICHE_MODERATORS, totalNrve);
    }

    @Override
    protected NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected Object doMonitoredTask() {
        long incompleteNicheCount = NicheModeratorReward.dao().getIncompleteNicheCountForPeriod(period);

        long totalPoints = NicheContentReward.dao().getTotalPointsForPeriod(period);

        if(logger.isInfoEnabled()) logger.info("Processing " + incompleteNicheCount + " niches for moderator rewards for " + period.getPeriod() + " with a total of " + totalPoints + " points.");

        // chunk through 500 niches at a time, and process each niche one at a time internally
        // bl: don't lock the RewardPeriod here since we will lock it internally, one Niche at a time.
        chunkProcess(incompleteNicheCount, false, period, new RewardSliceChunkProcessor(slice, 500) {
            @Override
            protected Integer doMonitoredTask() {
                List<ObjectPair<OID,Long>> nicheRewardOidAndPoints = NicheModeratorReward.dao().getIncompleteNicheModeratorRewards(period, chunkSize);
                for (ObjectPair<OID,Long> pair : nicheRewardOidAndPoints) {
                    OID nicheRewardOid = pair.getOne();
                    long nichePoints = pair.getTwo();

                    // now, distribute that NRVE evenly to the moderators of the niche
                    doRootAreaTask(true, period, new DistributeNicheModeratorRewardsForNicheTask(nicheRewardOid, nichePoints, totalPoints, totalNrve, nrveSlice));
                }
                return nicheRewardOidAndPoints.size();
            }
        });

        return null;
    }
}
