package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.customizations.narrative.service.api.model.kyc.KycIdentificationType;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Value
@Validated
@FieldNameConstants
public class KycApplicantInput {
    @NotNull
    private final KycIdentificationType kycIdentificationType;
}
