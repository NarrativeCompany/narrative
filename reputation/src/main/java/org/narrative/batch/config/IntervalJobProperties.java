package org.narrative.batch.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Spring batch properties for interval jobs
 */
@Data
@Validated
public class IntervalJobProperties {
    /**
     * Minimum interval between job executions
     */
    @NotNull
    private Duration jobInterval = Duration.of(2, ChronoUnit.HOURS);
}
