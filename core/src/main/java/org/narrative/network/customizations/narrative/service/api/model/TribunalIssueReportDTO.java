package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

import java.sql.Timestamp;

@JsonValueObject
@JsonTypeName("TribunalIssueReport")
@Value
@Validated
@Builder(toBuilder = true)
public class TribunalIssueReportDTO {
    private final OID oid;
    private final UserDTO reporter;
    private final String comments;
    private final Timestamp creationDatetime;
}