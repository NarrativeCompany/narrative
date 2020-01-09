package org.narrative.reputation.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(indexes = {@Index(name = "si_FollowerQualityEntity_jobIdUserOid", columnList="batchJobId, userOid")})
public class FollowerQualityEntity extends AbstractReputationEntity {
    @Column(columnDefinition = "double default 0.0")
    private double userQualityFollowerRatio;
    @Column(columnDefinition = "double default 0.0")
    private double userQualityFollowerPctRank;
    private long batchJobId;

    @Builder
    public FollowerQualityEntity(long userOid, Instant lastUpdated, double userQualityFollowerRatio, double userQualityFollowerPctRank, long batchJobId) {
        super(userOid, lastUpdated);
        this.userQualityFollowerRatio = userQualityFollowerRatio;
        this.userQualityFollowerPctRank = userQualityFollowerPctRank;
        this.batchJobId = batchJobId;
    }
}
