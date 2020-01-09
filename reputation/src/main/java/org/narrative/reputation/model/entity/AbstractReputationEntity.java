package org.narrative.reputation.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
@NoArgsConstructor
@Data
public class AbstractReputationEntity {

    @Id
    private long userOid;

    @UpdateTimestamp
    private Instant lastUpdated;

    public AbstractReputationEntity(long userOid, Instant lastUpdated) {
        this.userOid = userOid;
        this.lastUpdated = lastUpdated;
    }
}
