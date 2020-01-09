package org.narrative.network.customizations.narrative.service.impl.invoice;

import org.narrative.common.util.UnexpectedError;
import org.narrative.config.StaticConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.invoices.services.ProcessPaidInvoiceTask;
import org.narrative.network.customizations.narrative.invoices.services.ValidateFiatPaymentTokenTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.math.BigDecimal;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-06
 * Time: 14:11
 *
 * @author jonmark
 */
public class ProcessImmediateFiatPaymentTask extends AreaTaskImpl<Invoice> {
    private final FiatPaymentProcessorType processorType;
    private final InvoiceType invoiceType;
    private final String paymentToken;

    public ProcessImmediateFiatPaymentTask(FiatPaymentProcessorType processorType, InvoiceType invoiceType, String paymentToken) {
        assert processorType != null : "Should always specify a processor type.";
        assert invoiceType != null : "Should always specify a invoice type.";
        assert invoiceType.isImmediateFiatPaymentType() : "Specified Invoice Type must be for immediate fiat payments.";
        assert !isEmpty(paymentToken) : "Should always provide a token.";

        this.processorType = processorType;
        this.invoiceType = invoiceType;
        this.paymentToken = paymentToken;
    }

    // jw: we need to expose a method for some invoice types to create their own consumer prior to the invoice being finalized.
    protected void performConsumerProcessing(FiatPayment payment) {}

    @Override
    protected Invoice doMonitoredTask() {
        getNetworkContext().getPrimaryRole().checkRegisteredUser();
        User user = getNetworkContext().getUser();

        // jw: let's validate that the user can still access this type
        if (!invoiceType.isImmediateFiatPaymentTypeAvailable(user)) {
            throw UnexpectedError.getRuntimeException("This should never be called for users who cannot start the KYC Check process.");
        }

        // jw: first, let's get how much this is supposed to cost!
        BigDecimal price = invoiceType.getImmediateFiatPaymentAmount(user, StaticConfig.getBean(NarrativeProperties.class));

        // jw: next, let's get a validator task so we can get the transactionId from the paymentToken.
        ValidateFiatPaymentTokenTask validator = processorType.getPaymentValidator(invoiceType, price, paymentToken);
        if (validator==null) {
            throw UnexpectedError.getRuntimeException("Failed getting validator from processorType/"+processorType);
        }

        String transactionId = getAreaContext().doAreaTask(validator);
        if (isEmpty(transactionId)) {
            throw UnexpectedError.getRuntimeException("Failed getting transactionId from paymentToken/"+paymentToken+" for processorType/"+processorType+" and invoiceType/"+invoiceType);
        }

        // jw: now that we confirmed the payment, let's create a Invoice
        Invoice invoice = new Invoice(invoiceType, user, price);
        Invoice.dao().save(invoice);

        // jw: now, that the invoice is saved it is safe to save the FiatPayment
        FiatPayment fiatPayment = new FiatPayment(invoice);
        fiatPayment.setProcessorType(processorType);
        fiatPayment.setTransactionId(transactionId);
        fiatPayment.setTransactionDate(now());
        FiatPayment.dao().save(fiatPayment);

        // jw: prior to flushing let's perform consumer specific processing
        performConsumerProcessing(fiatPayment);

        // jw: before we move on we need to flush the objects to the database. Some of the ProcessPaidInvoiceTask processing
        //     requires refreshing the invoice for locking purposes, and thus we need to ensure that the object exists in the
        //     database prior.
        PartitionType.flushAllOpenSessionsForCurrentPartitionGroup();

        // jw: and finally, let's process the paid invoice
        getAreaContext().doAreaTask(new ProcessPaidInvoiceTask(invoice));

        return invoice;
    }
}
