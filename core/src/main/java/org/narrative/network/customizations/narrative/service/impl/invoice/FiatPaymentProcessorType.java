package org.narrative.network.customizations.narrative.service.impl.invoice;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.invoices.services.RefundFiatPaymentBaseTask;
import org.narrative.network.customizations.narrative.invoices.services.ValidateFiatPaymentTokenTask;
import org.narrative.network.customizations.narrative.paypal.services.PayInvoiceWithPayPalTask;
import org.narrative.network.customizations.narrative.paypal.services.RefundPayPalPaymentTask;
import org.narrative.network.customizations.narrative.paypal.services.ValidatePayPalPaymentTask;

import java.math.BigDecimal;

/**
 * Date: 2019-01-30
 * Time: 12:33
 *
 * @author jonmark
 */
public enum FiatPaymentProcessorType implements IntegerEnum {
    STRIPE(0) {
        @Override
        public PayInvoiceBaseTask<FiatPayment> getInvoicePaymentProcessor(OID invoiceOid, String paymentToken) {
            // jw: note: this is only here because of the Stripe webhook, which we are keeping in case of refunds and chargebacks.
            throw UnexpectedError.getRuntimeException("We no longer support stripe payments, so we should never get here!");
        }

        @Override
        public ValidateFiatPaymentTokenTask getPaymentValidator(InvoiceType invoiceType, BigDecimal usdAmount, String paymentToken) {
            // jw: note: this is only here because of the Stripe webhook, which we are keeping in case of refunds and chargebacks.
            throw UnexpectedError.getRuntimeException("We no longer support stripe payments, so we should never get here!");
        }

        @Override
        public RefundFiatPaymentBaseTask getPaymentRefundProcessor(FiatPayment payment) {
            throw UnexpectedError.getRuntimeException("We no longer support stripe payments, so we should never get here!");
        }
    }
    ,PAYPAL(1) {
        @Override
        public PayInvoiceBaseTask<FiatPayment> getInvoicePaymentProcessor(OID invoiceOid, String paymentToken) {
            return new PayInvoiceWithPayPalTask(paymentToken, invoiceOid);
        }

        @Override
        public ValidateFiatPaymentTokenTask getPaymentValidator(InvoiceType invoiceType, BigDecimal usdAmount, String paymentToken) {
            return new ValidatePayPalPaymentTask(invoiceType, usdAmount, paymentToken);
        }

        @Override
        public RefundFiatPaymentBaseTask getPaymentRefundProcessor(FiatPayment payment) {
            return new RefundPayPalPaymentTask(payment);
        }
    }
    ;

    private final int id;

    FiatPaymentProcessorType(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isPayPal() {
        return this == PAYPAL;
    }

    public abstract PayInvoiceBaseTask<FiatPayment> getInvoicePaymentProcessor(OID invoiceOid, String paymentToken);
    public abstract ValidateFiatPaymentTokenTask getPaymentValidator(InvoiceType invoiceType, BigDecimal usdAmount, String paymentToken);
    public abstract RefundFiatPaymentBaseTask getPaymentRefundProcessor(FiatPayment payment);
}