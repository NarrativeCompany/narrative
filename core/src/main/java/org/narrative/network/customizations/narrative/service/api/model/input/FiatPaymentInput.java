package org.narrative.network.customizations.narrative.service.api.model.input;

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
public class FiatPaymentInput implements InvoicePaymentInput {
    @NotNull
    private final FiatPaymentProcessorType processorType;
    @NotEmpty
    private final String paymentToken;

    public FiatPaymentInput(@NotNull FiatPaymentProcessorType processorType, @NotEmpty String paymentToken) {
        this.processorType = processorType;
        this.paymentToken = paymentToken;
    }
}
