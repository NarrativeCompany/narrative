package org.narrative.network.customizations.narrative.paypal.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.invoices.services.RefundFiatPaymentBaseTask;
import com.paypal.api.payments.Capture;
import com.paypal.api.payments.DetailedRefund;
import com.paypal.api.payments.RefundRequest;
import com.paypal.base.rest.APIContext;

/**
 * Date: 2019-04-17
 * Time: 08:55
 *
 * @author jonmark
 */
public class RefundPayPalPaymentTask extends RefundFiatPaymentBaseTask {

    public RefundPayPalPaymentTask(FiatPayment payment) {
        super(payment);

        assert payment.getProcessorType().isPayPal() : "Expected to get a paypal payment here!";
    }

    @Override
    protected void issueRefund(FiatPayment payment) throws Exception {
        Invoice invoice = payment.getInvoice();

        // jw: we will need the apiContext to get the capture for this transaction, and then again to issue the refund.
        APIContext apiContext = InvoiceType.NICHE_AUCTION_SECURITY_DEPOSIT.getPayPalApiConfig().getApiContext();

        // jw: fetch the "capture" from PayPal.
        Capture capture = Capture.get(apiContext, invoice.getFiatPayment().getTransactionId());

        // jw: ensure we fetched a capture from PayPal's API
        if (capture==null) {
            throw UnexpectedError.getRuntimeException("Failed looking up capture from PayPal for invoice/"+invoice.getOid()+" with transactionId/"+invoice.getFiatPayment().getTransactionId());
        }

        // jw: according to their documentation a empty object in the request means to issue a full refund, so that is what
        //     we will do here. See:
        // https://developer.paypal.com/docs/integration/direct/payments/refund-payments/#fully-refund-a-sale
        DetailedRefund refund = capture.refund(apiContext, new RefundRequest());

        // jw: the presence of the refund object tells us that the refund went through.
        if (refund == null) {
            throw UnexpectedError.getRuntimeException("Refund was not processed by PayPal for unknown reason");
        }
    }
}
