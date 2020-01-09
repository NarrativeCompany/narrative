package org.narrative.shared.event.reputation;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class ConductStatusEvent extends UserEvent implements Serializable {
    private static final long serialVersionUID = 5862340029271195025L;
    private ConductEventType conductEventType;

    @Builder
    public ConductStatusEvent(Instant eventTimestamp, long userOid, ConductEventType conductEventType) {
        super(eventTimestamp, userOid);
        this.conductEventType = conductEventType;
    }
}
