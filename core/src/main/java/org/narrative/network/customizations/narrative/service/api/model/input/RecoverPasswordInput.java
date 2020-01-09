package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
@FieldNameConstants
public class RecoverPasswordInput {
    @NotEmpty
    private final String emailAddress;

    // bl: allow this to be empty so that we can present a specific error message when it's invalid/not found
    private final String recaptchaResponse;
}
