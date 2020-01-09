package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoicePaymentBase;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/2/18
 * Time: 10:27 AM
 */
public class SendPaymentReceivedEmail extends SendSingleNarrativeEmailTaskBase {
    private InvoicePaymentBase payment;

    public SendPaymentReceivedEmail(InvoicePaymentBase payment) {
        super(payment.getInvoice().getUser());
        assert payment.hasBeenPaid() : "Should only ever call this for a payment that has been paid! p/" + payment.getOid();

        this.payment = payment;
    }

    @Override
    public boolean isAlwaysSendEmail() {
        return true;
    }

    public boolean isFiatPayment() {
        return isOfType(payment, FiatPayment.class);
    }

    public InvoicePaymentBase getPayment() {
        return payment;
    }

    public NicheAuctionSecurityDeposit getSecurityDeposit() {
        if (!payment.getInvoice().getType().isNicheAuction()) {
            return null;
        }

        Invoice invoice = payment.getInvoice();
        NicheAuctionInvoice auctionInvoice = invoice.getInvoiceConsumer();

        // jw: since this should only be called once in the JSP we can just make the request directly.
        return NicheAuctionSecurityDeposit.dao().getSecurityDeposit(auctionInvoice.getAuction(), invoice.getUser());
    }
}
