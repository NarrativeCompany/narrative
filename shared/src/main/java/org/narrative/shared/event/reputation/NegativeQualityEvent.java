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
public class NegativeQualityEvent extends UserEvent implements Serializable {
    private static final long serialVersionUID = -1470459803326701615L;
    private NegativeQualityEventType negativeQualityEventType;

    @Builder
    public NegativeQualityEvent(Instant eventTimestamp, long userOid, NegativeQualityEventType negativeQualityEventType) {
        super(eventTimestamp, userOid);
        this.negativeQualityEventType = negativeQualityEventType;
    }
}

