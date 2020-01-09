package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.sql.Timestamp;

/**
 * Value object representing a niche auction.
 */
@JsonValueObject
@JsonTypeName("NicheAuction")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class NicheAuctionDTO {
    private final OID oid;
    private final NicheDTO niche;
    private final boolean openForBidding;
    private final Timestamp startDatetime;
    private final Timestamp endDatetime;
    private final NicheAuctionBidDTO leadingBid;
    private final long totalBidCount;
    private final NrveUsdValue startingBid;
    private final NrveUsdPriceDTO nrveUsdPrice;
    private final Boolean currentRoleOutbid;
}
