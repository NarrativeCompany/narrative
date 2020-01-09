package org.narrative.network.customizations.narrative.controller.postbody.auction;

import org.narrative.network.customizations.narrative.controller.postbody.currency.NrveUsdPriceInputDTO;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Date: 10/24/18
 * Time: 7:13 PM
 *
 * @author jonmark
 */
@Value
@Builder
@Validated
@FieldNameConstants
public class NicheAuctionBidInputDTO {
    @NotNull
    private final BigDecimal maxNrveBid;
    @NotNull
    private final NrveUsdPriceInputDTO nrveUsdPrice;
}
