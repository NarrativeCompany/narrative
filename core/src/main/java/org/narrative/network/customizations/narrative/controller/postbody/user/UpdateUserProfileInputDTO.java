package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateUserProfileInput;

/**
 * Date: 9/19/18
 * Time: 10:38 AM
 *
 * @author brian
 */
public class UpdateUserProfileInputDTO extends UpdateUserProfileInput {

    @JsonCreator
    public UpdateUserProfileInputDTO(@JsonProperty(Fields.displayName) String displayName, @JsonProperty(Fields.username) String username) {
        super(displayName, username);
    }
}
