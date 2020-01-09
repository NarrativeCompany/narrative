package org.narrative.network.core.composition.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.CompositionStats;
import org.narrative.network.shared.daobase.CompositionDAOImpl;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Oct 6, 2006
 * Time: 3:01:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompositionStatsDAO extends CompositionDAOImpl<CompositionStats, OID> {
    public CompositionStatsDAO() {
        super(CompositionStats.class);
    }
}