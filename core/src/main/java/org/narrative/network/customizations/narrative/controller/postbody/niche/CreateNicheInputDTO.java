package org.narrative.network.customizations.narrative.controller.postbody.niche;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.CreateNicheRequest;

/**
 * Create niche request DTO.
 */
public class CreateNicheInputDTO extends CreateNicheRequest {
    @JsonCreator
    public CreateNicheInputDTO(@JsonProperty(Fields.name) String name,
                               @JsonProperty(Fields.description) String description,
                               @JsonProperty(Fields.assertChecked) boolean assertChecked,
                               @JsonProperty(Fields.agreeChecked) boolean agreeChecked) {
        super(name, description, assertChecked, agreeChecked);
    }
}
