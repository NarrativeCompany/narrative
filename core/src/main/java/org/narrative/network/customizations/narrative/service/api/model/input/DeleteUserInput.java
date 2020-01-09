package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.AssertTrue;

/**
 * Date: 9/28/18
 * Time: 3:12 PM
 *
 * @author brian
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Validated
@FieldNameConstants
public class DeleteUserInput extends UpdateProfileAccountConfirmationInputBase {
    @AssertTrue
    private final boolean confirmDeleteAccount;

    public DeleteUserInput(String currentPassword,
                           Integer twoFactorAuthCode,
                           boolean confirmDeleteAccount) {
        super(currentPassword, twoFactorAuthCode);
        this.confirmDeleteAccount = confirmDeleteAccount;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    public static class Fields extends UpdateProfileAccountConfirmationInputBase.Fields {}
}
