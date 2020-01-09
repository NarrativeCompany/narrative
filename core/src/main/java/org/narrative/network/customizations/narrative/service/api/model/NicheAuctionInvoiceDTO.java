package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

/**
 * Value object representing a niche auction invoice.
 */
@JsonValueObject
@JsonTypeName("NicheAuctionInvoice")
@Value
@Builder(toBuilder = true)
public class NicheAuctionInvoiceDTO {
    private final OID oid;

    private final NicheAuctionDTO auction;
    private final NicheAuctionBidDTO bid;
}
