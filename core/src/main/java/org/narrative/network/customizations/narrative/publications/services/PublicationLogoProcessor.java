package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.api.model.input.FileUploadInput;

/**
 * Date: 2019-08-28
 * Time: 20:44
 *
 * @author jonmark
 */
public class PublicationLogoProcessor extends PublicationImageProcessor {
    // jw: ideally we would trim this down, but since it is used on two seperate forms from two different inputs I am going
    //     to keep the constructor the same and use it as a pass-through.
    public PublicationLogoProcessor(FileUploadInput input, String fieldName, String fieldWordletName, Publication publication) {
        super(input, fieldName, fieldWordletName, publication);
    }

    @Override
    protected ImageOnDisk getExistingFile() {
        return getConsumer().getLogo();
    }

    @Override
    protected void updateFile(ImageOnDisk logo) {
        getConsumer().setLogo(logo);
    }
}
