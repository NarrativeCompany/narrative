package org.narrative.network.customizations.narrative.controller.postbody.posts;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.narrative.network.customizations.narrative.service.api.model.input.PostTextInput;
import org.narrative.network.customizations.narrative.service.api.model.validators.ValidPostTextInput;

/**
 * Date: 2019-01-15
 * Time: 08:29
 *
 * @author jonmark
 */
@ValidPostTextInput
public class PostTextInputDTO extends PostTextInput {
    @JsonCreator
    public PostTextInputDTO(String title, String subTitle, String body, String canonicalUrl) {
        super(title, subTitle, body, canonicalUrl);
    }
}
