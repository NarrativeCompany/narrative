package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.customizations.narrative.controller.postbody.invoice.ValidImmediateFiatPaymentInvoiceType;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.service.impl.invoice.FiatPaymentProcessorType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Date: 2019-02-06
 * Time: 14:25
 *
 * @author jonmark
 */
@Data
@EqualsAndHashCode()
@ToString(callSuper = true)
@Validated
@FieldNameConstants
public class ImmediateFiatPaymentInput extends FiatPaymentInput {
    @NotNull
    @ValidImmediateFiatPaymentInvoiceType
    private final InvoiceType invoiceType;

    public ImmediateFiatPaymentInput(@NotNull FiatPaymentProcessorType processorType, @NotEmpty String paymentToken, @NotNull InvoiceType invoiceType) {
        super(processorType, paymentToken);
        this.invoiceType = invoiceType;
    }
}
