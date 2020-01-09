package org.narrative.reputation.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity used for de-duplicating incoming messages
 */
@Entity
@Data
@NoArgsConstructor
public class EventDedupEntity {
    @Id
    @Column(columnDefinition = "binary(16)", updatable = false)
    private UUID uuid;
    @Version
    private Long version;
    private Instant lockTimestamp;
    private Integer retryAttempt = 0;
    private boolean processed = false;
    private Instant processedTimestamp;

    @Builder
    public EventDedupEntity(UUID uuid, Long version, Instant lockTimestamp) {
        this.uuid = uuid;
        this.version = version;
        this.lockTimestamp = lockTimestamp != null ? lockTimestamp : Instant.now();
    }
}
