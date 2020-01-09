package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Date: 10/12/18
 * Time: 11:25 AM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("ReferendumVoteGrouping")
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReferendumVoteGroupingDTO extends LoadMoreItemsBase<ReferendumVoteDTO> {
    private final Boolean votedFor;
    private final String lastVoterDisplayName;
    private final String lastVoterUsername;

    @Builder(toBuilder = true)
    public ReferendumVoteGroupingDTO(List<ReferendumVoteDTO> items, boolean hasMoreItems, Boolean votedFor, String lastVoterDisplayName, String lastVoterUsername) {
        super(items, hasMoreItems);
        this.votedFor = votedFor;
        this.lastVoterDisplayName = lastVoterDisplayName;
        this.lastVoterUsername = lastVoterUsername;
    }
}
