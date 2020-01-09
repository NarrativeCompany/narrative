package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.invoices.services.HandleReversedChannelInvoicePaymentTaskBase;

/**
 * Date: 2019-08-08
 * Time: 14:29
 *
 * @author jonmark
 */
public class HandleReversedPublicationInvoicePaymentTask extends HandleReversedChannelInvoicePaymentTaskBase {
    public HandleReversedPublicationInvoicePaymentTask(FiatPayment fiatPayment, boolean forChargeback, InvoiceStatus originalInvoiceStatus) {
        super(InvoiceType.PUBLICATION_ANNUAL_FEE, fiatPayment, forChargeback, originalInvoiceStatus);
    }

    @Override
    protected WalletTransactionType getFiatPaymentReversalTransactionType() {
        return WalletTransactionType.PUBLICATION_FIAT_PAYMENT_REVERSAL;
    }

    @Override
    protected void processReversal() {
        getAreaContext().doAreaTask(new ResetPublicationForReversedFiatPaymentTask(fiatPayment));
    }
}
