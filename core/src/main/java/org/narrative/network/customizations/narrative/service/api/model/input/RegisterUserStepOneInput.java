package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.PasswordFields;
import org.narrative.network.core.user.UserFields;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
public class RegisterUserStepOneInput extends UserProfileInputBase {
    @NotNull
    @Size(min = EmailAddress.MIN_EMAIL_ADDRESS_LENGTH, max = EmailAddress.MAX_EMAIL_ADDRESS_LENGTH, message = "{field.minMaxSize}")
    @Email
    private final String emailAddress;

    @NotNull
    @Size(min = PasswordFields.MIN_PASSWORD_LENGTH, max = PasswordFields.MAX_PASSWORD_LENGTH, message = "{field.minMaxSize}")
    private final String password;

    @AssertTrue
    private final boolean hasAgreedToTos;

    @NotNull
    private final TimeZone timeZone;

    @NotEmpty
    private final String recaptchaResponse;

    public RegisterUserStepOneInput(String displayName,
                                    String username,
                                    @NotNull String emailAddress,
                                    @NotNull String password,
                                    boolean hasAgreedToTos,
                                    @NotNull TimeZone timeZone,
                                    String recaptchaResponse) {
        super(displayName, username);
        this.emailAddress = emailAddress;
        this.password = password;
        this.hasAgreedToTos = hasAgreedToTos;
        this.timeZone = timeZone;
        this.recaptchaResponse = recaptchaResponse;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields extends UserProfileInputBase.Fields {}
}
