package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.customizations.narrative.service.api.model.PublicationUrls;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 2019-08-28
 * Time: 08:38
 *
 * @author jonmark
 */
@Data
@Validated
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PublicationUrlsInput extends PublicationUrls {
    public PublicationUrlsInput(String websiteUrl, String twitterUrl, String facebookUrl, String instagramUrl, String youtubeUrl, String snapchatUrl, String pinterestUrl, String linkedInUrl) {
        super(websiteUrl, twitterUrl, facebookUrl, instagramUrl, youtubeUrl, snapchatUrl, pinterestUrl, linkedInUrl);
    }
}
