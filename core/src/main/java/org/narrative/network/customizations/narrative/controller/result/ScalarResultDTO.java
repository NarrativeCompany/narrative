package org.narrative.network.customizations.narrative.controller.result;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

/**
 * A DTO wrapper for scalar values
 */
@JsonValueObject
@JsonTypeName("ScalarResult")
@Value
@Builder(toBuilder = true)
public class ScalarResultDTO<T> {
    private final T value;
}
