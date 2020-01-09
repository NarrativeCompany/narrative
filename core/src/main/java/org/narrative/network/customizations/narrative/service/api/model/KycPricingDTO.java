package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

/**
 * Date: 2019-02-11
 * Time: 10:13
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("KycPricing")
@Value
@Builder(toBuilder = true)
public class KycPricingDTO {
    private final UsdValue initialPrice;
    private final UsdValue retryPrice;

    private final UsdValue kycPromoPrice;
    private final String kycPromoMessage;
}
