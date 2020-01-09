package org.narrative.network.customizations.narrative.controller.postbody.publication;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.controller.postbody.file.FileUploadInputDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.CreatePublicationInput;

import javax.validation.constraints.NotNull;

/**
 * Date: 2019-07-31
 * Time: 12:43
 *
 * @author jonmark
 */
public class CreatePublicationInputDTO extends CreatePublicationInput {
    public CreatePublicationInputDTO(
            @JsonProperty(Fields.name) @NotNull String name,
            @JsonProperty(Fields.description) String description,
            @JsonProperty(Fields.agreedToAup) boolean agreedToAup,
            @JsonProperty(Fields.logo) FileUploadInputDTO logo
    ) {
        super(name, description, agreedToAup, logo);
    }
}
