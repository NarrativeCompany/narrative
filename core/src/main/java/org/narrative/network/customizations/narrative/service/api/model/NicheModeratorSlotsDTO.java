package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 11/19/18
 * Time: 11:53 AM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("NicheModeratorSlots")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class NicheModeratorSlotsDTO {
    private final NicheDTO niche;
    private final ElectionDTO activeModeratorElection;
    private final int moderatorSlots;
}
