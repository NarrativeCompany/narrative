package org.narrative.network.customizations.narrative.controller.postbody.user;

import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * Date: 9/30/18
 * Time: 11:34 AM
 *
 * @author brian
 */
@Value
@Validated
@FieldNameConstants
public class LoginInputDTO {
    @NotEmpty
    @Email
    private final String emailAddress;

    @NotEmpty
    private final String password;

    private final boolean rememberMe;

    @NotEmpty
    private final String recaptchaResponse;
}
