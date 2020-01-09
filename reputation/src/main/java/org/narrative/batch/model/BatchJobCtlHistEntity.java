package org.narrative.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.BatchStatus;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;

/**
 * History entity for batch job concurrency and tracking status.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BatchJobCtlHistEntity implements Serializable {
    private static final long serialVersionUID = 1797892543541258181L;

    @EmbeddedId
    @NotNull
    private BatchJobCtlHistId id;
    @NotNull
    private String host;
    @NotNull
    private Instant startTime;
    private Instant endTime;
    @NotNull
    @Enumerated(EnumType.STRING)
    private BatchStatus status;

    @NotNull
    public String getJobName() {
        return id.getJobName();
    }

    public @NotNull long getJobId() {
        return id.getJobId();
    }

    public @NotNull long getJobInstanceId() {
        return id.getJobInstanceId();
    }

    @NotNull
    public Long getJobExecutionId() {
        return id.getJobExecutionId();
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class BatchJobCtlHistId implements Serializable {
        private static final long serialVersionUID = 6557391069841958421L;

        @NotNull
        private String jobName;
        @NotNull
        private long jobId;
        @NotNull
        private long jobInstanceId;
        @NotNull
        private Long jobExecutionId;
    }
}
