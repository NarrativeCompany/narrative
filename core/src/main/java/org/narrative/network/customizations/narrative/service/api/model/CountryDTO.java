package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Value;

import java.io.Serializable;

@JsonValueObject
@JsonTypeName("Country")
@Value
public class CountryDTO implements Serializable {
    private final String countryCode;
    private final String countryName;
}
