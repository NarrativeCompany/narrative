package org.narrative.network.core.area.user.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.user.AreaUserStats;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 2, 2005
 * Time: 5:47:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class AreaUserStatsDAO extends GlobalDAOImpl<AreaUserStats, OID> {
    public AreaUserStatsDAO() {
        super(AreaUserStats.class);
    }

}
