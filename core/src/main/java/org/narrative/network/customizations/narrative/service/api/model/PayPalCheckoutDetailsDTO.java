package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-02-04
 * Time: 22:34
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("PayPalCheckoutDetails")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class PayPalCheckoutDetailsDTO {
    private final String clientMode;
    private final String clientId;
    private final String amountForPayPal;
    private final UsdValue usdAmount;
}
