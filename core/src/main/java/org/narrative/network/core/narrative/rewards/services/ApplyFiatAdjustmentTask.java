package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.wallet.NeoTransaction;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-20
 * Time: 18:56
 *
 * @author jonmark
 */
public class ApplyFiatAdjustmentTask extends AreaTaskImpl<Object> {
    private static final NetworkLogger logger = new NetworkLogger(ApplyFiatAdjustmentTask.class);

    private final ProratedMonthRevenue revenue;
    private final NrveValue adjustment;

    public ApplyFiatAdjustmentTask(ProratedMonthRevenue revenue, NrveValue adjustment) {
        assert exists(revenue) : "ProratedMonthRevenue must be provided!";
        assert adjustment != null : "Adjustment must be provided, even ZERO is good.";

        this.revenue = revenue;
        this.adjustment = adjustment;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: assert that the revenue month is in the past!
        if(!RewardUtils.nowYearMonth().isAfter(revenue.getMonth())) {
            throw UnexpectedError.getRuntimeException("Should only ever run ApplyFiatAdjustmentTask after the end of the previous month! period/" + revenue.getMonth());
        }

        // bl: let's lock the ProratedMonthRevenue up front to make sure it doesn't change out from underneath us.
        // we are going to need to set the totalNrve here.
        ProratedMonthRevenue.dao().refreshForLock(revenue);

        if(revenue.getCaptures()>0) {
            throw UnexpectedError.getRuntimeException("Should never apply fiat adjustment after captures have been made! period/" + revenue.getMonth());
        }

        // jw: before we do anything, let's lock the wallet.
        // bl: we need to lock the wallet after we lock ProratedMonthRevenue. this is because refreshing ProratedMonthRevenue
        // also cascades to trigger a refresh of Wallet. if we do that after locking the Wallet, the LockMode on the
        // Wallet will be downgraded from PESSIMISTIC_WRITE to OPTIMISTIC, which will then fail our assertions
        // that the wallet must be locked when doing balance updates (even though the object really is locked).
        Wallet revenueWallet = revenue.getWallet();
        Wallet.dao().refreshForLock(revenueWallet);

        // bl: before we do anything, let's get the total NRVE amount for the subsequent NeoTransactions.
        // we can cheat by just adding up the transactions that are COMPLETED as the NRVE payments
        // and use those PENDING_FIAT_ADJUSTMENT as the fiat payments
        Map<WalletTransactionStatus,NrveValue> statusToAmount = WalletTransaction.dao().getTransactionSumByToWalletType(revenueWallet, revenue.getType().getPaymentTransactionType());

        // bl: if there isn't an adjustment, then we have no reason to look up pending transactions to adjust.
        List<WalletTransaction> pendingTransactions = NrveValue.ZERO.equals(adjustment) ? null : WalletTransaction.dao().getForToWalletAndStatus(revenueWallet, WalletTransactionStatus.PENDING_FIAT_ADJUSTMENT);

        if (!isEmptyOrNull(pendingTransactions)) {
            if(logger.isDebugEnabled()) logger.debug("Applying fiat adjustment of " + adjustment + " NRVE to " + pendingTransactions.size() + " transactions. Wallet/" + revenueWallet);

            NrveValue originalBalance = revenueWallet.getBalance();

            RewardUtils.distributeNrveProportionally(
                    adjustment,
                    pendingTransactions,
                    WalletTransaction::getNrveAmount,
                    WalletTransaction::adjustFiatValue
            );

            // jw: ensure that we adjusted the balance in the wallet by the amount we expected.
            if (revenueWallet.getBalance().subtract(originalBalance).compareTo(adjustment) != 0) {
                throw UnexpectedError.getRuntimeException("The difference between the wallet when we started and when we finished should be exactly the same as the adjustment we were asked to make! originalBalance/" + originalBalance + " newBalance/" + revenueWallet.getBalance() + " adjustment/" + adjustment);
            }

            // bl: apply the adjustment to the status value
            statusToAmount.replace(WalletTransactionStatus.PENDING_FIAT_ADJUSTMENT, statusToAmount.get(WalletTransactionStatus.PENDING_FIAT_ADJUSTMENT).add(adjustment));
        } else {
            assert NrveValue.ZERO.equals(adjustment) : "We should never be given an adjustment when we don't have any pending fiat adjustment transactions to process.";
        }

        // bl: now that we're done and we know the final balance, set the totalNrve for the month!
        revenue.setTotalNrve(revenueWallet.getBalance());

        // bl: if the final balance is 0, then there's no need to ever capture from it, so let's set total captures to
        // 12 to indicate no further processing is required!
        if(NrveValue.ZERO.equals(revenue.getTotalNrve())) {
            revenue.setCaptures(RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR);
        }

        // bl: now that we know the total NRVE for the month, create the NeoTransactions for transferring the revenue
        // to the ProratedMonthRevenue wallet on the blockchain
        for (WalletTransactionStatus status : WalletTransactionStatus.FIAT_ADJUSTMENT_STATUSES) {
            NrveValue expectedAmount = statusToAmount.getOrDefault(status, NrveValue.ZERO);
            if(status.isPendingFiatAdjustment()) {
                NeoTransaction neoTransaction = revenue.recordFiatPaymentNeoTransaction();
                // bl: verify that our expectedAmount is equivalent to the FiatPayment total calculated!
                if(!neoTransaction.getNrveAmount().equals(expectedAmount)) {
                    throw UnexpectedError.getRuntimeException("Value mismatch for " + revenue.getType() + " fiat transfer! expected/" + expectedAmount + " fiatPaymentTotal/" + neoTransaction.getNrveAmount());
                }
            } else {
                assert status.isCompleted() : "Found an unsupported transaction status/" + status;
                NeoTransaction neoTransaction = revenue.recordNrvePaymentNeoTransaction();
                // bl: verify that our expectedAmount is equivalent to the NrvePayment total calculated!
                if(!neoTransaction.getNrveAmount().equals(expectedAmount)) {
                    throw UnexpectedError.getRuntimeException("Value mismatch for " + revenue.getType() + " NRVE transfer! expected/" + expectedAmount + " nrvePaymentTotal/" + neoTransaction.getNrveAmount());
                }
            }
        }

        // we have to adjust our expected amount by any refunds due for this month when doing the final sanity check
        Map<Wallet,NrveValue> walletToRefundTotal = WalletTransaction.dao().getRefundTransactionSumsByFromWalletInRange(revenue);
        NrveValue totalRefundsForCurrentMonth = walletToRefundTotal.getOrDefault(revenueWallet, NrveValue.ZERO);

        NrveValue totalByStatus = statusToAmount.values().stream().reduce(NrveValue.ZERO, NrveValue::add);
        if(!revenue.getTotalNrve().equals(totalByStatus.subtract(totalRefundsForCurrentMonth))) {
            throw UnexpectedError.getRuntimeException("Total value mismatch after applying fiat adjustment! expected/" + revenue.getTotalNrve() + " totalByStatus/" + totalByStatus + " totalRefundsForCurrentMonth/" + totalRefundsForCurrentMonth);
        }

        return null;
    }
}
