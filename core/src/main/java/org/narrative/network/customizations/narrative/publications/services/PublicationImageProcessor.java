package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.api.model.input.FileUploadInput;
import org.narrative.network.customizations.narrative.service.impl.file.FileUploadInputProcessor;

/**
 * Date: 2019-08-28
 * Time: 20:41
 *
 * @author jonmark
 */
public abstract class PublicationImageProcessor extends FileUploadInputProcessor<ImageOnDisk, Publication> {
    public PublicationImageProcessor(FileUploadInput input, String fieldName, String fieldWordletName, Publication publication) {
        super(input, fieldName, fieldWordletName, publication);
    }
}
