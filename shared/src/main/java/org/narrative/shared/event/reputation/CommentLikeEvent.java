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
public class CommentLikeEvent extends LikeEvent implements Serializable {
    private static final long serialVersionUID = 1168907776096458322L;

    @Builder
    public CommentLikeEvent(Instant eventTimestamp, long userOid, LikeEventType likeEventType, double likePoints) {
        super(eventTimestamp, userOid, likeEventType, likePoints);
    }
}
