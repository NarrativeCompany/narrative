package org.narrative.network.customizations.narrative.controller.postbody.publication;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.common.web.HorizontalAlignment;
import org.narrative.network.customizations.narrative.controller.postbody.file.FileUploadInputDTO;
import org.narrative.network.customizations.narrative.publications.PublicationContentRewardRecipientType;
import org.narrative.network.customizations.narrative.publications.PublicationContentRewardWriterShare;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationSettingsInput;

import javax.validation.constraints.NotNull;

/**
 * Date: 2019-08-23
 * Time: 14:08
 *
 * @author jonmark
 */
public class PublicationSettingsInputDTO extends PublicationSettingsInput {
    public PublicationSettingsInputDTO(
            @JsonProperty(Fields.name) @NotNull String name,
            @JsonProperty(Fields.description) @NotNull String description,
            @JsonProperty(Fields.fathomSiteId) String fathomSiteId,
            @JsonProperty(Fields.logo) @NotNull FileUploadInputDTO logo,
            @JsonProperty(Fields.headerImage) @NotNull FileUploadInputDTO headerImage,
            @JsonProperty(Fields.headerImageAlignment) @NotNull HorizontalAlignment headerImageAlignment,
            @JsonProperty(Fields.urls) @NotNull PublicationUrlsInputDTO urls,
            @JsonProperty(Fields.contentRewardWriterShare) PublicationContentRewardWriterShare contentRewardWriterShare,
            @JsonProperty(Fields.contentRewardRecipient) PublicationContentRewardRecipientType contentRewardRecipient
    ) {
        super(name, description, fathomSiteId, logo, headerImage, headerImageAlignment, urls, contentRewardWriterShare, contentRewardRecipient);
    }
}
