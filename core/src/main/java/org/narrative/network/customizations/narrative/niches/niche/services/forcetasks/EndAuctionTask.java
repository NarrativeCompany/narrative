package org.narrative.network.customizations.narrative.niches.niche.services.forcetasks;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.ProcessAuctionBeginningPaymentPeriodJob;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.ProcessAuctionExpiringJob;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.StartAuctionPaymentPeriodTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/23/18
 * Time: 10:37 AM
 */
public class EndAuctionTask extends AreaTaskImpl<Object> {
    private final NicheAuction auction;

    public EndAuctionTask(NicheAuction auction) {
        this.auction = auction;
    }

    @Override
    protected Object doMonitoredTask() {
        if(NetworkRegistry.getInstance().isProductionServer()) {
            throw UnexpectedError.getRuntimeException("Can't end Niche auction on production environments!");
        }

        // jw: if we have a auction, it is not already being invoiced, and it has at least one bid, then let's allow the niche auction to be ended early.

        // jw: also require a leading bid before we allow it to be terminated early
        if(!auction.isOpenForBidding()) {
            throw UnexpectedError.getRuntimeException("Can only end Niche auction if it's open for bidding!");
        }

        if(exists(auction.getActiveInvoice())) {
            throw UnexpectedError.getRuntimeException("Can't end Niche auction if it already has an active invoice!");
        }

        if(!exists(auction.getLeadingBid())) {
            throw UnexpectedError.getRuntimeException("Should always have a leading bidder at this point! auction/" + auction.getOid());
        }

        // jw: unschedule the auction ending job for this auction.
        ProcessAuctionBeginningPaymentPeriodJob.unschedule(auction);

        // jw: now that the job has been unscheduled, let's go ahead and run the task to invoice it manually:
        getAreaContext().doAreaTask(new StartAuctionPaymentPeriodTask(auction));

        // jw: to end the auction prematurely, we need to set the end datetime.
        auction.setEndDatetime(now());

        //mk: unschedule the auction expiring reminder job
        ProcessAuctionExpiringJob.unschedule(auction);

        return null;
    }
}
