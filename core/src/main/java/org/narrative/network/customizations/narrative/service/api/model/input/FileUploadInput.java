package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 8/26/19
 * Time: 3:32 PM
 *
 * @author brian
 */
@Data
@FieldNameConstants
@Validated
public class FileUploadInput {
    private final TempFileUploadInput tempFile;
    private final boolean remove;
}
