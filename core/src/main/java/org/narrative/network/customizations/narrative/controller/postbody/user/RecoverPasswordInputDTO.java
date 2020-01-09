package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.RecoverPasswordInput;

public class RecoverPasswordInputDTO extends RecoverPasswordInput {
    @JsonCreator
    public RecoverPasswordInputDTO(
            @JsonProperty(Fields.emailAddress) String emailAddress,
            @JsonProperty(Fields.recaptchaResponse) String recaptchaResponse
    ) {
        super(emailAddress, recaptchaResponse);
    }
}
