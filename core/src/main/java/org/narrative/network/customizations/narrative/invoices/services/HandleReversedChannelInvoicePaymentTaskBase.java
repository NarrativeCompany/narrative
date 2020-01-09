package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.services.RefundProratedRevenueTask;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.services.ProcessWalletTransactionTask;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;

import java.time.Instant;
import java.time.YearMonth;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-08
 * Time: 14:20
 *
 * @author jonmark
 */
public abstract class HandleReversedChannelInvoicePaymentTaskBase extends HandleReversedInvoicePaymentTaskBase {

    protected abstract WalletTransactionType getFiatPaymentReversalTransactionType();
    protected abstract void processReversal();

    protected HandleReversedChannelInvoicePaymentTaskBase(InvoiceType expectedInvoiceType, FiatPayment fiatPayment, boolean forChargeback, InvoiceStatus originalInvoiceStatus) {
        super(expectedInvoiceType, fiatPayment, forChargeback, originalInvoiceStatus);
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: use the invoice's updateDatetime for all time tracking purposes
        Instant updateDatetime = Instant.ofEpochMilli(fiatPayment.getInvoice().getUpdateDatetime().getTime());

        WalletTransaction reversalTransaction;

        // bl: if the invoice status is currently paid or pending refund, then we can reset the consumer. if the consumer
        // had been rejected previously, it could be pending a prorated refunded.
        // note that we have different handling below if the refund has already been processed.
        if (originalInvoiceStatus.isPaid() || originalInvoiceStatus.isPendingProratedRefund()) {
            processReversal();

            // pull the NRVE out of ProratedMonthRevenue to null for a refunded consumer
            reversalTransaction = getAreaContext().doAreaTask(new RefundProratedRevenueTask(fiatPayment, getFiatPaymentReversalTransactionType()));
        } else {
            assert originalInvoiceStatus.isRefundedProrated() : "Should always be refunded prorated status here! Is there a new status in InvoiceStatus.isHasBeenPaid()? status/" + originalInvoiceStatus;
            // bl: if we have a chargeback/refund for a payment that has already received a prorated refund,
            // then it's for a previously rejected consumer. in this case, we don't need to run the reversed fiat payment
            // handler since the consumer should have already been rejected and gone up for sale.
            // instead, we just need to try to reclaim the refund (back into the RewardPeriod wallet for the current month)
            WalletTransaction refundTransaction = fiatPayment.getRefundWalletTransaction();
            if(!exists(refundTransaction)) {
                throw UnexpectedError.getRuntimeException("Should never find an Invoice that has a prorated refund, but no refund transaction! payment/" + fiatPayment.getOid());
            }
            NrveValue reclaimAmount = refundTransaction.getNrveAmount();
            Wallet userWallet = refundTransaction.getToWallet();
            // bl: lock the wallet so that we can get a correct balance
            Wallet.dao().refreshForLock(userWallet);
            // bl: can't reclaim more than the user has in the available balance.
            // any difference here will be absorbed by the rewards pool
            reclaimAmount = new NrveValue(Math.max(reclaimAmount.toNeurons(), userWallet.getBalance().toNeurons()));
            // finally, record the transaction into the current RewardPeriod.
            YearMonth yearMonth = RewardUtils.calculateYearMonth(updateDatetime);
            RewardPeriod rewardPeriod = RewardPeriod.dao().getForYearMonth(yearMonth);
            reversalTransaction = getAreaContext().doAreaTask(new ProcessWalletTransactionTask(userWallet, rewardPeriod.getWallet(), WalletTransactionType.REFUND_REVERSAL, reclaimAmount));
        }

        // bl: in order to have audit accounting, we should also track the fiat payment reversal transaction
        // on the FiatPayment so we have record of it.
        fiatPayment.setReversalWalletTransaction(reversalTransaction);

        return null;
    }
}
