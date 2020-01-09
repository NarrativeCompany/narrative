package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit;
import org.narrative.network.customizations.narrative.service.impl.invoice.FiatPaymentProcessorType;
import org.narrative.network.customizations.narrative.service.impl.invoice.ProcessImmediateFiatPaymentTask;

/**
 * Date: 2019-04-18
 * Time: 12:16
 *
 * @author jonmark
 */
public class ProcessNicheAuctionSecurityDepositFiatPaymentTask extends ProcessImmediateFiatPaymentTask {
    private final NicheAuction auction;

    public ProcessNicheAuctionSecurityDepositFiatPaymentTask(FiatPaymentProcessorType processorType, String paymentToken, NicheAuction auction) {
        super(processorType, InvoiceType.NICHE_AUCTION_SECURITY_DEPOSIT, paymentToken);
        this.auction = auction;
    }

    @Override
    protected void performConsumerProcessing(FiatPayment payment) {
        // jw: now that we have an invoice we can create our security deposit.
        NicheAuctionSecurityDeposit.dao().save(new NicheAuctionSecurityDeposit(payment.getInvoice(), auction));
    }
}
