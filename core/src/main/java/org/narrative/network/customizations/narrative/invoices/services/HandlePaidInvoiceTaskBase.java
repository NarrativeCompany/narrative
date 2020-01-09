package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-06
 * Time: 13:44
 *
 * @author jonmark
 */
public abstract class HandlePaidInvoiceTaskBase extends AreaTaskImpl<Object> {
    protected final Invoice invoice;

    public HandlePaidInvoiceTaskBase(InvoiceType expectedType, Invoice invoice) {
        assert exists(invoice) : "We should always have a invoice when this is called";
        assert invoice.getType() == expectedType : "The invoice should always be of type/"+expectedType+" not/"+invoice.getType();
        assert invoice.getStatus().isPaid() : "The invoice should be in a paid state! invoice/" + invoice.getOid();

        this.invoice = invoice;
    }
}
