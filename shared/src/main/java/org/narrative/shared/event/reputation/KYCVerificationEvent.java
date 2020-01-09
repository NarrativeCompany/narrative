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
public class KYCVerificationEvent extends UserEvent implements Serializable {
    private static final long serialVersionUID = -8645728439876666389L;
    private boolean isVerified;

    @Builder
    public KYCVerificationEvent(Instant eventTimestamp, long userOid, boolean isVerified) {
        super(eventTimestamp, userOid);
        this.isVerified = isVerified;
    }
}
