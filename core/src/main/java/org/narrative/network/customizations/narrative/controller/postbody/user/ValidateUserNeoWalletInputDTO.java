package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.ValidateUserNeoWalletInput;

/**
 * Date: 2019-07-01
 * Time: 11:14
 *
 * @author jonmark
 */
public class ValidateUserNeoWalletInputDTO extends ValidateUserNeoWalletInput {
    @JsonCreator
    public ValidateUserNeoWalletInputDTO(@JsonProperty(Fields.neoAddress) String neoAddress) {
        super(neoAddress);
    }
}
