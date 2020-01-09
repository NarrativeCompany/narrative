package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.customizations.narrative.NrveValue;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Date: 2019-07-01
 * Time: 15:59
 *
 * @author jonmark
 */
@Data
@Validated
@FieldNameConstants
public class RequestRedemptionInput extends UpdateProfileAccountConfirmationInputBase {
    private final NrveValue redemptionAmount;
    private final NrveUsdPriceInput nrveUsdPrice;

    public RequestRedemptionInput(
            @NotEmpty String currentPassword,
            Integer twoFactorAuthCode,
            @NotNull NrveValue redemptionAmount,
            @NotNull NrveUsdPriceInput nrveUsdPrice
    ) {
        super(currentPassword, twoFactorAuthCode);
        this.redemptionAmount = redemptionAmount;
        this.nrveUsdPrice = nrveUsdPrice;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    public static class Fields extends UpdateProfileAccountConfirmationInputBase.Fields {}
}
