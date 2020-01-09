package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceConsumer;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-08
 * Time: 14:33
 *
 * @author jonmark
 */
public abstract class ResetChannelForReversedFiatPaymentTaskBase<IC extends InvoiceConsumer, CC extends ChannelConsumer> extends AreaTaskImpl<Object> {
    private static final NetworkLogger logger = new NetworkLogger(ResetChannelForReversedFiatPaymentTaskBase.class);

    protected final FiatPayment fiatPayment;

    protected ResetChannelForReversedFiatPaymentTaskBase(FiatPayment fiatPayment) {
        this.fiatPayment = fiatPayment;
    }

    protected abstract CC getChannelConsumer(IC invoiceConsumer);
    protected abstract void resetChannelConsumer(CC channelConsumer);

    @Override
    protected Object doMonitoredTask() {
        IC invoiceConsumer = fiatPayment.getInvoice().getInvoiceConsumer();
        CC channelConsumer = getChannelConsumer(invoiceConsumer);

        // jw: only process this if the payment is still the latest payment that paid for this channelConsumer.
        Invoice purchaseInvoice = channelConsumer.getChannel().getPurchaseInvoice();
        if (!exists(purchaseInvoice)) {
            return null;
        }
        if (!isEqual(fiatPayment, purchaseInvoice.getFiatPayment())) {
            return null;
        }

        // jw: Let's log out the details for this, just in case we need to track anything down.
        if (logger.isInfoEnabled()) {
            logger.info("A fiat payment for channel/" + channelConsumer.getOid() + "/type/" + channelConsumer.getChannel().getType() + " was disputed for transactionId/" + fiatPayment.getTransactionId() + " which is the active payment for channel.");
        }

        resetChannelConsumer(channelConsumer);

        return null;
    }
}
