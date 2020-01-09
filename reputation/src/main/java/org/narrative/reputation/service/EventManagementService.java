package org.narrative.reputation.service;

import org.narrative.shared.event.Event;
import org.narrative.shared.event.EventProcessedEvent;

/**
 * Service for general management of events.
 */
public interface EventManagementService {
    /**
     * De-duplicate an incoming event.  This method will return true if it determines the message has already been processed.
     *
     * @param event The event of interest
     * @return false if the event has been deduplicated and should be processed - true otherwise
     */
    boolean isDuplicateEvent(Event event);

    /**
     * Poll for the next event from the specified queue name.
     */
    <T> T pollEventFromQueue(String queueName);

    /**
     * Mark the dedup row as processed and generate and publish a {@link EventProcessedEvent} indicating that the event
     * processing for a {@link Event} has completed successfully.
     *
     * @param eventProcessedEvent The event of interest
     */
    void markProcessedAndPublishEventProcessedEvent(EventProcessedEvent eventProcessedEvent);
}
