package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.services.ProcessWalletTransactionTask;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoicePaymentBase;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-20
 * Time: 07:45
 *
 * @author jonmark
 */
public class CreateWalletTransactionFromPaidInvoiceTask extends CreateWalletTransactionFromPaidInvoiceTaskBase {
    private final ProratedRevenueType revenueType;
    private final InvoicePaymentBase payment;

    public CreateWalletTransactionFromPaidInvoiceTask(ProratedRevenueType revenueType, Invoice invoice) {
        super(invoice);

        this.revenueType = revenueType;
        this.payment = invoice.getPurchasePaymentResolved();

        assert exists(payment) : "The invoice"+getLoggingDetails()+" should always have a paid payment by the time this is invoked!";
    }

    @Override
    protected WalletTransaction doMonitoredTask() {
        // jw: the first thing we need to do is to fetch the RewardPeriod for the YearMonth for this payment.
        ProratedMonthRevenue revenue = ProratedMonthRevenue.dao().getForYearMonthAndType(RewardUtils.calculateYearMonth(payment.getTransactionDate()), revenueType);

        if (!exists(revenue)) {
            throw UnexpectedError.getRuntimeException("We should always be able to find a revenue pool from the transactionDate of the payment/"+payment.getOid()+getLoggingDetails());
        }

        // jw: this is not technically necessary, but I want to do it anyways to be extra safe!
        RewardPeriod period = RewardPeriod.dao().getForYearMonth(revenue.getMonth());

        if (!exists(period)) {
            throw UnexpectedError.getRuntimeException("We should always be able to find a RewardPeriod that corresponds to the same time as the revenue pool. payment/"+payment.getOid()+getLoggingDetails());
        }

        if (period.isCompleted()) {
            throw UnexpectedError.getRuntimeException("The reward period corresponding to the revenue pool should still be open when we process a payment/"+payment.getOid()+getLoggingDetails());
        }

        return getAreaContext().doAreaTask(new ProcessWalletTransactionTask(
                // jw: at some point we might consider allowing users to pay from their personal wallet, in which case we might specify that here.
                null,
                // jw: pay the NRVE into the wallet for the ProratedMonthRevenue wallet for Payments.
                revenue.getWallet(),
                revenueType.getPaymentTransactionType(),
                // jw: allow the payment to determine which wallet transaction status is most appropriate (needed for pending fiat payment)
                payment.getInitialWalletTransactionStatus(),
                payment.getNrveAmount()
        ));
    }

    private String getLoggingDetails() {
        return "/"+invoice.getType()+"/"+invoice.getOid();
    }
}
