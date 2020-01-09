package org.narrative.network.core.narrative.rewards.services.steps;

import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardPeriodStep;
import org.narrative.network.core.narrative.rewards.services.TransferProratedRevenueTask;
import org.narrative.network.shared.util.NetworkLogger;

import javax.persistence.LockModeType;

/**
 * Date: 2019-05-22
 * Time: 15:00
 *
 * @author jonmark
 */
public class TransferProratedRevenueStepProcessor extends RewardPeriodStepProcessorBase {
    private static final NetworkLogger logger = new NetworkLogger(TransferProratedRevenueStepProcessor.class);

    public TransferProratedRevenueStepProcessor(RewardPeriod period) {
        super(period, RewardPeriodStep.TRANSFER_PRORATED_REVENUE);
    }

    @Override
    public NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: while this is more complex than TransferMintedTokensStepProcessor it is not by much. We just need to fetch
        //     all ProratedMonthRevenue objects with open captures and process them against our period. The internals of
        //     the task will ensure that we have not already done a capture against them.
        // bl: the query will fetch them in order, oldest first
        // bl: since we are going to be using the totalNrve amounts to determine prorated distributions,
        // let's use a lock on these rows so that we are guaranteed they won't change out from under us (e.g. for refunds)
        for (ProratedMonthRevenue revenue : ProratedMonthRevenue.dao().getAllWithAvailableCaptures(period, LockModeType.PESSIMISTIC_WRITE)) {
            getAreaContext().doAreaTask(new TransferProratedRevenueTask(revenue, period));
        }

        return null;
    }
}
