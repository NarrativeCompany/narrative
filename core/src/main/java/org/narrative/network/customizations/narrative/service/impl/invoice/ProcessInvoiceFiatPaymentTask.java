package org.narrative.network.customizations.narrative.service.impl.invoice;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-01-30
 * Time: 14:13
 *
 * @author jonmark
 */
public class ProcessInvoiceFiatPaymentTask extends AreaTaskImpl<FiatPayment> {
    private final OID invoiceOid;
    private final FiatPaymentProcessorType processorType;
    private final String paymentToken;

    public ProcessInvoiceFiatPaymentTask(OID invoiceOid, FiatPaymentProcessorType processorType, String paymentToken) {
        assert processorType != null : "Should always specify a processor type.";
        assert invoiceOid != null : "Should always specify an invoice.";
        assert !isEmpty(paymentToken) : "Should always provide a token.";

        this.invoiceOid = invoiceOid;
        this.processorType = processorType;
        this.paymentToken = paymentToken;
    }

    @Override
    protected FiatPayment doMonitoredTask() {
        // jw: I considered getting the invoice up front here, but our existing tasks are all already designed to work
        //     with a invoiceOid, so just leaving for now.

        AreaTaskImpl<FiatPayment> paymentProcessor = processorType.getInvoicePaymentProcessor(invoiceOid, paymentToken);

        if (paymentProcessor==null) {
            throw UnexpectedError.getRuntimeException("Should always get a payment processor from the processorType!");
        }

        return getAreaContext().doAreaTask(paymentProcessor);
    }
}
