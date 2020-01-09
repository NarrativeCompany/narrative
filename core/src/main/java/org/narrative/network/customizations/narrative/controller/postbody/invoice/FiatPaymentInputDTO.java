package org.narrative.network.customizations.narrative.controller.postbody.invoice;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.narrative.network.customizations.narrative.service.api.model.input.FiatPaymentInput;
import org.narrative.network.customizations.narrative.service.impl.invoice.FiatPaymentProcessorType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Date: 2019-01-28
 * Time: 13:16
 *
 * @author jonmark
 */
public class FiatPaymentInputDTO extends FiatPaymentInput {
    @JsonCreator
    public FiatPaymentInputDTO(@NotNull FiatPaymentProcessorType processorType, @NotEmpty String paymentToken) {
        super(processorType, paymentToken);
    }
}
