package org.narrative.network.core.composition.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.ReplyStats;
import org.narrative.network.shared.daobase.CompositionDAOImpl;

/**
 * Date: May 4, 2010
 * Time: 6:05:13 PM
 *
 * @author brian
 */
public class ReplyStatsDAO extends CompositionDAOImpl<ReplyStats, OID> {
    public ReplyStatsDAO() {
        super(ReplyStats.class);
    }
}
