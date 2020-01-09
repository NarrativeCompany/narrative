package org.narrative.network.core.security.area.community.advanced.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.security.area.community.advanced.AreaCircleUser;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/10/16
 * Time: 10:03 AM
 */
public class AreaCircleUserDAO extends GlobalDAOImpl<AreaCircleUser, OID> {
    public AreaCircleUserDAO() {
        super(AreaCircleUser.class);
    }

}
