package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.UserKycEventType;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;
import lombok.Builder;
import lombok.Value;

@JsonValueObject
@JsonTypeName("UserKyc")
@Value
@Builder(toBuilder = true)
public class UserKycDTO {
    private final OID oid;
    private final UserKycStatus kycStatus;
    private final UserKycEventType rejectedReasonEventType;

    private final KycPricingDTO kycPricing;
    private final PayPalCheckoutDetailsDTO payPalCheckoutDetails;
}
