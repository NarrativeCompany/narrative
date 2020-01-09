package org.narrative.reputation.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.narrative.shared.event.reputation.ConductEventType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "conductStatus_userOid_idx", columnList = "userOid"),
        @Index(name = "conductStatus_eventTimestamp_idx", columnList = "eventTimestamp")})
public class ConductStatusEntity {
    @Column(columnDefinition = "binary(16)")
    @Id
    private UUID eventId;
    @NotNull
    private long userOid;
    @NotNull
    private Instant eventTimestamp;
    @NotNull
    private ConductEventType conductEventType;
}
