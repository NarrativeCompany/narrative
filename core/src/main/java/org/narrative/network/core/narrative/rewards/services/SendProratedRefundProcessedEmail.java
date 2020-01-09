package org.narrative.network.core.narrative.rewards.services;

import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;

/**
 * Date: 2019-06-01
 * Time: 09:54
 *
 * @author brian
 */
public class SendProratedRefundProcessedEmail extends SendSingleNarrativeEmailTaskBase {
    private final Invoice invoice;

    public SendProratedRefundProcessedEmail(Invoice invoice) {
        super(invoice.getUser());
        this.invoice = invoice;
    }

    @Override
    public boolean isAlwaysSendEmail() {
        return true;
    }

    public Invoice getInvoice() {
        return invoice;
    }
}
