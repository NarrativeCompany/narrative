package org.narrative.network.customizations.narrative.controller.postbody.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.controller.postbody.tempfile.TempFileUploadInputDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.FileUploadInput;

/**
 * Date: 8/26/19
 * Time: 3:33 PM
 *
 * @author brian
 */
public class FileUploadInputDTO extends FileUploadInput {
    public FileUploadInputDTO(
            @JsonProperty(Fields.tempFile) TempFileUploadInputDTO tempFile,
            @JsonProperty(Fields.remove) boolean remove
    ) {
        super(tempFile, remove);
    }
}
