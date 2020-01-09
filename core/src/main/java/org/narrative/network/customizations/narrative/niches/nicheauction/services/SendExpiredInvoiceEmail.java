package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/25/18
 * Time: 9:10 AM
 */
public class SendExpiredInvoiceEmail extends SendSingleNarrativeEmailTaskBase {
    private Invoice invoice;

    public SendExpiredInvoiceEmail(Invoice invoice) {
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

    public NicheAuctionSecurityDeposit getSecurityDeposit() {
        if (!invoice.getType().isNicheAuction()) {
            return null;
        }

        NicheAuctionInvoice auctionInvoice = invoice.getInvoiceConsumer();

        // jw: this should only be called once in the JSP, so should be safe to go to the db for it.
        return NicheAuctionSecurityDeposit.dao().getSecurityDeposit(auctionInvoice.getAuction(), invoice.getUser());
    }
}
