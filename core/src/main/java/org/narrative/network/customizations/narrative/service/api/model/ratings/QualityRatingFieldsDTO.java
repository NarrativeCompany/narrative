package org.narrative.network.customizations.narrative.service.api.model.ratings;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.posts.QualityLevel;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-02-28
 * Time: 08:31
 *
 * @author jonmark
 */
@Data
@JsonValueObject
@JsonTypeName("QualityRatingFields")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldNameConstants
public class QualityRatingFieldsDTO extends RatingFieldsBaseDTO {
    private final QualityLevel qualityLevel;

    @Builder
    public QualityRatingFieldsDTO(int totalVoteCount, Integer score, QualityLevel qualityLevel) {
        super(totalVoteCount, score);
        this.qualityLevel = qualityLevel;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    public static class Fields extends RatingFieldsBaseDTO.Fields {}
}
