package org.narrative.network.customizations.narrative.niches.elections.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.elections.ElectionStatus;
import org.narrative.network.customizations.narrative.niches.elections.NicheModeratorElection;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.hibernate.Query;

import java.util.List;

/**
 * Date: 11/12/18
 * Time: 10:17 AM
 *
 * @author jonmark
 */
public class NicheModeratorElectionDAO extends GlobalDAOImpl<NicheModeratorElection, OID> {
    public NicheModeratorElectionDAO() {
        super(NicheModeratorElection.class);
    }

    public long getElectionCount(ElectionStatus status) {
        Query q = getGSession().getNamedQuery("nicheModeratorElection.getElectionCount");
        return (Long) q.setParameter("status", status)
                .uniqueResult();
    }

    public List<NicheModeratorElection> getElections(ElectionStatus status, int page, int count) {
        Query q = getGSession().getNamedQuery("nicheModeratorElection.getElections");
        return q.setParameter("status", status)
                .setFirstResult(Math.max(0, page - 1) * count)
                .setMaxResults(count)
                .list();
    }
}
