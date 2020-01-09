package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdatePasswordInput;

/**
 * Date: 9/28/18
 * Time: 8:01 AM
 *
 * @author brian
 */
public class UpdatePasswordInputDTO extends UpdatePasswordInput {
    @JsonCreator
    public UpdatePasswordInputDTO(@JsonProperty(Fields.currentPassword) String currentPassword,
                                  @JsonProperty(Fields.twoFactorAuthCode) Integer twoFactorAuthCode,
                                  @JsonProperty(Fields.newPassword) String newPassword) {
        super(currentPassword, twoFactorAuthCode, newPassword);
    }
}
