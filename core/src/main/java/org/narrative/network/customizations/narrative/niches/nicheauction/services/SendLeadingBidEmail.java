package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/2/18
 * Time: 2:31 PM
 */
public class SendLeadingBidEmail extends SendBidEmailBase {
    private NicheAuctionBid inResponseToBid;
    private final boolean forActiveBidder;

    public SendLeadingBidEmail(NicheAuctionBid bid, NicheAuctionBid inResponseToBid, AreaUserRlm currentBidder) {
        super(bid);
        this.inResponseToBid = inResponseToBid;
        this.forActiveBidder = isEqual(bid.getBidder(), currentBidder);
    }

    @Override
    protected void setupForChunk(List<User> users) {
        super.setupForChunk(users);

        if (inResponseToBid != null) {
            inResponseToBid = NicheAuctionBid.dao().get(inResponseToBid.getOid());
        }
    }

    public NicheAuctionBid getInResponseToBid() {
        return inResponseToBid;
    }

    public boolean isForActiveBidder() {
        return forActiveBidder;
    }
}
