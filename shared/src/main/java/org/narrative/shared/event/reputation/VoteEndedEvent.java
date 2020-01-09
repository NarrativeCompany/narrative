package org.narrative.shared.event.reputation;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class VoteEndedEvent extends ReputationEvent implements BulkUserEvent, Serializable {
    private static final long serialVersionUID = 2629074847712551048L;
    private long referendumId;
    private DecisionEnum decision;
    private Map<Long, DecisionEnum> userVotesMap;

    @Builder
    public VoteEndedEvent(Instant eventTimestamp, long referendumId, DecisionEnum decision, Map<Long, DecisionEnum> userVotesMap) {
        super(eventTimestamp);
        this.referendumId = referendumId;
        this.decision = decision;
        this.userVotesMap = userVotesMap;
    }

    @Override
    public Set<Long> getUserOidSet() {
        return userVotesMap != null ? userVotesMap.keySet() : Collections.emptySet();
    }
}
