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
public class UserEvent extends ReputationEvent implements Serializable {
    private static final long serialVersionUID = 2643323182381910021L;
    private long userOid;

    public UserEvent(Instant eventTimestamp, long userOid) {
        super(eventTimestamp);
        this.userOid = userOid;
    }
}
