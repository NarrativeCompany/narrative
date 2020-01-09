package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.service.api.model.input.RegisterUserInput;

import java.util.List;
import java.util.TimeZone;

/**
 * Date: 9/25/18
 * Time: 12:00 PM
 *
 * @author brian
 */
public class RegisterUserInputDTO extends RegisterUserInput {
    @JsonCreator
    public RegisterUserInputDTO(@JsonProperty(Fields.displayName) String displayName,
                                @JsonProperty(Fields.username) String username,
                                @JsonProperty(Fields.emailAddress) String emailAddress,
                                @JsonProperty(Fields.password) String password,
                                @JsonProperty(Fields.hasAgreedToTos) boolean hasAgreedToTos,
                                @JsonProperty(Fields.timeZone) TimeZone timeZone,
                                @JsonProperty(Fields.recaptchaResponse) String recaptchaResponse,
                                @JsonProperty(Fields.recaptchaToken) String recaptchaToken,
                                @JsonProperty(Fields.nichesToFollow)  List<OID> nichesToFollow) {
        super(displayName, username, emailAddress, password, hasAgreedToTos, timeZone, recaptchaResponse, recaptchaToken, nichesToFollow);
    }
}
