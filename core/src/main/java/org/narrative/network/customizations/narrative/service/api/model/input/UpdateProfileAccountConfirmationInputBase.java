package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Date: 9/28/18
 * Time: 8:06 AM
 *
 * @author brian
 */
@Data
@Validated
@FieldNameConstants
public class UpdateProfileAccountConfirmationInputBase extends UpdateProfileAccount2FAConfirmationInputBase {
    @NotEmpty
    private final String currentPassword;

    public UpdateProfileAccountConfirmationInputBase(@NotEmpty String currentPassword, Integer twoFactorAuthCode) {
        super(twoFactorAuthCode);
        this.currentPassword = currentPassword;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields extends UpdateProfileAccount2FAConfirmationInputBase.Fields {}
}
