package org.narrative.network.core.narrative.rewards.services.steps;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardPeriodStep;
import org.narrative.network.core.narrative.rewards.services.SetupNextRewardPeriodTask;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.services.ProcessWalletTransactionTask;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.util.NetworkLogger;

import java.time.YearMonth;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-31
 * Time: 07:34
 *
 * @author brian
 */
public class RewardCleanupStepProcessor extends RewardPeriodStepProcessorBase {
    private static final NetworkLogger logger = new NetworkLogger(RewardCleanupStepProcessor.class);

    public RewardCleanupStepProcessor(RewardPeriod period) {
        super(period, RewardPeriodStep.CLEANUP);
    }

    @Override
    public NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: the RewardPeriod being processed is (usually) for the prior month. the RewardPeriod for the current
        // month should already exist. but, let's verify everything is in order as part of the process.
        // bl: note that on environments that get behind by multiple months, this should still month. we should
        // always have the next month RewardPeriod for the month following the RewardPeriod that is being processed.
        YearMonth nextMonth = period.getPeriod().plusMonths(1);
        RewardPeriod nextMonthsRewardPeriod = RewardPeriod.dao().getForYearMonth(nextMonth);
        if(!exists(nextMonthsRewardPeriod)) {
            throw UnexpectedError.getRuntimeException("Didn't find a RewardPeriod for the month after the month being processed! period/" + nextMonth);
        }

        // bl: we need to create a RewardPeriod for two months after the RewardPeriod being processed.
        YearMonth monthToAdd = period.getPeriod().plusMonths(2);
        {
            RewardPeriod existingRewardPeriod = RewardPeriod.dao().getForYearMonth(monthToAdd);
            if(exists(existingRewardPeriod)) {
                throw UnexpectedError.getRuntimeException("Found a RewardPeriod for two months after the month being processed! period/" + monthToAdd + " rewardPeriod/" + existingRewardPeriod.getOid());
            }
        }

        RewardPeriod rewardPeriod = getAreaContext().doAreaTask(new SetupNextRewardPeriodTask());
        if(!rewardPeriod.getPeriod().equals(monthToAdd)) {
            throw UnexpectedError.getRuntimeException("The RewardPeriod created wasn't for the expected month! actual/" + rewardPeriod.getPeriod() + " expected/" + monthToAdd);
        }

        // bl: finally, we need to carryover any remaining balance from the current RewardPeriod being processed
        // to the next month's RewardPeriod
        if(!period.getWallet().getBalance().equals(NrveValue.ZERO)) {
            if(logger.isInfoEnabled()) logger.info("Carrying over balance of " + period.getWallet().getBalance() + " NRVE from RewardPeriod/" + period.getPeriod() + " to " + nextMonth + ".");
            getAreaContext().doAreaTask(new ProcessWalletTransactionTask(period.getWallet(), nextMonthsRewardPeriod.getWallet(), WalletTransactionType.REWARD_PERIOD_CARRYOVER, period.getWallet().getBalance()));

            if(!period.getWallet().getBalance().equals(NrveValue.ZERO)) {
                throw UnexpectedError.getRuntimeException("Balance remaining in RewardPeriod wallet after carryover! wallet/" + period.getWallet().getOid() + " balance/" + period.getWallet().getBalance());
            }
        } else {
            if(logger.isInfoEnabled()) logger.info("No carryover balance from RewardPeriod/" + period.getPeriod() + " so skipping carryover transaction.");
        }

        // bl: new steps! record the MISCELLANEOUS_REVENUE NeoTransaction and the ALL_USERS_MONTH_CREDITS

        // first, do the MISCELLANEOUS_REVENUE, which includes DELETED_USER_ABANDONED_BALANCES and REFUND_REVERSAL
        period.recordMiscellaneousRevenueNeoTransaction();

        // next, let's do ALL_USERS_MONTH_CREDITS
        period.recordAllUsersMonthCreditsNeoTransaction();

        return null;
    }
}
