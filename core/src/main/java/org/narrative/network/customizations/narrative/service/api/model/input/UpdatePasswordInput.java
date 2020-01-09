package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.core.user.PasswordFields;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Date: 9/27/18
 * Time: 9:52 AM
 *
 * @author brian
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Validated
@FieldNameConstants
public class UpdatePasswordInput extends UpdateProfileAccountConfirmationInputBase {
    @NotNull
    @Size(min = PasswordFields.MIN_PASSWORD_LENGTH, max = PasswordFields.MAX_PASSWORD_LENGTH, message = "{field.minMaxSize}")
    private final String newPassword;

    @Builder
    public UpdatePasswordInput(String currentPassword,
                               Integer twoFactorAuthCode,
                               @NotNull String newPassword) {
        super(currentPassword, twoFactorAuthCode);
        this.newPassword = newPassword;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    public static class Fields extends UpdateProfileAccountConfirmationInputBase.Fields {}
}
