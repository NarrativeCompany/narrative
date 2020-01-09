package org.narrative.network.customizations.narrative.controller.postbody.invoice;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.narrative.network.customizations.narrative.service.api.model.input.NrvePaymentInput;

import javax.validation.constraints.NotEmpty;

public class NrvePaymentInputDTO extends NrvePaymentInput {
    @JsonCreator
    public NrvePaymentInputDTO(@NotEmpty String neoAddress) {
        super(neoAddress);
    }
}
