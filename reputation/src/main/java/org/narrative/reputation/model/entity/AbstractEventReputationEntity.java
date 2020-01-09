package org.narrative.reputation.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AbstractEventReputationEntity extends AbstractReputationEntity{
    @Column(columnDefinition = "binary(16)")
    private UUID lastEventId;

    private Instant lastEventTimestamp;

    public AbstractEventReputationEntity(long userOid, Instant lastUpdated, UUID lastEventId, Instant lastEventTimestamp) {
        super(userOid, lastUpdated);
        this.lastEventId = lastEventId;
        this.lastEventTimestamp = lastEventTimestamp;
    }
}
