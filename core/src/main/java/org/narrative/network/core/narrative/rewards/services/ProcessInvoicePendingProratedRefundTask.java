package org.narrative.network.core.narrative.rewards.services;

import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoicePaymentBase;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Date: 2019-05-31
 * Time: 16:38
 *
 * @author brian
 */
public class ProcessInvoicePendingProratedRefundTask extends AreaTaskImpl<Object> {
    private final Invoice invoice;

    public ProcessInvoicePendingProratedRefundTask(Invoice invoice) {
        assert invoice.getStatus().isPendingProratedRefund() : "Should only call this task for invoices pending prorated refunds! not status/" + invoice.getStatus();
        this.invoice = invoice;
    }

    @Override
    protected Object doMonitoredTask() {
        InvoicePaymentBase invoicePayment = invoice.getPurchasePaymentResolved();

        // bl: process the refund
        WalletTransaction refundTransaction = getAreaContext().doAreaTask(new RefundProratedRevenueTask(invoicePayment, invoice.getType().getRefundTransactionType()));

        // bl: set the refund transaction on the original payment since the user got a refund.
        invoicePayment.setRefundWalletTransaction(refundTransaction);

        // now that the refund has been processed, we just need to update the invoice status to reflect it
        invoice.updateStatus(InvoiceStatus.REFUNDED_PRORATED);

        getAreaContext().doAreaTask(new SendProratedRefundProcessedEmail(invoice));

        return null;
    }
}
