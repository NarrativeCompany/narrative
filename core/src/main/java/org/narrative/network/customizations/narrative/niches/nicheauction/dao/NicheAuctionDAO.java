package org.narrative.network.customizations.narrative.niches.nicheauction.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
public class NicheAuctionDAO extends GlobalDAOImpl<NicheAuction, OID> {
    public NicheAuctionDAO() {
        super(NicheAuction.class);
    }

    public Map<NicheAuction, Long> getBidCountsForAuctions(Collection<NicheAuction> auctions) {
        Collection<ObjectPair<NicheAuction, Long>> data = getGSession().getNamedQuery("nicheAuction.getBidCountsForAuctions").setParameterList("auctions", auctions).list();
        return ObjectPair.getAsMap(data);
    }

    public Map<NicheAuction, Long> getBidCountsForActiveAuctions(List<Niche> niches) {
        Collection<ObjectPair<NicheAuction, Long>> data = getGSession().getNamedQuery("nicheAuction.getBidCountsForActiveAuctions").setParameterList("niches", niches).list();
        return ObjectPair.getAsMap(data);
    }
}