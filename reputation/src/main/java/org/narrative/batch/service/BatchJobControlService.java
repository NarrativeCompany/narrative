package org.narrative.batch.service;

import org.springframework.batch.core.BatchStatus;

import java.time.Instant;

/**
 * Helper for Spring Batch job control
 */
public interface BatchJobControlService {

    /**
     * Is the specified job an execution candidate?  This will check if:
     *
     * a) The job is not running or if running but timed out
     * b) The job is in a restartable state
     *
     * @param jobName The job name of interest
     * @return If a) or b) are true then true otherwise false
     */
    boolean isSingletonJobExecutionCandidate(String jobName);

    /**
     * Find the last job execution timestamp for the specified job.
     *
     * @param jobName The job name of interest
     * @return An {@link Instant} representing the last job execution start timestamp.  Method will return an
     * {@link Instant} representing the epoch if the job has never executed.
     */
    Instant findLastJobExecutionStartInstant(String jobName);

    /**
     * Find the last successful job execution timestamp for the specified job.
     *
     * @param jobName The job name of interest
     * @return An {@link Instant} representing the last job execution start timestamp.  Method will return an
     * {@link Instant} representing the epoch if the job has never executed.
     */
    Instant findLastSuccessfulJobExecutionStartInstant(String jobName);

    /**
     * Acquire ownership of a singleton job.
     *
     * @param jobName The job name
     * @param jobId         The job id
     * @param jobInstanceId The job instance id
     * @param jobExecutionId The job execution id
     * @return true if this process now owns the job, false otherwise
     */
    boolean acquireOwnershipOfSingletonJob(String jobName, long jobId, long jobInstanceId, long jobExecutionId);

    /**
     * Complete a singleton job and release ownership.
     *
     * @param jobName       The name of the job
     * @param jobStatus     The resulting job status
     */
    void completeOwnedSingletonJob(String jobName, BatchStatus jobStatus);
}
