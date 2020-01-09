package org.narrative.network.customizations.narrative.controller.postbody.rating;

import org.narrative.network.core.rating.QualityRating;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 2019-02-26
 * Time: 16:32
 *
 * @author brian
 */
@Value
@Validated
public class QualityRatingInputDTO {
    private final QualityRating rating;
    private final String reason;
}
