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
public class ContentLikeEvent extends LikeEvent implements Serializable {
    private static final long serialVersionUID = -6716424973070658370L;

    @Builder
    public ContentLikeEvent(Instant eventTimestamp, long userOid, LikeEventType likeEventType, double likePoints) {
        super(eventTimestamp, userOid, likeEventType, likePoints);
    }
}
