package org.narrative.network.core.content.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.content.base.ContentStats;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: Dec 2, 2005
 * Time: 3:14:51 PM
 *
 * @author Brian
 */
public class ContentStatsDAO extends GlobalDAOImpl<ContentStats, OID> {
    public ContentStatsDAO() {
        super(ContentStats.class);
    }

}