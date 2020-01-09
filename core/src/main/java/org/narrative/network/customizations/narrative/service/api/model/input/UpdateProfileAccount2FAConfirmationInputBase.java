package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 2019-07-08
 * Time: 15:03
 *
 * @author jonmark
 */
@Data
@Validated
@FieldNameConstants
public class UpdateProfileAccount2FAConfirmationInputBase {

    /**
     * 2-factor auth code to verify 2FA for accounts that have it enabled.
     * optional since not all accounts will have 2FA enabled.
     */
    private final Integer twoFactorAuthCode;

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields {}
}
