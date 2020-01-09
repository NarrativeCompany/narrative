package org.narrative.shared.event;

import java.io.Serializable;

/**
 * Interface for event types typically implemented by an Enum.
 * @param <ET> The event type for the event class
 * @param <EC> The event class
 */
public interface EventType<ET, EC> extends Serializable {
    public static final String QUEUE_NAME_PREFIX = "eventQueue_";

    /**
     * Return the event queue name for this type.
     *
     * @return The event queue name
     */
    String getEventQueueName();
}
