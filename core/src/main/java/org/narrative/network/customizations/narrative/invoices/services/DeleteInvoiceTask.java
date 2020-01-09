package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceConsumer;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-06
 * Time: 10:46
 *
 * @author jonmark
 */
public class DeleteInvoiceTask extends AreaTaskImpl<Boolean> {
    private final Invoice invoice;


    public DeleteInvoiceTask(Invoice invoice) {
        assert exists(invoice) : "Should always have an invoice!";

        this.invoice = invoice;
    }

    @Override
    protected Boolean doMonitoredTask() {
        invoice.lockForProcessing();

        // jw: if there was a race condition and the invoice is paid then we need to short out and return the invoice so
        //     the caller knows what happened.
        if (invoice.getStatus().isPaid()) {
            return false;
        }

        // jw: I was getting order of operation issues with the cascade deletions from invoice to these payments, so I am
        //     going to do it manually and ensure the order is correct.

        // jw: first, we need to delete any payment objects that might exist.
        if (exists(invoice.getNrvePayment())) {
            NrvePayment payment = invoice.getNrvePayment();
            invoice.setNrvePayment(null);

            // jw: to ensure we do not get a order of operations issue when this flushes we need to force a flush so that
            //     the invoice no longer references the payment when it is deleted.
            PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

            NrvePayment.dao().delete(payment);
        }

        if (exists(invoice.getFiatPayment())) {
            FiatPayment payment = invoice.getFiatPayment();
            invoice.setFiatPayment(null);

            // jw: to ensure we do not get a order of operations issue when this flushes we need to force a flush so that
            //     the invoice no longer references the payment when it is deleted.
            PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

            FiatPayment.dao().delete(payment);
        }

        // jw: let's do one more flush now to ensure that the above changes were all committed in the proper order.
        PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

        InvoiceConsumer invoiceConsumer = invoice.getInvoiceConsumer();
        // jw: if we have a invoice consumer, we need to delete that first since it will have a reference to Invoice. This
        //     means that
        if (exists(invoiceConsumer)) {
            invoice.getType().deleteInvoiceConsumer(invoiceConsumer);

            // jw: just like a above we need to flush to ensure that the SQL is written in the proper order.
            PartitionGroup.getCurrentPartitionGroup().flushAllSessions();
        }

        // jw: it was a long road, but we are finally capable of deleting this silly invoice!
        Invoice.dao().delete(invoice);

        return true;
    }
}
