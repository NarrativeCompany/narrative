package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;
import static org.quartz.TriggerBuilder.*;

/**
 * Date: 2019-07-09
 * Time: 13:25
 *
 * @author brian
 */
public class SendRewardNotificationEmailsJob extends NetworkJob {
    private static final NetworkLogger logger = new NetworkLogger(SendRewardNotificationEmailsJob.class);

    private static final String REWARD_PERIOD_OID = "rewardPeriodOid";

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        OID rewardOid = getOidFromContext(context, REWARD_PERIOD_OID);

        assert rewardOid != null : "Should always have a rewardOid";

        RewardPeriod period = RewardPeriod.dao().get(rewardOid);

        Wallet rewardPeriodWallet = period.getWallet();
        Map<OID, NrveValue> userOidToReward = WalletTransaction.dao().getUserTransactionSumsFromWallet(rewardPeriodWallet);
        if(logger.isInfoEnabled()) logger.info("Sending reward notification emails to " + userOidToReward.size() + " users.");
        getNetworkContext().doAreaTask(Area.dao().getNarrativePlatformArea(), new SendBulkRewardNotificationEmails(period, userOidToReward));
    }

    public static void schedule(RewardPeriod period) {
        assert exists(period) : "The period should always be provided!";
        assert period.isCompleted() : "The period should already be completed!";

        JobBuilder builder = QuartzJobScheduler.createRecoverableJobBuilder(SendRewardNotificationEmailsJob.class, "-" + period.getPeriod())
                .usingJobData(REWARD_PERIOD_OID, period.getOid().getValue())
                ;

        // bl: just schedule the job to run now
        QuartzJobScheduler.GLOBAL.schedule(builder, newTrigger().startNow());
    }
}
