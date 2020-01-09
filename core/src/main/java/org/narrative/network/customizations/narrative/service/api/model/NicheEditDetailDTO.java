package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

@JsonValueObject
@JsonTypeName("NicheEditDetail")
@Value
@Validated
@Builder(toBuilder = true)
public class NicheEditDetailDTO {
    private final String newName;
    private final String newDescription;
    private final String originalName;
    private final String originalDescription;
}
