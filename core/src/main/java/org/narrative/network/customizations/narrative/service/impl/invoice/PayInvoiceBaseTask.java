package org.narrative.network.customizations.narrative.service.impl.invoice;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

public abstract class PayInvoiceBaseTask<T> extends AreaTaskImpl<T> {

    private final OID invoiceOid;
    protected Invoice invoice;

    public PayInvoiceBaseTask(OID invoiceOid) {
        this.invoiceOid = invoiceOid;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        invoice = getAndValidateInvoice(invoiceOid, getAreaContext().getAreaRole());
    }

    public static Invoice getAndValidateInvoice(OID invoiceOid, AreaRole areaRole) {
        // jw: if we were provided with a areaRole, let's ensure that it is a registered user.
        areaRole.checkRegisteredCommunityUser();

        Invoice invoice = Invoice.dao().get(invoiceOid);
        if (!exists(invoice)) {
            if (invoice != null) {
                throw new ApplicationError(wordlet("payInvoiceBaseTask.invoiceNoLongerAvailable"));
            }
            throw UnexpectedError.getRuntimeException("Should always specify a invoice, and since we likely will never delete them we should have never gotten here unless someone is guessing!");
        }

        if (!invoice.isAccessibleByAreaRole(areaRole)) {
            // bl: give an explicit error as to why the invoice is inaccessible
            throw new ApplicationError(wordlet("payInvoiceBaseTask.wrongUser."+invoice.getType(), invoice.getUser().getDisplayNameResolved()));
        }

        return invoice;
    }
}