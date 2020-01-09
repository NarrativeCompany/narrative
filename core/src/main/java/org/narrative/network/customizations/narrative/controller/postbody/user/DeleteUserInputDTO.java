package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.DeleteUserInput;

/**
 * Date: 9/25/18
 * Time: 12:00 PM
 *
 * @author brian
 */
public class DeleteUserInputDTO extends DeleteUserInput {
    @JsonCreator
    public DeleteUserInputDTO(@JsonProperty(Fields.currentPassword) String currentPassword,
                              @JsonProperty(Fields.twoFactorAuthCode) Integer twoFactorAuthCode,
                              @JsonProperty(Fields.confirmDeleteAccount) boolean confirmDeleteAccount) {
        super(currentPassword, twoFactorAuthCode, confirmDeleteAccount);
    }
}
