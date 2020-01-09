package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceConsumer;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.ProcessInvoiceExpiringJob;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-08
 * Time: 16:19
 *
 * @author jonmark
 */
public abstract class HandlePaidChannelInvoiceTaskBase<IC extends InvoiceConsumer, CC extends ChannelConsumer> extends HandlePaidInvoiceTaskBase {
    protected HandlePaidChannelInvoiceTaskBase(InvoiceType expectedInvoiceType, Invoice invoice) {
        super(expectedInvoiceType, invoice);
    }

    protected abstract CC getChannelConsumer(IC invoiceConsumer);
    protected abstract void handlePaidInvoice(IC invoiceConsumer, CC channelConsumer);

    @Override
    protected Object doMonitoredTask() {
        IC invoiceConsumer = invoice.getInvoiceConsumer();
        assert exists(invoiceConsumer) : "Should always be able to get a invoiceConsumer from an invoice!";

        CC channelConsumer = getChannelConsumer(invoiceConsumer);
        assert exists(channelConsumer) : "Should always be able to get a channelConsumer from an invoice when using this task!";

        handlePaidInvoice(invoiceConsumer, channelConsumer);

        // jw: associate the paid invoice with the channel.
        channelConsumer.getChannel().setPurchaseInvoice(invoice);

        // jw: unschedule the process invoice expiring job to ensure if one is scheduled it will not be sent.
        ProcessInvoiceExpiringJob.unschedule(invoice);

        return null;
    }
}
