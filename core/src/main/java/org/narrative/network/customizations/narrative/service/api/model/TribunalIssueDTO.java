package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueStatus;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.sql.Timestamp;

@JsonValueObject
@JsonTypeName("TribunalIssue")
@Value
@Builder(toBuilder = true)
public class TribunalIssueDTO {
    private final OID oid;
    private final TribunalIssueType type;
    private final TribunalIssueStatus status;
    private final Timestamp creationDatetime;
    private final ReferendumDTO referendum;
    private final TribunalIssueReportDTO lastReport;
    private final NicheEditDetailDTO nicheEditDetail;
}
