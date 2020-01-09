package org.narrative.network.core.narrative.rewards.services;

import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.customizations.narrative.NrveValue;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Arrays;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-27
 * Time: 12:48
 *
 * @author brian
 */
public class CheckRewardsProcessStatusJob extends NetworkJob {
    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        RewardPeriod rewardPeriod = RewardPeriod.dao().getOldestIncompleteRewardPeriodBefore(RewardUtils.nowYearMonth());

        // bl: if there aren't any incomplete months, then good! nothing further to do.
        if(!exists(rewardPeriod)) {
            return;
        }

        // bl: on production, just send a reminder email to let us know that rewards need to be processed
        if(NetworkRegistry.getInstance().isProductionServer()) {
            String rewardYearMonth = rewardPeriod.getFormatted();
            NetworkRegistry.getInstance().sendDevOpsStatusEmail(rewardYearMonth + " Rewards Not Processed", "Rewards still need to be processed for " + rewardYearMonth + ". Steps completed so far: " + Arrays.toString(rewardPeriod.getCompletedSteps().toArray()));
            return;
        }

        // bl: for local, dev, and staging, let's just apply the fiat adjustment as 0 and kick off rewards to be processed
        for (ProratedRevenueType revenueType : ProratedRevenueType.ACTIVE_TYPES) {
            ProratedMonthRevenue proratedMonthRevenue = ProratedMonthRevenue.dao().getForYearMonthAndType(rewardPeriod.getPeriod(), revenueType);

            getNetworkContext().doAreaTask(Area.dao().getNarrativePlatformArea(), new ApplyFiatAdjustmentTask(
                    proratedMonthRevenue,
                    NrveValue.ZERO
            ));
        }

        // jw: next, let's schedule the job
        ProcessRewardPeriodJob.schedule(rewardPeriod);
    }
}
