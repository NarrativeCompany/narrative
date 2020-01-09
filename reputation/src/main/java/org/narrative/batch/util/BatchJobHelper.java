package org.narrative.batch.util;

import org.narrative.batch.config.BatchProperties;
import org.narrative.batch.config.IntervalJobProperties;
import org.narrative.batch.service.BatchJobControlService;
import org.slf4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.narrative.batch.util.JobShouldRunStep.*;

public class BatchJobHelper {
    public static final String JOB_UNIQUE_ID_KEY = "jobUniqueId";

    private final BatchJobControlService batchJobControlService;
    private final JobLauncher jobLauncher;

    public BatchJobHelper(BatchJobControlService batchJobControlService, JobLauncher jobLauncher) {
        this.batchJobControlService = batchJobControlService;
        this.jobLauncher = jobLauncher;
    }

    /**
     * Build a random offset so all jobs don't start simultaneously
     *
     * @return {@link Instant} representing the offset delay
     */
    public Instant buildRandomOffsetStartInstant(Instant lastInstant, BatchProperties batchProperties) {
        long delay = ThreadLocalRandom.current().nextLong(TimeUnit.MINUTES.toMillis(1), batchProperties.getMaxJobInitialExecutionDelay().toMillis());
        return lastInstant.plus(Duration.of(delay, ChronoUnit.MILLIS));
    }

    public void launchJob(Job job, Logger log) {
        String jobName = job.getName();
        JobParameters params = new JobParametersBuilder()
                .addLong(JOB_UNIQUE_ID_KEY, System.currentTimeMillis())
                .toJobParameters();
        try {
            JobExecution jobExecution = jobLauncher.run(job, params);
            log.info("Started batch job {} id: {} instance: {} status: {}",
                    jobName, jobExecution.getJobId(),
                    jobExecution.getJobInstance().getInstanceId(), jobExecution.getStatus());
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Job {} is already running.", jobName);
        } catch (JobRestartException e) {
            log.error("Job {} unable to be restarted.", jobName, e);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("Job {} can't be restarted - already completed successfully", e);
        } catch (JobParametersInvalidException e) {
            log.error("Job {} - invalid parameters", jobName, e);
        }
    }

    /**
     * Build a job execution listener for singleton jobs - i.e. jobs that should run on only one node.
     */
    public JobExecutionListener buildSingletonJobExecutionListener(String jobName, Supplier<Boolean> jobShouldRun, Logger log) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                boolean shouldRun = false;

                // Does the run condition check pass?
                if (jobShouldRun.get()) {
                    log.debug("jobShouldRun for job {} condition passed", jobName);

                    // Can we acquire ownership of the job?
                    if (batchJobControlService.acquireOwnershipOfSingletonJob(jobName, jobExecution.getJobId(), jobExecution.getJobInstance().getInstanceId(), jobExecution.getId())) {
                        log.debug("Job {} is owned by this node - starting", jobName);
                        shouldRun = true;
                    } else {
                        log.debug("Job {} ownership cannot be acquired by this node - stopping execution", jobName);
                    }
                } else {
                    log.debug("jobShouldRun for job {} condition failed - stopping execution", jobName);
                }

                if (shouldRun) {
                    jobExecution.getExecutionContext().put(SHOULD_RUN_KEY, true);
                } else {
                    jobExecution.getExecutionContext().put(SHOULD_RUN_KEY, false);
                    jobExecution.setExitStatus(ExitStatus.NOOP);
                }

                log.info("beforeJob {} id: {} status: {} shouldRun: {}",
                        jobName, jobExecution.getJobId(), jobExecution.getExitStatus().getExitCode(), shouldRun);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                batchJobControlService.completeOwnedSingletonJob(jobName, jobExecution.getStatus());
                log.info("afterJob {} id: {} status: {} failures: {}",
                        jobName, jobExecution.getJobId(), jobExecution.getExitStatus().getExitCode(),
                        jobExecution.getAllFailureExceptions().size());
            }
        };
    }

    /**
     * Build an execution listener for interval jobs - determine when job should run based on last time it ran
     */
    public JobExecutionListener buildSingletonIntervalJobExecutionListener(String jobName, IntervalJobProperties intervalJobProperties, Logger log) {
        return buildSingletonJobExecutionListener(jobName,
                buildIntervalExpiredFunction(jobName, intervalJobProperties),
                log);
    }

    public Supplier<Boolean> buildIntervalExpiredFunction(String jobName, IntervalJobProperties intervalJobProperties){
        return ()-> {
            Instant lastExecutionInstant = batchJobControlService.findLastJobExecutionStartInstant(jobName);
            return lastExecutionInstant.plus(intervalJobProperties.getJobInterval()).isBefore(Instant.now());
        };
    }
}
