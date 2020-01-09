package org.narrative.batch.service;

import java.time.Instant;

/**
 * Clean old Spring Batch job metadata from the DB
 */
public interface BatchJobMetadataPurgeService {
    /**
     * Purge job metadata.
     *
     * @param olderThanInstant    Purge everything older than this instant
     * @param purgeUnfinishedJobs If true purge all job status values, otherwise only jobs with status ==
     *                            {@link org.springframework.batch.core.BatchStatus#COMPLETED}
     * @return Count of jobs removed
     */
    int purgeJobMetadata(Instant olderThanInstant, boolean purgeUnfinishedJobs);
}
