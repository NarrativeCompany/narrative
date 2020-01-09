package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/2/18
 * Time: 3:08 PM
 */
public class SendLostAuctionEmail extends SendBidEmailBase {
    public SendLostAuctionEmail(NicheAuctionBid winningBid) {
        super(winningBid);
    }

    @Override
    protected List<OID> getUserOidsToNotify() {
        // jw: Get all bidders, and exclude the leading bidder since they will get the win email.
        List<OID> userOids = NicheAuctionBid.dao().getUserOidsOutbidOnAuction(getBid().getAuction());
        userOids.remove(getBid().getBidder().getUser().getOid());

        return userOids;
    }

    public Niche getNiche() {
        return getBid().getAuction().getNiche();
    }

    public NicheAuctionSecurityDeposit getSecurityDeposit() {
        // jw: this should only be called once in the JSP, and thus should be safe to fetch directly
        return NicheAuctionSecurityDeposit.dao().getSecurityDeposit(getBid().getAuction(), getUser());
    }
}
