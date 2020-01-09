package org.narrative.network.customizations.narrative.controller.postbody.publication;

import org.narrative.network.customizations.narrative.publications.PublicationRole;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * Date: 9/12/19
 * Time: 4:11 PM
 *
 * @author brian
 */
@Value
@Validated
@FieldNameConstants
public class InvitePublicationPowerUserInputDTO {
    @NotEmpty
    private final Set<PublicationRole> roles;
}
