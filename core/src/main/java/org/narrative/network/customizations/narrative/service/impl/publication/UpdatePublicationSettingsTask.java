package org.narrative.network.customizations.narrative.service.impl.publication;

import org.narrative.common.util.ValidationHandler;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationSettings;
import org.narrative.network.customizations.narrative.publications.PublicationUrlType;
import org.narrative.network.customizations.narrative.publications.services.PublicationHeaderImageProcessor;
import org.narrative.network.customizations.narrative.publications.services.PublicationLogoProcessor;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationSettingsInput;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-23
 * Time: 15:33
 *
 * @author jonmark
 */
public class UpdatePublicationSettingsTask extends AreaTaskImpl<Object> {
    private static final Pattern FATHOM_SITE_ID_PATTERN = Pattern.compile("[A-Z]{5}|[A-Z]{8}");

    private final Publication publication;
    private final PublicationSettingsInput input;
    private final PublicationLogoProcessor logoProcessor;
    private final PublicationHeaderImageProcessor headerImageProcessor;

    public UpdatePublicationSettingsTask(Publication publication, PublicationSettingsInput input) {
        this.publication = publication;
        this.input = input;
        logoProcessor = new PublicationLogoProcessor(input.getLogo(), PublicationSettingsInput.Fields.logo, "updatePublicationSettingsTask.publicationLogo", publication);
        headerImageProcessor = new PublicationHeaderImageProcessor(input, publication);
    }

    @Override
    protected void validate(ValidationHandler handler) {
        CreatePublicationTask.validateCorePublicationFields(handler, input.getName(), input.getDescription());

        handler.validateNotNull(input.getHeaderImageAlignment(), PublicationSettingsInput.Fields.headerImageAlignment, "updatePublicationSettingsTask.headerAlignment");
        // jw: this does not appear to be documented, but the validateString that takes a pattern only does string optional
        //     checks. So this will only validate with the pattern if a value is specified.
        handler.validateString(input.getFathomSiteId(), FATHOM_SITE_ID_PATTERN, PublicationSettingsInput.Fields.fathomSiteId, "updatePublicationSettingsTask.fathomSiteId");

        // jw: let's validate all urls using the enum
        for (PublicationUrlType urlType : PublicationUrlType.values()) {
            urlType.validate(handler, input);
        }

        // jw: let's validate the uploaded images.
        logoProcessor.validate(handler);
        headerImageProcessor.validate(handler);

        // bl: only the owner can update the Rewards
        if(publication.isCurrentRoleOwner()) {
            if(handler.validateNotNull(input.getContentRewardWriterShare(), PublicationSettingsInput.Fields.contentRewardWriterShare, "updatePublicationSettingsTask.contentRewardWriterShare")) {
                // bl: if the writer share is not 100%, then the recipient must be specified
                if(!input.getContentRewardWriterShare().isOneHundredPercent()) {
                    handler.validateNotNull(input.getContentRewardRecipient(), PublicationSettingsInput.Fields.contentRewardRecipient, "updatePublicationSettingsTask.contentRewardRecipient");
                }
            }
        }
    }

    @Override
    protected Object doMonitoredTask() {
        PublicationSettings settings = publication.getSettings();

        publication.updateName(input.getName());
        publication.setDescription(input.getDescription());

        settings.setFathomSiteId(input.getFathomSiteId());
        settings.setHeaderImageAlignment(input.getHeaderImageAlignment());

        // jw: for each url type we need to get the URL and set it on the settings. Since we have already ran validation
        //     we can just set the urls into the settings.
        Map<PublicationUrlType, String> newUrls = new HashMap<>();
        for (PublicationUrlType urlType : PublicationUrlType.values()) {
            String url = urlType.getUrl(input.getUrls());

            if (!isEmpty(url)) {
                newUrls.put(urlType, url);
            }
        }
        settings.setUrlsByType(newUrls.isEmpty() ? null : newUrls);

        // jw: finally, let's apply the images
        logoProcessor.process();
        headerImageProcessor.process();

        // bl: only the owner can update the Rewards
        if(publication.isCurrentRoleOwner()) {
            publication.setContentRewardWriterShare(input.getContentRewardWriterShare());
            publication.setContentRewardRecipient(publication.getContentRewardWriterShare().isOneHundredPercent() ? null : input.getContentRewardRecipient());
        }

        return null;
    }
}
