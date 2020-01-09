package org.narrative.network.customizations.narrative.niches.nicheauction.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.niches.nicheauction.BidStatus;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
public class NicheAuctionBidDAO extends GlobalDAOImpl<NicheAuctionBid, OID> {
    public NicheAuctionBidDAO() {
        super(NicheAuctionBid.class);
    }

    public int updateStatusForAllBids(NicheAuction auction, AreaUserRlm bidder, BidStatus toStatus) {
        return getGSession().getNamedQuery("nicheAuctionBid.updateStatusForAllBids").setParameter("auction", auction).setParameter("bidder", bidder).setParameter("toStatus", toStatus).executeUpdate();
    }

    public long getTotalBidCount(NicheAuction auction) {
        return getCountForAllBy(new NameValuePair<>(NicheAuctionBid.FIELD__AUCTION__NAME, auction));
    }

    public List<OID> getUserOidsOutbidOnAuction(NicheAuction auction) {
        List<OID> areaUserOids = getGSession().getNamedQuery("nicheAuctionBid.getUserOidsOutbidOnAuction").setParameter("auction", auction).setParameter("outbidStatus", BidStatus.OUTBID).list();
        return AreaUser.dao().getUserOidsForAreaUserOids(areaUserOids);
    }

    public List<OID> getUserOidsWithActiveBidOnAuction(NicheAuction auction) {
        List<OID> areaUserOids = getGSession().getNamedQuery("nicheAuctionBid.getUserOidsWithActiveBidOnAuction").setParameter("auction", auction).setParameterList("activeStatuses", BidStatus.ACTIVE_STATUSES).list();
        return AreaUser.dao().getUserOidsForAreaUserOids(areaUserOids);
    }

    public NicheAuctionBid getLatestBidForUser(NicheAuction auction, AreaUserRlm bidder) {
        assert exists(auction) : "Should always call this method with a auction!";
        assert exists(bidder) : "Should always call this method with a bidder!";

        return (NicheAuctionBid) getGSession().getNamedQuery("nicheAuctionBid.getLatestBidForUser")
                .setParameter("auction", auction)
                .setParameter("bidder", bidder)
                .setMaxResults(1)
                .uniqueResult();
    }
}