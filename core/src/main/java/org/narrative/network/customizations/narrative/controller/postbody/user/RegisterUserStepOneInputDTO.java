package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.RegisterUserStepOneInput;

import java.util.TimeZone;

/**
 * Date: 9/25/18
 * Time: 12:00 PM
 *
 * @author brian
 */
public class RegisterUserStepOneInputDTO extends RegisterUserStepOneInput {
    @JsonCreator
    public RegisterUserStepOneInputDTO(@JsonProperty(Fields.displayName) String displayName,
                                       @JsonProperty(Fields.username) String username,
                                       @JsonProperty(Fields.emailAddress) String emailAddress,
                                       @JsonProperty(Fields.password) String password,
                                       @JsonProperty(Fields.hasAgreedToTos) boolean hasAgreedToTos,
                                       @JsonProperty(Fields.timeZone) TimeZone timeZone,
                                       @JsonProperty(Fields.recaptchaResponse) String recaptchaResponse) {
        super(displayName, username, emailAddress, password, hasAgreedToTos, timeZone, recaptchaResponse);
    }
}
