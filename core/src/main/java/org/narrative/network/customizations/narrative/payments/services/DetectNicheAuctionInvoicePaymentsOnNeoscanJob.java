package org.narrative.network.customizations.narrative.payments.services;

import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

/**
 * Date: 11/19/18
 * Time: 8:02 AM
 *
 * @author brian
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class DetectNicheAuctionInvoicePaymentsOnNeoscanJob extends DetectInvoicePaymentsOnNeoscanBaseJob {
    @Override
    protected String getPaymentNeoAddress() {
        return NeoWallet.dao().getNichePaymentNeoAddress();
    }

    @Override
    protected String getEmailNotificationBody(NrvePayment payment) {
        assert payment.getInvoice().getType().isNicheAuction() : "Should never get a payment to the Niche address for anything other than a NicheAuctionInvoice! found/" + payment.getInvoice().getType();
        NicheAuctionInvoice auctionInvoice = payment.getInvoice().getInvoiceConsumer();
        Niche niche = auctionInvoice.getAuction().getNiche();
        return "New Neoscan payment detected for niche/" + niche.getOid() + " " + niche.getDisplayUrl(true) + " - " + payment.getInvoice().getNrveAmount().getFormattedWithSuffix() + " sent from " + payment.getFromNeoAddress() + ". Due by " + payment.getInvoice().getPaymentDueDatetime();
    }
}
