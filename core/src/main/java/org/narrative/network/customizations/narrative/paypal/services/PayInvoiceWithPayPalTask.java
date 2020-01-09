package org.narrative.network.customizations.narrative.paypal.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.FiatPaymentStatus;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.customizations.narrative.invoices.services.ProcessPaidInvoiceTask;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.impl.invoice.FiatPaymentProcessorType;
import org.narrative.network.customizations.narrative.service.impl.invoice.PayInvoiceBaseTask;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-01-28
 * Time: 13:51
 *
 * @author jonmark
 */
public class PayInvoiceWithPayPalTask extends PayInvoiceBaseTask<FiatPayment> {
    private final String paymentToken;

    public PayInvoiceWithPayPalTask(String paymentToken, OID invoiceOid) {
        super(invoiceOid);
        this.paymentToken = paymentToken;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        super.validate(validationContext);

        if (invoice.getType().isImmediateFiatPaymentType()) {
            throw UnexpectedError.getRuntimeException("Should never process ImmediateFiatPaymentTypes with this task!");
        }

        // jw: if the invoice does not support fiat payments we need to error out.
        if (!invoice.isSupportsFiatPayment()) {
            throw UnexpectedError.getRuntimeException("Attempting to purchase a Niche by Credit Card when fiat payments are not enabled!");
        }

        if (!exists(invoice.getFiatPayment())) {
            throw UnexpectedError.getRuntimeException("should always have a fiat payment associated at the point where this is called!");
        }
    }

    @Override
    protected FiatPayment doMonitoredTask() {
        // jw: lets lock this before we do any processing. This is the same object we lock on for all payment processing
        invoice.lockForProcessing();

        FiatPayment fiatPayment = invoice.getFiatPayment();

        // Setup the APIContext so that we can make requests to the PayPal Rest API
        String transactionId = getAreaContext().doAreaTask(new ValidatePayPalPaymentTask(
                invoice.getType()
                , fiatPayment.getTotalUsdAmount()
                , paymentToken
        ));

        // jw: let's ensure that there was not a double submit with the same paymentId
        if (isWasFiatPaymentAlreadyMade(fiatPayment, transactionId)) {
            return fiatPayment;
        }

        // jw: at this point the payment should be calculated... Should never get here for anything else.
        assert fiatPayment.getStatus().isCalculated() : "Expect the payment to be flagged as calculated at this point! not/"+fiatPayment.getStatus();

        // jw: update the fiatPayment so that it is considered paid!
        fiatPayment.setProcessorType(FiatPaymentProcessorType.PAYPAL);
        fiatPayment.setTransactionId(transactionId);
        fiatPayment.setTransactionDate(now());
        fiatPayment.setStatus(FiatPaymentStatus.PAID);

        // jw: if there was a nrve payment in process, let's go ahead and kill that now.
        NrvePayment nrvePayment = invoice.getNrvePayment();
        if (exists(nrvePayment)) {
            invoice.setNrvePayment(null);
            NrvePayment.dao().delete(nrvePayment);
        }

        // jw: finally, let's process the paid invoice
        getAreaContext().doAreaTask(new ProcessPaidInvoiceTask(invoice));

        return fiatPayment;
    }

    private boolean isWasFiatPaymentAlreadyMade(FiatPayment fiatPayment, String transactionId) {
        if (isEmpty(fiatPayment.getTransactionId())) {
            return false;
        }
        assert fiatPayment.getStatus().isPaid() : "Expect the payment to be flagged as paid at this point! not/"+fiatPayment.getStatus();

        // jw: as mentioned above, if a different payment already settled this invoice, then we need to refund this payment.
        if (!isEqual(fiatPayment.getTransactionId(), transactionId)) {
            throw UnexpectedError.getRuntimeException("Invoice was paid by a different process... That should NEVER happen. payed with paymentId/"+fiatPayment.getTransactionId()+" but was provided paymentId/"+ paymentToken);
        }

        return true;
    }
}
