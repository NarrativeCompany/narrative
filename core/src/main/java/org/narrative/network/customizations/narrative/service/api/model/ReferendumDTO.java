package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.sql.Timestamp;

@JsonValueObject
@JsonTypeName("Referendum")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class ReferendumDTO {
    private OID oid;
    private ReferendumType type;
    private final Timestamp startDatetime;
    private final Timestamp endDatetime;
    // jw: due to floating point accuracy concerns we need to return these values as Strings with two decimal precision.
    private final String votePointsFor;
    private final String votePointsAgainst;
    private final int commentCount;
    private final NicheDTO niche;
    private final PublicationDTO publication;
    private final DeletedChannelDTO deletedChannel;
    private final boolean open;
    private final ReferendumVoteDTO currentUserVote;
}
