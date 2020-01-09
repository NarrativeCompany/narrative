package org.narrative.network.customizations.narrative.reputation;

import org.narrative.common.persistence.OID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;

import java.util.Date;

/**
 * This is a global view into the reputation database's ReputationHistoryEntity.
 *
 * Date: 2018-12-15
 * Time: 09:24
 *
 * @author brian
 */
@Entity
@Immutable
@Getter
@NoArgsConstructor
public class UserReputationHistory {
    @Id
    private OID userOid;

    private int period;
    private Date snapshotDate;
    private int totalScore;
}
