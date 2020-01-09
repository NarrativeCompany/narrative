package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.services.SendBulkNarrativeEmailTaskBase;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/2/18
 * Time: 3:08 PM
 */
public class SendAuctionExpiringEmail extends SendBulkNarrativeEmailTaskBase {
    NicheAuction auction;

    public SendAuctionExpiringEmail(NicheAuction auction) {
        this.auction = auction;
    }

    @Override
    protected List<OID> getUserOidsToNotify() {
        // jw: Get all watchers
        return FollowedChannel.dao().getUserOidsFollowing(auction.getNiche().getChannel());
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
