package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.services.ResetChannelForReversedFiatPaymentTaskBase;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationInvoice;

import java.time.Instant;

/**
 * Date: 2019-08-08
 * Time: 14:31
 *
 * @author jonmark
 */
public class ResetPublicationForReversedFiatPaymentTask extends ResetChannelForReversedFiatPaymentTaskBase<PublicationInvoice, Publication> {

    public ResetPublicationForReversedFiatPaymentTask(FiatPayment fiatPayment) {
        super(fiatPayment);
    }

    @Override
    protected Publication getChannelConsumer(PublicationInvoice publicationInvoice) {
        return publicationInvoice.getPublication();
    }

    @Override
    protected void resetChannelConsumer(Publication publication) {
        // jw: let's terminate the Publications current plan but give the owner the standard 30 days to renew before the publication is deleted.
        publication.setEndDatetime(Instant.now());

        // jw: since we will be emailing the owner about the reversed payment no need to send any expiration emails.
        ProcessPublicationExpiringJob.unschedule(publication);
    }
}
