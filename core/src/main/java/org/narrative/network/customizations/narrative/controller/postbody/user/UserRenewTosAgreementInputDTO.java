package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.UserRenewTosAgreementInput;

/**
 * Date: 9/25/18
 * Time: 12:00 PM
 *
 * @author brian
 */
public class UserRenewTosAgreementInputDTO extends UserRenewTosAgreementInput {
    @JsonCreator
    public UserRenewTosAgreementInputDTO(@JsonProperty(Fields.hasAgreedToTos) boolean hasAgreedToTos) {
        super(hasAgreedToTos);
    }
}
