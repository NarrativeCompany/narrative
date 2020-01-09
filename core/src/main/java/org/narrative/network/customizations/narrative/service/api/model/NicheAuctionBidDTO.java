package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.niches.nicheauction.BidStatus;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Value object representing an niche auction bid.
 */
@JsonValueObject
@JsonTypeName("NicheAuctionBid")
@Value
@Builder(toBuilder = true)
public class NicheAuctionBidDTO {
    private final OID oid;
    private final UserDTO bidder;
    private final BidStatus status;
    private final NrveUsdValue bidAmount;
    private final Instant bidDatetime;
}
