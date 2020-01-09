package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.common.persistence.OID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.TimeZone;

/**
 * Date: 9/27/18
 * Time: 8:23 AM
 *
 * @author brian
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Validated
@FieldNameConstants
public class RegisterUserInput extends RegisterUserStepOneInput {

    @NotEmpty
    private final String recaptchaToken;

    private final List<OID> nichesToFollow;

    public RegisterUserInput(String displayName,
                             String username,
                             @NotNull String emailAddress,
                             @NotNull String password,
                             boolean hasAgreedToTos,
                             @NotNull TimeZone timeZone,
                             String recaptchaResponse,
                             String recaptchaToken,
                             List<OID> nichesToFollow) {
        super(displayName, username, emailAddress, password, hasAgreedToTos, timeZone, recaptchaResponse);
        this.recaptchaToken = recaptchaToken;
        this.nichesToFollow = nichesToFollow;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    public static class Fields extends RegisterUserStepOneInput.Fields {}
}
