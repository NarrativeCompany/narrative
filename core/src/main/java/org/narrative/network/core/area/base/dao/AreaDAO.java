package org.narrative.network.core.area.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 30, 2005
 * Time: 8:25:59 AM
 */
public class AreaDAO extends GlobalDAOImpl<Area, OID> {

    public AreaDAO() {
        super(Area.class);
    }

    public Area getNarrativePlatformArea() {
        return getGSession().createNamedQuery("area.getNarrativePlatformArea", Area.class)
                .setCacheable(true)
                .uniqueResult();
    }

    public OID getNarrativePlatformAreaOid() {
        return getGSession().createNamedQuery("area.getNarrativePlatformAreaOid", OID.class)
                .setCacheable(true)
                .uniqueResult();
    }
}
