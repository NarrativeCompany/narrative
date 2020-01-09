package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.core.user.PasswordFields;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Date: 9/29/18
 * Time: 10:51 AM
 *
 * @author brian
 */
@Data
@Validated
@FieldNameConstants
public class ResetPasswordInput extends UpdateProfileAccount2FAConfirmationInputBase {
    @NotNull
    private final String resetPasswordKey;

    @NotNull
    @Size(min = PasswordFields.MIN_PASSWORD_LENGTH, max = PasswordFields.MAX_PASSWORD_LENGTH, message = "{field.minMaxSize}")
    private final String password;

    @NotNull
    @Size(min = PasswordFields.MIN_PASSWORD_LENGTH, max = PasswordFields.MAX_PASSWORD_LENGTH, message = "{field.minMaxSize}")
    private final String passwordConfirm;

    private final Long timestamp;

    public ResetPasswordInput(Integer twoFactorAuthCode, @NotNull String resetPasswordKey, @NotNull @Size(min = PasswordFields.MIN_PASSWORD_LENGTH, max = PasswordFields.MAX_PASSWORD_LENGTH, message = "{field.minMaxSize}") String password, @NotNull @Size(min = PasswordFields.MIN_PASSWORD_LENGTH, max = PasswordFields.MAX_PASSWORD_LENGTH, message = "{field.minMaxSize}") String passwordConfirm, Long timestamp) {
        super(twoFactorAuthCode);

        this.resetPasswordKey = resetPasswordKey;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.timestamp = timestamp;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields extends UpdateProfileAccount2FAConfirmationInputBase.Fields {}
}
