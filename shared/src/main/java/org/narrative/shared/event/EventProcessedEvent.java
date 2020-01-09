package org.narrative.shared.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Value
public class EventProcessedEvent implements Event, Serializable {
    private static final long serialVersionUID = 3743928948842769755L;
    private static final String QUEUE_NAME = EventType.QUEUE_NAME_PREFIX + EventProcessedEvent.class.getSimpleName();
    public static final EventType EVENT_TYPE = new EventProcessedEventType();

    @NonNull
    private final UUID eventId;
    private final Instant eventTimestamp;
    private final boolean successful;

    public EventProcessedEvent(@NonNull UUID eventId) {
        this.eventId = eventId;
        this.successful = true;
        this.eventTimestamp = Instant.now();
    }

    @Builder
    @JsonCreator
    public EventProcessedEvent(@NonNull @JsonProperty("eventId") UUID eventId, @JsonProperty("eventTimestamp") Instant eventTimestamp, boolean successful) {
        this.eventId = eventId;
        this.successful = successful;
        this.eventTimestamp = eventTimestamp != null ? eventTimestamp : Instant.now();
    }

    /**
     * Determine an event type based on an event class of the same type.
     *
     * @return Event type determined from event class
     */
    @JsonIgnore
    @Override
    public EventType getEventType() {
        return EVENT_TYPE;
    }

    public static class EventProcessedEventType implements EventType<EventProcessedEventType, EventProcessedEvent> {
        /**
         * Return the event queue name for this type.
         *
         * @return The event queue name
         */
        @Override
        public String getEventQueueName() {
            return QUEUE_NAME;
        }
    }
}
