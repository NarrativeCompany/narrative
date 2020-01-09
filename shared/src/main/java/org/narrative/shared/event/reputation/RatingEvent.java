package org.narrative.shared.event.reputation;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class RatingEvent extends UserEvent implements Serializable {
    private static final long serialVersionUID = -4567175830486733481L;
    private boolean ratedWithConsensus;
    private boolean revote;
    private boolean removeVote;
    private boolean wasRatedWithConsensus;

    @Builder
    public RatingEvent(Instant eventTimestamp, long userOid, boolean ratedWithConsensus, boolean revote, boolean removeVote, boolean wasRatedWithConsensus) {
        super(eventTimestamp, userOid);
        this.ratedWithConsensus = ratedWithConsensus;
        this.revote = revote;
        this.removeVote = removeVote;
        this.wasRatedWithConsensus = wasRatedWithConsensus;
    }
}
