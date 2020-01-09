package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.niches.nicheauction.BidStatus;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 10/24/18
 * Time: 12:46 PM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("NicheAuctionDetail")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class NicheAuctionDetailDTO {
    private final NicheAuctionDTO auction;
    private final BidStatus currentUserLatestBidStatus;
    private final NrveUsdValue currentUserLatestMaxNrveBid;

    private OID currentUserActiveInvoiceOid;

    private Boolean currentUserBypassesSecurityDepositRequirement;
    private PayPalCheckoutDetailsDTO securityDepositPayPalCheckoutDetails;
}
