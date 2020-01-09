package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * Date: 2019-08-28
 * Time: 08:35
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("PublicationUrls")
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PublicationUrlsDTO extends PublicationUrls {
    @Builder
    public PublicationUrlsDTO(String websiteUrl, String twitterUrl, String facebookUrl, String instagramUrl, String youtubeUrl, String snapchatUrl, String pinterestUrl, String linkedInUrl) {
        super(websiteUrl, twitterUrl, facebookUrl, instagramUrl, youtubeUrl, snapchatUrl, pinterestUrl, linkedInUrl);
    }
}
