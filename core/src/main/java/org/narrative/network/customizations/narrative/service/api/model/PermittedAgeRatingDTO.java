package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.util.Set;

@JsonValueObject
@JsonTypeName("PermittedAgeRating")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class PermittedAgeRatingDTO {
    private final Set<AgeRating> permittedAgeRatings;
}
