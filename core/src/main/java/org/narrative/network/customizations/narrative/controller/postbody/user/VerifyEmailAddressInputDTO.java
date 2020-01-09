package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.VerifyEmailAddressInput;

/**
 * Date: 10/1/18
 * Time: 8:38 AM
 *
 * @author brian
 */
public class VerifyEmailAddressInputDTO extends VerifyEmailAddressInput {
    @JsonCreator
    public VerifyEmailAddressInputDTO(@JsonProperty(Fields.confirmationId) String confirmationId) {
        super(confirmationId);
    }
}
