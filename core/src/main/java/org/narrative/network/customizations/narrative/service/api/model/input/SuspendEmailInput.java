package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * Date: 9/29/18
 * Time: 10:51 AM
 *
 * @author brian
 */
@Data
@Validated
@FieldNameConstants
@Builder
@AllArgsConstructor
public class SuspendEmailInput {
    @NotEmpty
    @Email
    private final String emailAddress;

    @NotEmpty
    private final String token;
}
