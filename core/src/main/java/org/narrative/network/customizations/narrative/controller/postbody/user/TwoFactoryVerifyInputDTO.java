package org.narrative.network.customizations.narrative.controller.postbody.user;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Date: 10/8/18
 * Time: 9:42 AM
 *
 * @author brian
 */
@Value
@Validated
@FieldNameConstants
@Builder(toBuilder = true)
public class TwoFactoryVerifyInputDTO {
    @NotEmpty
    private final String verificationCode;

    private final boolean rememberMe;
}
