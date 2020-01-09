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
public class ConsensusChangedEvent extends ReputationEvent implements BulkUserEvent, Serializable {
    private static final long serialVersionUID = 32204433511039030L;
    private Map<Long, Boolean> usersConsensusChangedMap;

    @Builder
    public ConsensusChangedEvent(Instant eventTimestamp, Map<Long, Boolean> usersConsensusChangedMap) {
        super(eventTimestamp);
        this.usersConsensusChangedMap = usersConsensusChangedMap;
    }

    @Override
    public Set<Long> getUserOidSet() {
        return usersConsensusChangedMap != null ? usersConsensusChangedMap.keySet() : Collections.emptySet();
    }
}
