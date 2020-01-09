package org.narrative.network.core.security.area.community.advanced.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: Mar 5, 2009
 * Time: 8:54:29 AM
 *
 * @author brian
 */
public class AreaCircleDAO extends GlobalDAOImpl<AreaCircle, OID> {
    public AreaCircleDAO() {
        super(AreaCircle.class);
    }

}
