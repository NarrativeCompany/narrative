package org.narrative.network.core.area.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.AreaStats;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Jun 6, 2006
 * Time: 11:53:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class AreaStatsDAO extends GlobalDAOImpl<AreaStats, OID> {
    public AreaStatsDAO() {
        super(AreaStats.class);
    }
}
