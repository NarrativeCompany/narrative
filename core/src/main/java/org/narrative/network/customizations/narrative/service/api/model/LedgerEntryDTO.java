package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.publications.PublicationPaymentType;
import org.narrative.network.customizations.narrative.publications.PublicationPlanType;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

/**
 * Value object representing a ledger entry.
 */
@JsonValueObject
@JsonTypeName("LedgerEntry")
@Value
@Builder
@FieldNameConstants
public class LedgerEntryDTO {
    private final OID oid;
    private final UserDTO actor;
    private final LedgerEntryType type;
    private final Instant eventDatetime;

    private final NicheDTO niche;
    private final PublicationDTO publication;

    private final NicheAuctionDTO auction;
    private final NicheAuctionBidDTO auctionBid;

    private final TribunalIssueDTO tribunalIssue;
    private final TribunalIssueReportDTO tribunalIssueReport;

    private final ElectionDTO election;

    private final InvoiceDTO invoice;

    private final ReferendumDTO referendum;
    private final Boolean wasReferendumVotedFor;

    private final OID postOid;
    private final PostDTO post;
    private final OID commentOid;
    private final UserDTO author;

    private final PublicationPaymentType publicationPaymentType;
    private final PublicationPlanType publicationPlan;

    private final UsdValue securityDepositValue;
}
