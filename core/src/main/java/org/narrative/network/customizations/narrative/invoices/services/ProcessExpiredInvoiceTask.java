package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.SendExpiredInvoiceEmail;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-04
 * Time: 16:55
 *
 * @author jonmark
 */
public class ProcessExpiredInvoiceTask extends AreaTaskImpl<Invoice> {
    private final Invoice invoice;

    public ProcessExpiredInvoiceTask(Invoice invoice) {
        this.invoice = invoice;
    }

    @Override
    protected Invoice doMonitoredTask() {
        // jw: before we do anything, let's lock for processing
        invoice.lockForProcessing();

        // jw: if the auction has a payment, we need to lock that as well
        NrvePayment nrvePayment = invoice.getFreshNrvePayment();
        FiatPayment fiatPayment = invoice.getFiatPayment();
        // jw: now that we have a locked payment, let's double check that if it was paid we process that payment.
        if (exists(nrvePayment)) {
            // jw: if a payment has been paid from the blockchain, then we may just be waiting to notice.
            if (nrvePayment.hasBeenPaid()) {
                // jw: since we are going to consider this paid via the direct nrve payment, let's delete the fiat payment to make things cleaner
                if (exists(fiatPayment)) {
                    invoice.setFiatPayment(null);
                    FiatPayment.dao().delete(fiatPayment);
                }

                // jw: since we have it locked, lets go ahead and process it if we can.
                if (nrvePayment.getPaymentStatus() != null) {
                    getAreaContext().doAreaTask(new ProcessPaidInvoiceTask(invoice));
                }

                return invoice;
            }
            // jw: since we are closing this auction bid we need to delete the payment as well.
            invoice.setNrvePayment(null);
            NrvePayment.dao().delete(nrvePayment);
        }

        // jw: need to process fiat payments
        if (exists(fiatPayment)) {
            if (fiatPayment.hasBeenPaid()) {
                // jw: unlike above, there should NEVER be anything to do here. The process that flags the payment
                //     as paid will run the payment received task, and since we are locking the same object there is
                //     nothing to do here.
                return invoice;
            }
            // jw: since this payment was not paid, let's just remove it.
            invoice.setFiatPayment(null);
            FiatPayment.dao().delete(fiatPayment);
        }

        if (invoice.getType().isDeleteExpiredInvoices()) {
            getAreaContext().doAreaTask(new DeleteInvoiceTask(invoice));

            return null;
        }

        // jw: finally, let's run the invoice type specific handler.
        AreaTaskImpl<?> expiredInvoiceHandler = invoice.getType().getExpiredInvoiceHandler(invoice);

        // jw: since this invoice is not deleted then a handler is required.
        if (expiredInvoiceHandler==null) {
            throw UnexpectedError.getRuntimeException("Any invoice that supports long term payment windows should provide a expiration handler. i/ " +invoice.getOid()+ " it/" + invoice.getType());
        }

        // jw: now, let's set the status of the invoice to Expired!
        invoice.updateStatus(InvoiceStatus.EXPIRED);

        // jw: Let's email the person who failed to pay and let them know that about their failure, and the consequences
        //     of that mistake.
        getAreaContext().doAreaTask(new SendExpiredInvoiceEmail(invoice));

        // jw: process the handler
        getAreaContext().doAreaTask(expiredInvoiceHandler);

        return invoice;
    }
}
