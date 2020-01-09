package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.AssertTrue;

/**
 * Date: 9/28/18
 * Time: 3:12 PM
 *
 * @author brian
 */
@Data
@Validated
@FieldNameConstants
public class UserRenewTosAgreementInput {
    @AssertTrue
    private final boolean hasAgreedToTos;
}
