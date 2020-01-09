package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.customizations.narrative.niches.services.SendBulkNarrativeEmailTaskBase;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/30/18
 * Time: 12:09 PM
 */
public class SendAuctionEndedEmail extends SendBulkNarrativeEmailTaskBase {
    NicheAuction auction;

    public SendAuctionEndedEmail(NicheAuction auction) {
        this.auction = auction;
    }

    @Override
    protected List<OID> getUserOidsToNotify() {
        // jw: let's start with the people are watching this auction
        List<OID> watchers = FollowedChannel.dao().getUserOidsFollowing(auction.getNiche().getChannel());

        // jw: subtract the person who won, since they will receive the invoice email!
        watchers.remove(auction.getLeadingBid().getBidder().getUser().getOid());

        // jw: remove the people who have been outbid, since they would have received the lost auction email
        watchers.removeAll(NicheAuctionBid.dao().getUserOidsOutbidOnAuction(auction));

        // jw: and we are done!
        return watchers;
    }

    @Override
    protected void setupForChunk(List<User> users) {
        auction = NicheAuction.dao().get(auction.getOid());
    }

    @Override
    public boolean isAlwaysSendEmail() {
        return true;
    }

    public Niche getNiche() {
        return auction.getNiche();
    }

    public NicheAuction getAuction() {
        return auction;
    }
}
