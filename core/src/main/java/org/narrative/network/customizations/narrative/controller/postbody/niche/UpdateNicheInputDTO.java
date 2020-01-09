package org.narrative.network.customizations.narrative.controller.postbody.niche;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.NicheInputBase;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

/**
 * Update niche request DTO.
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Validated
public class UpdateNicheInputDTO extends NicheInputBase {
    @Builder
    @JsonCreator
    public UpdateNicheInputDTO(@JsonProperty(Fields.name) String name, @JsonProperty(Fields.description) String description) {
        super(name, description);
    }
}
