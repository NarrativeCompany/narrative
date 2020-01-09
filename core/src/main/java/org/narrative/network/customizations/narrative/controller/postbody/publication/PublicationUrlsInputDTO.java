package org.narrative.network.customizations.narrative.controller.postbody.publication;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationUrlsInput;

/**
 * Date: 2019-08-28
 * Time: 08:41
 *
 * @author jonmark
 */
public class PublicationUrlsInputDTO extends PublicationUrlsInput {
    public PublicationUrlsInputDTO(
            @JsonProperty(Fields.websiteUrl) String websiteUrl,
            @JsonProperty(Fields.twitterUrl) String twitterUrl,
            @JsonProperty(Fields.facebookUrl) String facebookUrl,
            @JsonProperty(Fields.instagramUrl) String instagramUrl,
            @JsonProperty(Fields.youtubeUrl) String youtubeUrl,
            @JsonProperty(Fields.snapchatUrl) String snapchatUrl,
            @JsonProperty(Fields.pinterestUrl) String pinterestUrl,
            @JsonProperty(Fields.linkedInUrl) String linkedInUrl
    ) {
        super(websiteUrl, twitterUrl, facebookUrl, instagramUrl, youtubeUrl, snapchatUrl, pinterestUrl, linkedInUrl);
    }
}
