package org.narrative.network.core.area.base.dao;

import org.narrative.common.persistence.*;
import org.narrative.network.core.area.base.RoleContentPageView;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Date: 2019-03-19
 * Time: 09:07
 *
 * @author brian
 */
public class RoleContentPageViewDAO extends GlobalDAOImpl<RoleContentPageView, OID> {
    public RoleContentPageViewDAO() {
        super(RoleContentPageView.class);
    }

    public boolean hasRoleViewedRecently(String roleId, OID contentOid) {
        return getGSession().getNamedQuery("roleContentPageView.getOneForRoleContentAfterDate")
                .setParameter("roleId", roleId)
                .setParameter("contentOid", contentOid)
                .setParameter("afterDate", Instant.now().minus(RoleContentPageView.DAYS_OF_HISTORY, ChronoUnit.DAYS))
                .setMaxResults(1)
                .uniqueResult()!=null;
    }

    public void deleteOldPageViews() {
        getGSession().getNamedQuery("roleContentPageView.deleteOldPageViews")
                .setParameter("beforeDate", Instant.now().minus(RoleContentPageView.DAYS_OF_HISTORY, ChronoUnit.DAYS))
                .executeUpdate();
    }
}
