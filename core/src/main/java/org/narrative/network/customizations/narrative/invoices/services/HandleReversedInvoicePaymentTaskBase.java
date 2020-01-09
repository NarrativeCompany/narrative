package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-06
 * Time: 13:49
 *
 * @author jonmark
 */
public abstract class HandleReversedInvoicePaymentTaskBase extends AreaTaskImpl<Object> {

    protected final FiatPayment fiatPayment;
    protected final boolean forChargeback;
    protected final InvoiceStatus originalInvoiceStatus;

    public HandleReversedInvoicePaymentTaskBase(InvoiceType expectedType, FiatPayment fiatPayment, boolean forChargeback, InvoiceStatus originalInvoiceStatus) {
        this.originalInvoiceStatus = originalInvoiceStatus;
        assert exists(fiatPayment) : "Should always be created with a fiatPayment.";
        assert fiatPayment.getInvoice().getType() == expectedType : "Expected invoiceType/"+expectedType+" but got/"+fiatPayment.getInvoice().getType();

        this.fiatPayment = fiatPayment;
        this.forChargeback = forChargeback;
    }
}
