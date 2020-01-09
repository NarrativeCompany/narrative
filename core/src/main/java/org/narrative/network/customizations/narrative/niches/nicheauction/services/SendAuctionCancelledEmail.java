package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.customizations.narrative.niches.services.SendBulkNarrativeEmailTaskBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 4/4/18
 * Time: 10:43 AM
 */
public class SendAuctionCancelledEmail extends SendBulkNarrativeEmailTaskBase {
    NicheAuction auction;

    public SendAuctionCancelledEmail(NicheAuction auction) {
        this.auction = auction;
    }

    @Override
    protected List<OID> getUserOidsToNotify() {
        // jw: let's run this through a set to ensure we do not get any duplicates.
        Set<OID> watchers = new HashSet<>();

        // jw: let's start with the people are watching this auction
        watchers.addAll(FollowedChannel.dao().getUserOidsFollowing(auction.getNiche().getChannel()));
        // jw: then, lets add anyone who had bid on the auction.
        watchers.addAll(NicheAuctionBid.dao().getUserOidsWithActiveBidOnAuction(auction));

        // jw: and we are done!
        return new ArrayList<>(watchers);
    }

    @Override
    protected void setupForChunk(List<User> users) {
        auction = NicheAuction.dao().get(auction.getOid());
    }

    public Niche getNiche() {
        return auction.getNiche();
    }

    public NicheAuction getAuction() {
        return auction;
    }
}
