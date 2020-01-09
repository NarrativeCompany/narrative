package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonValueObject
@JsonTypeName("NicheDetail")
@Value
@Builder(toBuilder = true)
public class NicheDetailDTO {
    private final NicheDTO niche;

    private OID activeAuctionOid;
    private OID activeModeratorElectionOid;
    private OID currentUserActiveInvoiceOid;
    private OID currentBallotBoxReferendumOid;
    private List<OID> currentTribunalAppealOids;
    private List<TribunalIssueType> availableTribunalIssueTypes;

}
