package org.narrative.shared.event.reputation;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.narrative.shared.event.Event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ReputationEvent implements Event<ReputationEventType, ReputationEvent>, Serializable {
    private static final long serialVersionUID = -5482632303534482840L;
    private final UUID eventId = UUID.randomUUID();

    private Instant eventTimestamp;

    public ReputationEvent(Instant eventTimestamp) {
        // bl: ideally this would use Builder.Default, but it's not possible with subclasses here without @SuperBuilder
        // refer to #2558 for why we are not using @SuperBuilder.
        this.eventTimestamp = eventTimestamp == null ? Instant.now() : eventTimestamp;
    }

    @Override
    public ReputationEventType getEventType() {
        //noinspection unchecked
        return ReputationEventType.getEventType(((Class<ReputationEvent>) getClass()));
    }
}