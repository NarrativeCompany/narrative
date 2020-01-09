package org.narrative.network.customizations.narrative.controller.postbody.posts;

import org.narrative.network.customizations.narrative.posts.FeaturePostDuration;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Date: 2019-01-04
 * Time: 15:17
 *
 * @author jonmark
 */
@Value
@Validated
public class FeaturedPostInputDTO {
    @NotNull
    private final FeaturePostDuration duration;
}
