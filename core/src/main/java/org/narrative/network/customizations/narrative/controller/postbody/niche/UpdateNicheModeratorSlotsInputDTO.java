package org.narrative.network.customizations.narrative.controller.postbody.niche;

import org.narrative.network.customizations.narrative.niches.niche.Niche;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 11/20/18
 * Time: 8:57 AM
 *
 * @author brian
 */
@Value
@Validated
@FieldNameConstants
public class UpdateNicheModeratorSlotsInputDTO {
    @Range(min = 1, max= Niche.MAXIMUM_MODERATOR_SLOTS)
    private final int moderatorSlots;
}
