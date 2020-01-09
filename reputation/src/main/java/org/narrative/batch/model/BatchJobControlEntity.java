package org.narrative.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.BatchStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;

/**
 * Entity for for controlling batch job concurrency and tracking job status.
 */
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BatchJobControlEntity implements Serializable {
    private static final long serialVersionUID = -828484949598197444L;

    @Id
    private String jobName;
    @NotNull
    private Long jobId;
    @NotNull
    private Long jobInstanceId;
    @NotNull
    private Long jobExecutionId;
    @NotNull
    private String host;
    @NotNull
    private Instant startTime;
    private Instant endTime;
    @NotNull
    @Enumerated(EnumType.STRING)
    private BatchStatus status;
    @Version
    private long version;
}
