package org.narrative.network.customizations.narrative.service.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-08-26
 * Time: 13:43
 *
 * @author jonmark
 */
@Getter
@RequiredArgsConstructor
@FieldNameConstants
public class PublicationUrls {
    private final String websiteUrl;
    private final String twitterUrl;
    private final String facebookUrl;
    private final String instagramUrl;
    private final String youtubeUrl;
    private final String snapchatUrl;
    private final String pinterestUrl;
    private final String linkedInUrl;
}
