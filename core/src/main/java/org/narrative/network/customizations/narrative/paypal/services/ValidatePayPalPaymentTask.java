package org.narrative.network.customizations.narrative.paypal.services;

import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.invoices.services.ValidateFiatPaymentTokenTask;
import org.narrative.network.shared.util.NetworkLogger;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RelatedResources;
import com.paypal.api.payments.Sale;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;

import java.math.BigDecimal;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-02-04
 * Time: 19:53
 *
 * @author jonmark
 */
public class ValidatePayPalPaymentTask extends ValidateFiatPaymentTokenTask {
    private static final NetworkLogger logger = new NetworkLogger(ValidatePayPalPaymentTask.class);

    public static final String COMPLETED_SALE_STATE = "completed";
    public static final String REFUNDED_SALE_STATE = "refunded";

    public ValidatePayPalPaymentTask(InvoiceType invoiceType, BigDecimal usdAmount, String paymentToken) {
        super(invoiceType, usdAmount, paymentToken);
    }

    @Override
    protected String doMonitoredTask() {
        PayPalCheckoutDetails checkoutDetails = getInvoiceType().getPayPalCheckoutDetails(getUsdAmount());

        APIContext apiContext = checkoutDetails.getApiContext();

        Payment payment;
        try {
            // jw: fetch the payment details from the PayPal REST API.
            payment = Payment.get(apiContext, getPaymentToken());
        } catch (Exception e) {
            // jw: even for webhook purposes this is bad, Let's allow the error to happen like normal, and that should
            //     cause the webhook to try again later.
            logger.error("Failed calling PayPal Rest API!", e);
            throw new ApplicationError(wordlet("purchaseNicheWithCardAction.apiException"));
        }

        // jw: let's ensure that the charge was made, and that it was for the expected amount!
        // jw: note: if a different payment already settled this invoice we would have errored out above.
        return validatePayment(checkoutDetails, payment);
    }

    private Sale getCompletedSaleFromPayment(Payment payment) {
        assert !payment.getTransactions().isEmpty() : "We should always have at least one transaction!";

        for (Transaction transaction : payment.getTransactions()) {
            Sale sale = getCompletedSaleFromTransaction(transaction);
            if (sale!=null) {
                return sale;
            }
        }

        // jw: if we made it through all transactions and did not hit a sale then let's try again.
        throw UnexpectedError.getRuntimeException("We should always find a sale from the payment. paymentId/"+paymentToken);
    }

    private Sale getCompletedSaleFromTransaction(Transaction transaction) {
        assert transaction!=null : "Received null transaction from Paypal!";

        for (RelatedResources resources : transaction.getRelatedResources()) {
            // jw: the moment we come across a refund let's short out.
            if (resources.getRefund()!=null) {
                throw UnexpectedError.getRuntimeException("We should never hit a payment that has been refunded before we processed the payment... paymentId/"+paymentToken);
            }
            // jw: if we have a sale, let's track it
            Sale sale = resources.getSale();
            if (sale!=null) {
                if (!COMPLETED_SALE_STATE.equals(sale.getState())) {
                    // jw: even for webhook purposes this is bad, Let's allow the error to happen like normal, and that should
                    //     cause the webhook to try again later.
                    if (logger.isInfoEnabled()) {
                        logger.info("Found sale with unexpected state/" + sale.getState() + " expected/" + COMPLETED_SALE_STATE);
                    }
                    return null;
                }

                return sale;
            }
        }

        // jw: seems strange that we would have a transaction with no sale, but let's not worry about that.
        return null;
    }

    private String validatePayment(PayPalCheckoutDetails checkoutDetails, Payment payment) {
        // first, get the sale out of the payment.
        Sale sale = getCompletedSaleFromPayment(payment);

        String totalPrice = checkoutDetails.getAmountForPayPal();

        // jw: next, let's ensure that it matches what we expected.
        if (!"USD".equals(sale.getAmount().getCurrency()) || !totalPrice.equals(sale.getAmount().getTotal())) {
            // jw: if the amount charged does not match what we requested, let's give a error that gives us the information
            //     necessary to refund the cost. Further, we need to know about this as pro-actively as possible, so log,
            //     record, and email about the problem.
            // note: now that we are throwing an UnexpectedError below, we do not need to manually log this message. Just email it.
            String loggingMessage = "Failed processing payment for niche purchase because the amount paid by the user (" + sale.getAmount().getTotal() + ") was not the amount that it should have been (" + totalPrice + " cents) for paymentToken/" + getPaymentToken() +" saleId/"+sale.getId();
            NetworkRegistry.getInstance().sendDevOpsStatusEmail("PurchaseNicheWithPayPalTask Unexpected Charge Error", loggingMessage);

            // jw: this should almost never happen, so if it does let's error out since something really bad is going on.
            throw UnexpectedError.getRuntimeException(loggingMessage);
        }

        // jw: use the sale.id as the transactionId.
        return sale.getId();
    }
}
