package org.narrative.network.customizations.narrative.controller.postbody.redemption;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.controller.postbody.currency.NrveUsdPriceInputDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.RequestRedemptionInput;

/**
 * Date: 2019-07-01
 * Time: 16:00
 *
 * @author jonmark
 */
public class RequestRedemptionInputDTO extends RequestRedemptionInput {
    @JsonCreator
    public RequestRedemptionInputDTO(
            @JsonProperty(Fields.currentPassword) String currentPassword,
            @JsonProperty(Fields.twoFactorAuthCode) Integer twoFactorAuthCode,
            @JsonProperty(Fields.redemptionAmount) NrveValue redemptionAmount,
            @JsonProperty(Fields.nrveUsdPrice) NrveUsdPriceInputDTO nrveUsdPrice
    ) {
        super(currentPassword, twoFactorAuthCode, redemptionAmount, nrveUsdPrice);
    }
}
