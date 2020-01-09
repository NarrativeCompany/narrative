package org.narrative.batch.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Spring batch properties
 */
@Data
@Validated
public class BatchProperties {
    /**
     * Executor thread count for execution of batch jobs
     */
    @NotNull
    private Integer maxThreadPoolSize = 10;

    /**
     * Effective timeout for a running job.  This controls when a running job can be restarted by another node.
     */
    @NotNull
    private Duration maxJobDuration = Duration.of(4, ChronoUnit.HOURS);

    /**
     * How long should the job history be kept around?
     */
    @NotNull
    private Duration jobHistoryRetention = Duration.of(30, ChronoUnit.DAYS);

    /**
     * Max random offset duration when scheduling jobs.
     */
    @NotNull
    private Duration maxJobInitialExecutionDelay = Duration.of(2, ChronoUnit.MINUTES);
}
