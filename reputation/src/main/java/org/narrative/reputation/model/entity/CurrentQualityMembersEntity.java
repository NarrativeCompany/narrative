package org.narrative.reputation.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentQualityMembersEntity {
    @Id
    private int id;
    private double meanReputationScore;
    private long totalQualityMembers;
    @NotNull
    private Instant lastUpdated;
}
