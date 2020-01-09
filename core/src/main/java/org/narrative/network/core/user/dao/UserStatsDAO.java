package org.narrative.network.core.user.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.UserStats;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 2, 2005
 * Time: 3:55:10 PM
 */
public class UserStatsDAO extends GlobalDAOImpl<UserStats, OID> {
    public UserStatsDAO() {
        super(UserStats.class);
    }

}
