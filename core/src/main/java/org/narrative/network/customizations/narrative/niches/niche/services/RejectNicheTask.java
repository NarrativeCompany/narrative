package org.narrative.network.customizations.narrative.niches.niche.services;

import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.posts.services.RemoveAllPostsFromChannelTask;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.services.CancelInvoiceTask;
import org.narrative.network.customizations.narrative.niches.elections.services.CancelNicheModeratorElectionTask;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheUserAssociation;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.ProcessAuctionBeginningPaymentPeriodJob;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.ProcessAuctionExpiringJob;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.SendAuctionCancelledEmail;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/15/18
 * Time: 10:30 AM
 */
public class RejectNicheTask extends NicheTaskBase {
    private final TribunalIssue dueToIssue;

    public RejectNicheTask(Niche niche, TribunalIssue dueToIssue) {
        super(niche);

        this.dueToIssue = dueToIssue;
    }

    @Override
    protected void processNiche(Niche niche) {
        Timestamp updateDatetime = now();
        User originalOwner = null;
        if (niche.getStatus().isActive()) {
            assert exists(dueToIssue) && dueToIssue.getType().isRatifyNiche() : "Should only reject active Niches due to TribunalIssue! niche/" + niche.getOid() + " tribunalIssue/" + (exists(dueToIssue) ? dueToIssue.getType() : null);
            AreaUserRlm owner = niche.getOwner();
            assert exists(owner) : "Should always have an owner when a Niche is approved!";
            originalOwner = owner.getUser();
            niche.setOwner(null);
            // bl: mark the purchaseInvoice as requiring a prorated refund so that we can easily identify it at the end of the month
            Invoice invoice = niche.getChannel().refundPurchaseInvoice();
            updateDatetime = invoice.getUpdateDatetime();

            // jw: ensure that we remove the association from the user.
            owner.removeExpectedNicheAssociation(niche, true);

            // jw: if we have a active moderator election let's cancel it.
            if (exists(niche.getActiveModeratorElection())) {
                getAreaContext().doAreaTask(new CancelNicheModeratorElectionTask(niche));
            }
            // jw: since this niche has been rejected, let's reset the moderatorSlots back to the default.
            niche.updateModeratorSlots(Niche.DEFAULT_MODERATOR_SLOT_COUNT);

            getAreaContext().doAreaTask(new RemoveAllPostsFromChannelTask(niche.getChannel(), false));

            // todo:moderator-voting: We need to notify and remove all moderators who are currently elected.

        }

        // jw: we need to clear all associations to the niche, to free up those slots for those users.
        List<NicheUserAssociation> associations = new ArrayList<>(niche.getUserAssociations());
        for (NicheUserAssociation association : associations) {
            NicheUserAssociation.dao().delete(association);
        }
        // jw: just to be safe, lets remove the associations from the niche as well
        niche.setUserAssociations(new LinkedList<>());

        // jw: handle any active auctions on the niche, if present
        if (exists(niche.getActiveAuction())) {
            // jw: lets close the current auction now that the niche has been rejected (since rejection could potentially happen during the bidding period).
            NicheAuction auction = niche.getActiveAuction();

            auction.setEndDatetime(updateDatetime);

            auction.setLeadingBid(null);
            niche.setActiveAuction(null);

            // jw: next, let's unschedule the jobs used to handle lifecycle events for this auction.
            ProcessAuctionBeginningPaymentPeriodJob.unschedule(auction);
            ProcessAuctionExpiringJob.unschedule(auction);

            // jw: next, if auction has a active invoice then we need to cancel that.
            if (exists(auction.getActiveInvoice())) {
                getAreaContext().doAreaTask(new CancelInvoiceTask(auction.getActiveInvoice().getInvoice()));
            }

            // jw: we need to email all subscribers and bidders that the auction has been cancelled.
            // jw: it's important that we run this before we flag all active bidders as withdrawn, since we want to only
            //     send this to non-withdrawn bidders.
            getAreaContext().doAreaTask(new SendAuctionCancelledEmail(auction));
        }

        niche.updateStatus(NicheStatus.REJECTED);

        // bl: when doing the initial approval by the community, send the email to the suggester
        if(!exists(dueToIssue)) {
            getAreaContext().doAreaTask(new SendSuggestedNicheRejectedEmail(niche));
        } else {
            // bl: otherwise, it's a TribunalIssue. we only want to send the email to the niche owner if the niche
            // had been active. this should handle it since the email will only be sent if there was an originalOwner.
            // note that in some cases we will not send an email at all (niche approved, but not purchased + rejected by tribunal)
            if(exists(originalOwner)) {
                getAreaContext().doAreaTask(new SendActiveNicheRejectedEmailToOwner(originalOwner, niche));
            }
        }
    }
}
