package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/28/18
 * Time: 3:33 PM
 */
public class StartAuctionPaymentPeriodTask extends AreaTaskImpl<Object> {
    private final NicheAuction auction;

    public StartAuctionPaymentPeriodTask(NicheAuction auction) {
        this.auction = auction;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: once the payment period begins, we need to flag the niche as pending payment.
        auction.getNiche().updateStatus(NicheStatus.PENDING_PAYMENT);

        auction.saveLedgerEntry(auction.getLeadingBid(), LedgerEntryType.NICHE_AUCTION_ENDED);

        auction.saveLedgerEntry(auction.getLeadingBid(), LedgerEntryType.NICHE_AUCTION_WON, 1);

        // jw: let's process all refunds for security deposits made by losing bidders.
        getAreaContext().doAreaTask(new RefundNicheAuctionLosingBiddersTask(auction.getLeadingBid()));

        getAreaContext().doAreaTask(new InvoiceLeadingBidderTask(auction));

        // jw: since we are just starting the payment period, lets send the failure email to all bidders.
        getAreaContext().doAreaTask(new SendLostAuctionEmail(auction.getLeadingBid()));

        // jw: similar to above, lets email all followers about the auction ending.
        getAreaContext().doAreaTask(new SendAuctionEndedEmail(auction));

        return null;
    }
}
