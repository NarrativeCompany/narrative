package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Value object representing a the Current User's format preferences.
 */
@JsonValueObject
@JsonTypeName("FormatPreferences")
@Value
@Builder(toBuilder = true)
public class FormatPreferencesDTO {
    private final TimeZone timeZone;
    private final Locale locale;
    // jw: this needs to be a ISO BCP 47 formatted local. See:
    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl#Locale_identification_and_negotiation
    private final String localeForNumber;

}
