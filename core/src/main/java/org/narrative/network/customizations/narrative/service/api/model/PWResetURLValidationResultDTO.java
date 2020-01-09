package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

@JsonValueObject
@JsonTypeName("PWResetURLValidationResult")
@Value
@Builder(toBuilder = true)
public class PWResetURLValidationResultDTO {
    boolean valid;
    boolean expired;
    boolean twoFactorEnabled;
}
