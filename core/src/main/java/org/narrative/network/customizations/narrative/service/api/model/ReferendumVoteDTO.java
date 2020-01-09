package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVoteReason;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.sql.Timestamp;

@JsonValueObject
@JsonTypeName("ReferendumVote")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class ReferendumVoteDTO {
    private OID oid;
    private UserDTO voter;
    private Timestamp voteDatetime;
    private Boolean votedFor;
    // jw: due to floating point accuracy concerns we need to return this value as Strings with two decimal precision.
    private String votePoints;
    private ReferendumVoteReason reason;
    private OID commentOid;
    private String comment;
}
