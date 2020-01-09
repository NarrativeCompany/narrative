package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationSettingsInput;

/**
 * Date: 2019-08-28
 * Time: 20:44
 *
 * @author jonmark
 */
public class PublicationHeaderImageProcessor extends PublicationImageProcessor {
    public PublicationHeaderImageProcessor(PublicationSettingsInput input, Publication publication) {
        super(input.getHeaderImage(), PublicationSettingsInput.Fields.headerImage, "publicationHeaderImageProcessor.headerImage", publication);
    }

    @Override
    protected ImageOnDisk getExistingFile() {
        return getConsumer().getHeaderImage();
    }

    @Override
    protected void updateFile(ImageOnDisk headerImage) {
        getConsumer().setHeaderImage(headerImage);
    }
}
