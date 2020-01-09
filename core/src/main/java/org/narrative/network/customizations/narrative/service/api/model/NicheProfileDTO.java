package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 11/19/18
 * Time: 3:33 PM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("NicheProfile")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class NicheProfileDTO {
    // jw: include the niche so that we can drive more basic elements of the UI without having to fetch it independently.
    private final NicheDTO niche;
    private final TribunalIssueDTO editDetailsTribunalIssue;
}
