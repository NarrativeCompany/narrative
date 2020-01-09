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
 * Date: 3/30/18
 * Time: 12:14 PM
 */
public class SendAuctionFailureEmail extends SendBulkNarrativeEmailTaskBase {
    NicheAuction originalAuction;
    NicheAuction newAuction;

    public SendAuctionFailureEmail(NicheAuction originalAuction, NicheAuction newAuction) {
        this.originalAuction = originalAuction;
        this.newAuction = newAuction;
    }

    @Override
    protected List<OID> getUserOidsToNotify() {
        // jw: notify anyone who is following the original auction.
        //     note: it's important that this task is ran before we move the watches to the new auction.
        return FollowedChannel.dao().getUserOidsFollowing(originalAuction.getNiche().getChannel());
    }

    @Override
    protected void setupForChunk(List<User> users) {
        originalAuction = NicheAuction.dao().get(originalAuction.getOid());
        newAuction = NicheAuction.dao().get(newAuction.getOid());
    }

    @Override
    public boolean isAlwaysSendEmail() {
        return true;
    }

    public Niche getNiche() {
        return originalAuction.getNiche();
    }

    public NicheAuction getOriginalAuction() {
        return originalAuction;
    }

    public NicheAuction getNewAuction() {
        return newAuction;
    }
}
