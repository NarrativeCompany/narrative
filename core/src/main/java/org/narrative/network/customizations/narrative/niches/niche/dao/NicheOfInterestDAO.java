package org.narrative.network.customizations.narrative.niches.niche.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.niches.niche.NicheOfInterest;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.List;

public class NicheOfInterestDAO extends GlobalDAOImpl<NicheOfInterest, OID> {
    public NicheOfInterestDAO() {
        super(NicheOfInterest.class);
    }

    public List<NicheOfInterest> getNichesOfInterest() {
        return getGSession().getNamedQuery("nicheOfInterest.getNichesOfInterest")
                .setParameter("activeStatus", NicheStatus.ACTIVE)
                .setCacheable(true)
                .list();
    }

}
