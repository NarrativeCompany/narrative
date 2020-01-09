package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.service.impl.invoice.FiatPaymentProcessorType;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-04-17
 * Time: 08:47
 *
 * @author jonmark
 */
public abstract class RefundFiatPaymentBaseTask extends AreaTaskImpl<Object> {
    private static final NetworkLogger logger = new NetworkLogger(RefundFiatPaymentBaseTask.class);

    private final FiatPayment payment;

    protected RefundFiatPaymentBaseTask(FiatPayment payment) {
        assert exists(payment) : "Should always be provided with a payment!";

        this.payment = payment;
    }

    // jw: this should throw an exception if there is any problem issuing the refund. The exception will be sent to us
    //     via email.
    protected abstract void issueRefund(FiatPayment payment) throws Exception;

    @Override
    protected Object doMonitoredTask() {
        // jw: if the payment is anything except paid we should skip it!
        if (!payment.getStatus().isPaid()) {
            return null;
        }

        // jw: before we issue the refund, let's lock for processing in case there is a webhook or other process we are
        //     racing against.
        payment.getInvoice().lockForProcessing();

        // jw: we need to refresh the payment just in case something about it changed externally.
        FiatPayment.dao().refresh(payment);

        // jw: one last time, just in case another a different process beat us to it, short out for anything other than paid.
        if (!payment.getStatus().isPaid()) {
            return null;
        }

        try {
            // jw: issue the refund
            issueRefund(payment);

            // jw: let's use the generic ProcessReversedFiatPaymentTask to handle updating the invoice/payment. This is
            //     the same task used by the webhook, so should be safe and should help ensure consistency.
            getAreaContext().doAreaTask(new ProcessReversedFiatPaymentTask(
                    FiatPaymentProcessorType.PAYPAL
                    , payment.getTransactionId()
                    // jw: this is a refund, not a chargeback
                    , false
            ));

        // jw: catch any exception and report the failure to us via email
        } catch (Exception e) {
            String message = "Failed issuing fiat payment refund for invoice/"+payment.getInvoice().getOid()+" processorType/"+payment.getProcessorType()+" transactionId/"+payment.getTransactionId();
            StatisticManager.recordException(UnexpectedError.getRuntimeException(message), false, null);
            logger.error(message);
            NetworkRegistry.getInstance().sendDevOpsStatusEmail("RefundFiatPaymentTask Error", message);
        }

        return null;
    }
}
