package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Validated
@FieldNameConstants
public class EnableTwoFactorAuthInput {
    @NotEmpty
    private final String currentPassword;
    @NotNull
    private final Integer twoFactorAuthCode;
    @NotEmpty
    private final String secret;
    private final boolean rememberMe;

    @Builder
    public EnableTwoFactorAuthInput(@NotEmpty String currentPassword, @NotNull Integer twoFactorAuthCode, @NotEmpty String secret, boolean rememberMe) {
        this.currentPassword = currentPassword;
        this.twoFactorAuthCode = twoFactorAuthCode;
        this.secret = secret;
        this.rememberMe = rememberMe;
    }
}
