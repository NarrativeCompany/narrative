package org.narrative.shared.event.reputation;

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
public class LikeEvent extends UserEvent implements Serializable {
    private static final long serialVersionUID = -1123408461296343505L;
    private LikeEventType likeEventType;
    private double likePoints;

    public LikeEvent(Instant eventTimestamp, long userOid, LikeEventType likeEventType, double likePoints) {
        super(eventTimestamp, userOid);
        this.likeEventType = likeEventType;
        this.likePoints = likePoints;
    }
}
