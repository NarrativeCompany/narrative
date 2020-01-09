package org.narrative.reputation.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ReputationHistoryId implements Serializable {
        private static final long serialVersionUID = 3389752745371096643L;

        private long userOid;
        private RollupPeriod period;
        private LocalDate snapshotDate;
}
