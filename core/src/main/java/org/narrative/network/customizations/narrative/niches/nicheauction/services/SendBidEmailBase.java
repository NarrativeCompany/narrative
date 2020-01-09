package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.customizations.narrative.niches.services.SendBulkNarrativeEmailTaskBase;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/2/18
 * Time: 2:31 PM
 */
public class SendBidEmailBase extends SendBulkNarrativeEmailTaskBase {
    private NicheAuctionBid bid;

    protected SendBidEmailBase(NicheAuctionBid bid) {
        this.bid = bid;
    }

    @Override
    protected List<OID> getUserOidsToNotify() {
        // jw: by default, we want to email the person that created the bid for this task.
        return Collections.singletonList(bid.getBidder().getUser().getOid());
    }

    @Override
    protected void setupForChunk(List<User> users) {
        bid = NicheAuctionBid.dao().get(bid.getOid());
    }

    public NicheAuctionBid getBid() {
        return bid;
    }

    @Override
    public boolean isAlwaysSendEmail() {
        return true;
    }
}
