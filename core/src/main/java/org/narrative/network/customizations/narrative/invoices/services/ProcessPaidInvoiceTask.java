package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoicePaymentBase;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.ProcessInvoiceExpiringJob;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.SendPaymentReceivedEmail;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-04
 * Time: 14:57
 *
 * @author jonmark
 */
public class ProcessPaidInvoiceTask extends AreaTaskImpl<Invoice> {
    private final Invoice invoice;

    public ProcessPaidInvoiceTask(Invoice invoice) {
        this.invoice = invoice;
    }

    @Override
    protected Invoice doMonitoredTask() {
        // jw: first things first, lets lock on the various objects to ensure we are the only ones doing anything with them.
        // bl: note that we probably have already sufficiently locked the object, but this won't hurt. every instance
        // that currently calls this task either has already locked the Invoice or has just inserted the invoice
        // (in ProcessImmediateFiatPaymentTask).
        invoice.lockForProcessing();

        NrvePayment nrvePayment = invoice.getFreshNrvePayment();

        if (exists(nrvePayment)) {
            assert nrvePayment.hasBeenPaid() : "The associated payment should always be paid at this point! invoice/" + invoice.getOid();

            // jw: first, lets clear the paymentStatus since this is now paid!
            nrvePayment.setPaymentStatus(null);

            // jw: if there was a fiatPayment then let's clear it.
            FiatPayment fiatPayment = invoice.getFiatPayment();
            if (exists(fiatPayment)) {
                invoice.setFiatPayment(null);
                FiatPayment.dao().delete(fiatPayment);
            }

        } else {
            FiatPayment fiatPayment = invoice.getFiatPayment();
            assert exists(fiatPayment) : "If we did not have a regular payment, we should always have a fiatPayment! invoice/" + invoice.getOid();
            assert fiatPayment.hasBeenPaid() : "The fiat payment should have been paid before this was called! invoice/" + invoice.getOid();
        }

        // jw: let's flag the invoice as having been paid
        invoice.updateStatus(InvoiceStatus.PAID);

        //mk: unschedule invoice payment reminder for this invoice
        ProcessInvoiceExpiringJob.unschedule(invoice);

        // jw: before we send the email to the payer, let's do the type specific processing in case a refund needs to be
        //     issued for any deposits that might have been made.
        HandlePaidInvoiceTaskBase paidInvoiceHandler = invoice.getType().getPaidInvoiceHandler(invoice);

        // jw: not all invoice types need to do anything fancy/special when the invoice is paid.
        if (paidInvoiceHandler!=null) {
            getAreaContext().doAreaTask(paidInvoiceHandler);
        }

        // jw: We need to give the InvoiceType a chance to post process the invoice and provide a WalletTransaction to associate
        //     to the payment. This will ease the load on the InvoiceType and allow it to focus on just the WalletTransaction
        //     if necessary.
        InvoicePaymentBase payment = invoice.getPurchasePaymentResolved();
        CreateWalletTransactionFromPaidInvoiceTaskBase walletTransactionTask = invoice.getType().getCreateWalletTransactionTask(invoice);
        if (walletTransactionTask!=null) {
            WalletTransaction transaction = getAreaContext().doAreaTask(walletTransactionTask);
            assert exists(transaction) : "We should always have a transaction if the InvoiceType provided a transaction creation task.";

            // jw: Associate the transaction to the payment so that we can do post processing with relational integrity.
            payment.setPaymentWalletTransaction(transaction);
        }

        // jw: finally, let's send the receipt email
        getAreaContext().doAreaTask(new SendPaymentReceivedEmail(payment));

        return null;
    }
}
