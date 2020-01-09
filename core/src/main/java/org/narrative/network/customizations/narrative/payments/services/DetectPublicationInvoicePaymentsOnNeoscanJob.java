package org.narrative.network.customizations.narrative.payments.services;

import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationInvoice;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

/**
 * Date: 10/4/19
 * Time: 9:33 AM
 *
 * @author brian
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class DetectPublicationInvoicePaymentsOnNeoscanJob extends DetectInvoicePaymentsOnNeoscanBaseJob {
    @Override
    protected String getPaymentNeoAddress() {
        return NeoWallet.dao().getPublicationPaymentNeoAddress();
    }

    @Override
    protected String getEmailNotificationBody(NrvePayment payment) {
        assert payment.getInvoice().getType().isPublicationAnnualFee() : "Should never get a payment to the Publication address for anything other than a Publication annual fee! found/" + payment.getInvoice().getType();
        PublicationInvoice publicationInvoice = payment.getInvoice().getInvoiceConsumer();
        Publication publication = publicationInvoice.getPublication();
        return "New Neoscan payment detected for publication/" + publication.getOid() + " " + publication.getDisplayUrl() + " - " + payment.getInvoice().getNrveAmount().getFormattedWithSuffix() + " sent from " + payment.getFromNeoAddress() + ". Due by " + payment.getInvoice().getPaymentDueDatetime();
    }
}
