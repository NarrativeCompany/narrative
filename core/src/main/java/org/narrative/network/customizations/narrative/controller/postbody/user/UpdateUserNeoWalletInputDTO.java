package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateUserNeoWalletInput;

/**
 * Date: 2019-06-26
 * Time: 12:43
 *
 * @author jonmark
 */
public class UpdateUserNeoWalletInputDTO extends UpdateUserNeoWalletInput {
    @JsonCreator
    public UpdateUserNeoWalletInputDTO(@JsonProperty(Fields.currentPassword) String currentPassword,
                                       @JsonProperty(Fields.twoFactorAuthCode) Integer twoFactorAuthCode,
                                       @JsonProperty(Fields.neoAddress) String neoAddress) {
        super(currentPassword, twoFactorAuthCode, neoAddress);
    }
}
