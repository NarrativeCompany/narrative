package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.services.ProcessWalletTransactionTask;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-21
 * Time: 14:11
 *
 * @author jonmark
 */
public class TransferProratedRevenueTask extends AreaTaskImpl<Object> {
    private final ProratedMonthRevenue revenue;
    private final RewardPeriod period;

    public TransferProratedRevenueTask(ProratedMonthRevenue revenue, RewardPeriod period) {
        super(true);
        assert exists(revenue) : "Should always be given a revenue object!";
        assert exists(period) : "Should always be given a revenue object!";

        this.revenue = revenue;
        this.period = period;
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: first, the number of captures should always be the same as the number of months ago this revenue was created from the period we are processing against.
        if (revenue.getMonth().plusMonths(revenue.getCaptures()).compareTo(period.getPeriod()) != 0) {
            throw UnexpectedError.getRuntimeException("The RewardPeriod/"+period.getOid()+" performing a capture should always have a YearMonth the same number of months ahead of the ProratedMonthRevenue/"+revenue.getOid()+" as the number of previous captures there have been.");
        }

        // jw: let's ensure that there have not been any transfers between these wallets already.
        List<WalletTransaction> previousTransactions = WalletTransaction.dao().getForWalletsAndType(
                revenue.getWallet(),
                period.getWallet(),
                revenue.getType().getRevenueTransactionType()
        );
        if (!previousTransactions.isEmpty()) {
            // jw: we need to carve out an exception here for the April 2019 transaction against May 2019
            if (!period.getPeriod().equals(RewardUtils.FIRST_ACTIVE_YEAR_MONTH) || previousTransactions.size() > 1) {
                throw UnexpectedError.getRuntimeException("We should never have already processed a transfer from ProratedMonthRevenue/"+revenue.getOid()+" against RewardPeriod/"+period.getOid());
            }
        }

        // bl: the revenue for the month should never be zero!
        if(NrveValue.ZERO.equals(revenue.getTotalNrve())) {
            throw UnexpectedError.getRuntimeException("Should never have a ProratedMonthRevenue with a balance of 0 that has remaining captures! revenue/" + revenue.getOid() + " period/" + period.getPeriod());
        }

        revenue.setCaptures(revenue.getCaptures() + 1);

        // jw: now that we have incremented the capture count, let's get the capture value
        NrveValue captureValue = revenue.calculateCurrentCaptureValue();

        // jw: finally, let's transfer the funds between Wallets
        getAreaContext().doAreaTask(new ProcessWalletTransactionTask(
                revenue.getWallet(),
                period.getWallet(),
                revenue.getType().getRevenueTransactionType(),
                captureValue
        ));

        return null;
    }
}
