package org.narrative.network.customizations.narrative.reputation.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.reputation.UserReputation;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.time.Instant;
import java.util.List;

/**
 * Date: 2018-12-14
 * Time: 09:28
 *
 * @author brian
 */
public class UserReputationDAO extends GlobalDAOImpl<UserReputation, OID> {
    public UserReputationDAO() {
        super(UserReputation.class);
    }

    public List<OID> getUserOidsWhoBecameConductNeutralInWindow(Instant startTimestamp, Instant endTimestamp) {
        return getGSession().getNamedQuery("userReputation.getUserOidsWhoBecameConductNeutralInWindow")
                .setParameter("startTimestamp", startTimestamp)
                .setParameter("endTimestamp", endTimestamp)
                .list();
    }
}
