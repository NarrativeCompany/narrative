package org.narrative.network.customizations.narrative.service.api.model.ratings;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-03-04
 * Time: 07:42
 *
 * @author jonmark
 */
@Data
@EqualsAndHashCode()
@ToString(callSuper = true)
@FieldNameConstants
public class RatingFieldsBaseDTO {
    private final int totalVoteCount;
    private final Integer score;

    public RatingFieldsBaseDTO(int totalVoteCount, Integer score) {
        this.totalVoteCount = totalVoteCount;
        this.score = score;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields {}
}
