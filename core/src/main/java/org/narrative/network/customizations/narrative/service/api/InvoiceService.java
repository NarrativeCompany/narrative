package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.service.api.model.InvoiceDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.InvoiceStatusDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.NrvePaymentDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.FiatPaymentInput;
import org.narrative.network.customizations.narrative.service.api.model.input.ImmediateFiatPaymentInput;
import org.narrative.network.customizations.narrative.service.api.model.input.NrvePaymentInput;

public interface InvoiceService {
    InvoiceDetailDTO getInvoice(OID invoiceOid);

    InvoiceStatusDetailDTO getInvoicePaymentStatus(OID invoiceOid);

    NrvePaymentDTO putNrvePayment(NrvePaymentInput nrvePaymentInput, OID invoiceOid);

    // jw: since there is only ever one payment, there is no need to provide the payment OID here
    InvoiceDetailDTO deleteNrvePayment(OID invoiceOid);

    InvoiceDetailDTO putFiatPayment(FiatPaymentInput paymentInput, OID invoiceOid);

    InvoiceDetailDTO postImmediateFiatPayment(ImmediateFiatPaymentInput paymentInput);
}
