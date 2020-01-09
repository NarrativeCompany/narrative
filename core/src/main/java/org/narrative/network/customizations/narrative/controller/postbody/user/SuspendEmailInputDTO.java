package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.SuspendEmailInput;

/**
 * Date: 10/4/18
 * Time: 8:36 AM
 *
 * @author brian
 */
public class SuspendEmailInputDTO extends SuspendEmailInput {
    @JsonCreator
    public SuspendEmailInputDTO(@JsonProperty(Fields.emailAddress) String emailAddress, @JsonProperty(Fields.token) String token) {
        super(emailAddress, token);
    }
}
