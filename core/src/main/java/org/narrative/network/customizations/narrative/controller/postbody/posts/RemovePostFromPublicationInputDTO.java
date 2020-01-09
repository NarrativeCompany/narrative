package org.narrative.network.customizations.narrative.controller.postbody.posts;

import lombok.Value;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 9/18/19
 * Time: 8:17 AM
 *
 * @author brian
 */
@Value
@Validated
public class RemovePostFromPublicationInputDTO {
    private final String message;
}
