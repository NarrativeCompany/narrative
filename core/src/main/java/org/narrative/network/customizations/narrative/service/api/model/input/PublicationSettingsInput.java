package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.common.web.HorizontalAlignment;
import org.narrative.network.customizations.narrative.publications.PublicationContentRewardRecipientType;
import org.narrative.network.customizations.narrative.publications.PublicationContentRewardWriterShare;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 2019-08-23
 * Time: 12:33
 *
 * @author jonmark
 */
@Data
@Validated
@FieldNameConstants
public class PublicationSettingsInput {
    private final String name;
    private final String description;

    private final String fathomSiteId;

    private final FileUploadInput logo;
    private final FileUploadInput headerImage;
    private final HorizontalAlignment headerImageAlignment;

    private final PublicationUrlsInput urls;

    private final PublicationContentRewardWriterShare contentRewardWriterShare;
    private final PublicationContentRewardRecipientType contentRewardRecipient;

    public PublicationSettingsInput(String name, String description, String fathomSiteId, FileUploadInput logo, FileUploadInput headerImage, HorizontalAlignment headerImageAlignment, PublicationUrlsInput urls, PublicationContentRewardWriterShare contentRewardWriterShare, PublicationContentRewardRecipientType contentRewardRecipient) {
        this.name = name;
        this.description = description;
        this.fathomSiteId = fathomSiteId;
        this.logo = logo;
        this.headerImage = headerImage;
        this.headerImageAlignment = headerImageAlignment;
        this.urls = urls;
        this.contentRewardWriterShare = contentRewardWriterShare;
        this.contentRewardRecipient = contentRewardRecipient;
    }
}
