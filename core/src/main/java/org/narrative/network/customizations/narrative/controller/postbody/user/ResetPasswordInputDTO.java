package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.ResetPasswordInput;

public class ResetPasswordInputDTO extends ResetPasswordInput {
    @JsonCreator
    public ResetPasswordInputDTO(
            @JsonProperty(Fields.twoFactorAuthCode) Integer twoFactorAuthCode,
            @JsonProperty(Fields.resetPasswordKey) String resetPasswordKey,
            @JsonProperty(Fields.password) String password,
            @JsonProperty(Fields.passwordConfirm) String passwordConfirm,
            @JsonProperty(Fields.timestamp) Long timestamp
    ) {
        super(twoFactorAuthCode, resetPasswordKey, password, passwordConfirm, timestamp);
    }
}
