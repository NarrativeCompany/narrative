package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateProfileAccountConfirmationInputBase;

/**
 * To delete the account's NEO wallet, you just need to provide the account confirmation details and nothing else.
 *
 * Date: 2019-07-18
 * Time: 9:54
 *
 * @author brian
 */
public class DeleteUserNeoWalletInputDTO extends UpdateProfileAccountConfirmationInputBase {
    @JsonCreator
    public DeleteUserNeoWalletInputDTO(@JsonProperty(Fields.currentPassword) String currentPassword,
                                       @JsonProperty(Fields.twoFactorAuthCode) Integer twoFactorAuthCode) {
        super(currentPassword, twoFactorAuthCode);
    }
}
