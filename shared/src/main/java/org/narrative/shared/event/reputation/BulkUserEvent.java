package org.narrative.shared.event.reputation;

import java.util.Set;

/**
 * Interface for bulk user events
 */
public interface BulkUserEvent {
    /**
     * Get the set of user OIDs for this event.
     *
     * @return The set of user OIDs for this event
     */
    Set<Long> getUserOidSet();
}
