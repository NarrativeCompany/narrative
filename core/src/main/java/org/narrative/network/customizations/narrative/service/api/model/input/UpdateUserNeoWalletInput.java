package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 2019-06-26
 * Time: 12:37
 *
 * @author jonmark
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Validated
@FieldNameConstants
public class UpdateUserNeoWalletInput extends UpdateProfileAccountConfirmationInputBase {
    private final String neoAddress;

    @Builder
    public UpdateUserNeoWalletInput(
            String currentPassword,
            Integer twoFactorAuthCode,
            String neoAddress
    ) {
        super(currentPassword, twoFactorAuthCode);
        this.neoAddress = neoAddress;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    public static class Fields extends UpdateProfileAccountConfirmationInputBase.Fields {}
}
