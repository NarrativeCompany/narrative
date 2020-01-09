package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * DTO for transporting a two factor authentication secret and correpsonding QR code to the SPA
 */
@JsonValueObject
@JsonTypeName("TwoFactorSecret")
@Value
@Builder(toBuilder = true)
public class TwoFactorSecretDTO {
    private final String secret;
    private final String qrCodeImage;
    private final List<Integer> backupCodes;
}
