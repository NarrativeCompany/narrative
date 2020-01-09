package org.narrative.network.core.security.area.community.advanced.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.security.area.community.advanced.AreaResource;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: Mar 5, 2009
 * Time: 8:58:39 AM
 *
 * @author brian
 */
public class AreaResourceDAO extends GlobalDAOImpl<AreaResource, OID> {
    public AreaResourceDAO() {
        super(AreaResource.class);
    }

}
