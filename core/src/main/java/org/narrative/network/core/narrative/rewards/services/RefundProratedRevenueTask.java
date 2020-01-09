package org.narrative.network.core.narrative.rewards.services;

import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.WalletType;
import org.narrative.network.core.narrative.wallet.services.ProcessWalletTransactionTask;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.invoices.InvoicePaymentBase;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import javax.persistence.LockModeType;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-31
 * Time: 14:19
 *
 * @author brian
 */
public class RefundProratedRevenueTask extends AreaTaskImpl<WalletTransaction> {
    private final InvoicePaymentBase invoicePayment;
    private final WalletTransactionType refundTransactionType;

    public RefundProratedRevenueTask(InvoicePaymentBase invoicePayment, WalletTransactionType refundTransactionType) {
        assert refundTransactionType.getFromWalletType().isProratedMonthRevenue() : "Refunds should always be from ProratedMonthRevenue wallets! type/" + refundTransactionType;
        this.invoicePayment = invoicePayment;
        this.refundTransactionType = refundTransactionType;
    }

    @Override
    protected WalletTransaction doMonitoredTask() {
        WalletTransaction originalPaymentTransaction = invoicePayment.getPaymentWalletTransaction();

        // bl: the refund should be for whatever the remaining balance is based on the number of captures.
        // first, let's identify the ProratedMonthRevenue corresponding to the original payment transaction
        // bl: lock the ProratedMonthRevenue so that we can adjust the total nrve amount on it
        ProratedMonthRevenue proratedMonthRevenue = ProratedMonthRevenue.dao().getForWallet(originalPaymentTransaction.getToWallet(), LockModeType.PESSIMISTIC_WRITE);

        // jw: use this utility method to calculate the refund amount. There are cases where we need to report this in
        //     UIs, which is why it is split out.
        NrveValue refundAmount = calculateRefundAmount(invoicePayment, proratedMonthRevenue);

        // bl: opting to record 0-value refunds here. prefer it for transactional purposes
        WalletType toWalletType = refundTransactionType.getToWalletType();
        assert toWalletType==null || toWalletType.isUser() : "Refunds should always be to null or a user wallet! type/" + refundTransactionType;
        Wallet toWallet = toWalletType==null ? null : invoicePayment.getInvoice().getUser().getWallet();
        WalletTransaction refundTransaction = getAreaContext().doAreaTask(new ProcessWalletTransactionTask(originalPaymentTransaction.getToWallet(), toWallet, refundTransactionType, refundAmount));

        // finally, deduct the original transaction amount from the ProratedMonthRevenue so that future
        // captures of this month will be correct
        // bl: if the month this FiatPayment is for hasn't been processed/completed yet, then ProratedMonthRevenue.totalNrve has not been set yet.
        // in that case, we don't need to touch totalNrve. it should stay at zero until the month is over and we apply the fiat adjustment.
        // we can identify whether the month is over or not based on whether there are any captures. if the number of captures
        // is 0, then the month hasn't been processed yet.
        // unlikely to happen, but should do it for completeness and accuracy!
        if(proratedMonthRevenue.getCaptures()>0) {
            proratedMonthRevenue.setTotalNrve(proratedMonthRevenue.getTotalNrve().subtract(originalPaymentTransaction.getNrveAmount()));
        }
        return refundTransaction;
    }

    public static NrveValue calculateRefundAmount(InvoicePaymentBase invoicePayment, ProratedMonthRevenue proratedMonthRevenue) {
        WalletTransaction originalPaymentTransaction = invoicePayment.getPaymentWalletTransaction();
        assert originalPaymentTransaction.getFromWallet()==null : "Invoice payment transactions currently should always be from null!";
        assert originalPaymentTransaction.getToWallet().getType().isProratedMonthRevenue() : "Invoice payment transactions currently should always be to a prorated month revenue wallet!";
        assert exists(proratedMonthRevenue) && isEqual(proratedMonthRevenue.getWallet(), originalPaymentTransaction.getToWallet()) : "The provided month revenue should have the same wallet as the transaction!";

        // next, calculate the total disbursed to date based on the current number of captures.
        // note that we use calculateNrveShare so that we get the total for the number of captures that have
        // been completed. this way, when captures==12, the amountDisbursed should equal the amount of the original transaction.
        NrveValue amountDisbursed = RewardUtils.calculateNrveShare(originalPaymentTransaction.getNrveAmount(), proratedMonthRevenue.getCaptures(), RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR);
        // now that we know how much has been disbursed, we can subtract to identify the amount to refund
        return originalPaymentTransaction.getNrveAmount().subtract(amountDisbursed);
    }
}
