package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@JsonValueObject
@JsonTypeName("ReferendumVotes")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class ReferendumVotesDTO {
    private final int totalVotes;
    // jw: due to floating point accuracy concerns we need to return these values as Strings with two decimal precision.
    private final String votePointsFor;
    private final String votePointsAgainst;
    private final ReferendumVoteGroupingDTO recentVotesFor;
    private final ReferendumVoteGroupingDTO recentVotesAgainst;
    private final ReferendumVoteGroupingDTO tribunalMembersYetToVote;
}