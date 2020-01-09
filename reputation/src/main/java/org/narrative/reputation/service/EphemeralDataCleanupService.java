package org.narrative.reputation.service;

/**
 * Helpers for periodically cleaning up ephemeral batch job and event processing data.
 */
public interface EphemeralDataCleanupService {
    /**
     * Purge the event dedup table of old, completed event rows.
     */
    void purgeEventDedupTable();

    /**
     * Purge the batch job control history of old event rows.
     */
    void purgeBatchJobControlHistoryTable();

    /**
     * Purge old Spring Batch job data.
     */
    void purgeSpringBatchJobData();


    /**
     * Purge old conduct status events
     */
    void purgeOldConductStatusEvents();
}
