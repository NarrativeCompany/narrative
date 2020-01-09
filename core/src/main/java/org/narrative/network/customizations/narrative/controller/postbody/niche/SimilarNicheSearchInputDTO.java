package org.narrative.network.customizations.narrative.controller.postbody.niche;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.SimilarNicheSearchRequest;
import org.springframework.validation.annotation.Validated;

/**
 * Similar niche search request DTO.
 */
@Validated
public class SimilarNicheSearchInputDTO extends SimilarNicheSearchRequest {
    @JsonCreator
    public SimilarNicheSearchInputDTO(@JsonProperty(Fields.name) String name, @JsonProperty(Fields.description) String description) {
        super(name, description);
    }
}
