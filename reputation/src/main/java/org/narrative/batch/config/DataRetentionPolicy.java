package org.narrative.batch.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
@AllArgsConstructor
@Validated
public class DataRetentionPolicy {
    /**
     * Execution interval
     */
    @NotNull
    private Duration jobInterval = Duration.of(12, ChronoUnit.HOURS);
    /**
     * Retention duration for non-errored data
     */
    private Duration retentionDuration = Duration.of(30, ChronoUnit.DAYS);
    /**
     * Retention duration for errored data
     */
    private Duration erroredRetentionDuration = Duration.of(90, ChronoUnit.DAYS);
}
