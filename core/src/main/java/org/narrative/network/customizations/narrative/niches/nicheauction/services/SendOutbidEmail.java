package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/2/18
 * Time: 2:11 PM
 * <p>
 * jw: Since this task and the leadingBidEmail task both have variants based on whether they are automatic emails in
 * response to another members bid, lets extend this one from SendLeadingBidEmail so we can inherit that behavior.
 */
public class SendOutbidEmail extends SendLeadingBidEmail {
    public SendOutbidEmail(NicheAuctionBid bid, NicheAuctionBid inResponseToBid, AreaUserRlm currentBidder) {
        super(bid, inResponseToBid, currentBidder);
    }
}
