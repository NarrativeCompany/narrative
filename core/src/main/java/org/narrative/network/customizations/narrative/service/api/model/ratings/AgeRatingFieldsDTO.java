package org.narrative.network.customizations.narrative.service.api.model.ratings;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-02-28
 * Time: 08:34
 *
 * @author jonmark
 */
@Data
@JsonValueObject
@JsonTypeName("AgeRatingFields")
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AgeRatingFieldsDTO extends RatingFieldsBaseDTO {
    private final AgeRating ageRating;

    @Builder
    public AgeRatingFieldsDTO(int totalVoteCount, Integer score, AgeRating ageRating) {
        super(totalVoteCount, score);
        this.ageRating = ageRating;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    public static class Fields extends RatingFieldsBaseDTO.Fields {}
}
