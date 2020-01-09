package org.narrative.network.customizations.narrative.controller.postbody.posts;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.service.api.model.input.PostInput;
import org.narrative.network.customizations.narrative.service.api.model.validators.ValidPostInput;

import java.util.List;

/**
 * Date: 2019-01-04
 * Time: 14:47
 *
 * @author jonmark
 */
@ValidPostInput
public class PostInputDTO extends PostInput {
    @JsonCreator
    public PostInputDTO(boolean draft, String title, String subTitle, String body, String canonicalUrl, OID publishToPrimaryChannel, boolean allowComments, boolean ageRestricted, List<OID> publishToNiches) {
        super(draft, title, subTitle, body, canonicalUrl, publishToPrimaryChannel, allowComments, ageRestricted, publishToNiches);
    }
}
