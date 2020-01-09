package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-04
 * Time: 19:05
 *
 * @author jonmark
 */
public class CancelInvoiceTask extends AreaTaskImpl<Object> {
    private final Invoice invoice;

    public CancelInvoiceTask(Invoice invoice) {
        assert exists(invoice) : "Should always have an invoice!";

        this.invoice = invoice;
    }

    @Override
    protected Object doMonitoredTask() {
        invoice.lockForProcessing();

        // jw: if the invoice is not invoiced then short out, it has already been handled.
        if (!invoice.getStatus().isInvoiced()) {
            return null;
        }

        invoice.updateStatus(InvoiceStatus.CANCELED);

        NrvePayment nrvePayment = invoice.getFreshNrvePayment();
        if (exists(nrvePayment)) {
            assert !nrvePayment.hasBeenPaid() : "The lock up front should guarantee that any processing threads would have completed before we got here!";

            invoice.setNrvePayment(null);
            NrvePayment.dao().delete(nrvePayment);
        }

        FiatPayment fiatPayment = invoice.getFiatPayment();
        if (exists(fiatPayment)) {
            assert !fiatPayment.hasBeenPaid() : "The lock up front should guarantee that any processing threads would have completed before we got here!";
            assert fiatPayment.getStatus().isCalculated() : "Since the payment hasn't been made, we expect this invoice to be calculated.";

            invoice.setFiatPayment(null);
            FiatPayment.dao().delete(fiatPayment);
        }

        return null;
    }
}
