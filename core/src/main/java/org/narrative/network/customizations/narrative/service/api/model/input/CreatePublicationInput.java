package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.customizations.narrative.controller.postbody.file.FileUploadInputDTO;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Date: 2019-07-31
 * Time: 12:41
 *
 * @author jonmark
 */
@Data
@Validated
@FieldNameConstants
public class CreatePublicationInput {
    @NotNull
    private final String name;
    private final String description;

    private final boolean agreedToAup;

    @NotNull
    private final FileUploadInputDTO logo;
}
