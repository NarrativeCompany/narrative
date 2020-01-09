package org.narrative.shared.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Interface for events.
 *
 * @param <ET> The event type for the event class
 * @param <EC> The event class
 */
public interface Event<ET extends EventType, EC> extends Serializable {
    public static final String SER_TYPE_FIELD = "_type";

    UUID getEventId();
    Instant getEventTimestamp();

    /**
     * Determine an event type based on an event class of the same type.
     *
     * @return Event type determined from event class
     */
    @JsonIgnore
    ET getEventType();
}
