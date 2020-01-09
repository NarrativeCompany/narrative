package org.narrative.network.customizations.narrative.service.impl.kyc;

import org.narrative.network.core.locations.Country;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.codec.digest.DigestUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Helper DTO for document properties
 */
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class DocCheckUserProps {

    @NotEmpty
    private final String firstName;
    @NotEmpty
    private final String lastName;
    @NotEmpty
    private final Country country;
    @NotEmpty
    private final String documentNumber;
    @NotNull
    private final LocalDate birthDate;

    String calculateHash() {
        String hashInput =
                firstName.toLowerCase() + "_" +
                lastName.toLowerCase() + "_" +
                country.getCountryCode() + "_" +
                birthDate + "_" +
                documentNumber.toLowerCase();
        return DigestUtils.sha512Hex(hashInput);
    }
}
