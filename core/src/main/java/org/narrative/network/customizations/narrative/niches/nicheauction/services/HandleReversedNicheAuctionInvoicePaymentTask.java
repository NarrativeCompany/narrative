package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.invoices.services.HandleReversedChannelInvoicePaymentTaskBase;

/**
 * Date: 2019-02-04
 * Time: 15:11
 *
 * @author jonmark
 */
public class HandleReversedNicheAuctionInvoicePaymentTask extends HandleReversedChannelInvoicePaymentTaskBase {

    public HandleReversedNicheAuctionInvoicePaymentTask(FiatPayment fiatPayment, boolean forChargeback, InvoiceStatus originalInvoiceStatus) {
        super(InvoiceType.NICHE_AUCTION, fiatPayment, forChargeback, originalInvoiceStatus);
    }

    @Override
    protected WalletTransactionType getFiatPaymentReversalTransactionType() {
        return WalletTransactionType.NICHE_FIAT_PAYMENT_REVERSAL;
    }

    @Override
    protected void processReversal() {
        getAreaContext().doAreaTask(new ResetNicheForReversedFiatPaymentTask(fiatPayment));
    }
}
