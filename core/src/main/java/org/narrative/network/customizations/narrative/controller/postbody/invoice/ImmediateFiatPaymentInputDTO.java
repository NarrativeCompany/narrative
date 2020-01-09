package org.narrative.network.customizations.narrative.controller.postbody.invoice;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.service.api.model.input.ImmediateFiatPaymentInput;
import org.narrative.network.customizations.narrative.service.impl.invoice.FiatPaymentProcessorType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Date: 2019-02-06
 * Time: 14:12
 *
 * @author jonmark
 */
public class ImmediateFiatPaymentInputDTO extends ImmediateFiatPaymentInput {
    @JsonCreator
    public ImmediateFiatPaymentInputDTO(@NotNull FiatPaymentProcessorType processorType, @NotEmpty String paymentToken, @NotNull InvoiceType invoiceType) {
        super(processorType, paymentToken, invoiceType);
    }
}
