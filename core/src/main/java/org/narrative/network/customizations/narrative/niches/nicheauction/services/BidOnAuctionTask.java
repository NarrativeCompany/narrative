package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.channels.channel.services.UpdateFollowedChannelTask;
import org.narrative.network.customizations.narrative.controller.postbody.auction.NicheAuctionBidInputDTO;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheTaskBase;
import org.narrative.network.customizations.narrative.niches.nicheassociation.AssociationType;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheUserAssociation;
import org.narrative.network.customizations.narrative.niches.nicheauction.BidStatus;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.NrveUsdPriceFields;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/20/18
 * Time: 8:37 AM
 */
public class BidOnAuctionTask extends NicheTaskBase {
    private final NicheAuction auction;
    private final AreaUserRlm bidder;
    private final NrveValue maxBid;
    private final NrveUsdPriceFields nrveUsdPriceFields;

    public BidOnAuctionTask(Niche niche, AreaUserRlm bidder, BigDecimal maxBid, NrveUsdPriceFields nrveUsdPriceFields) {
        super(niche);
        this.auction = niche.getActiveAuction();
        assert exists(auction) : "The provided Niche should have an activeAuction, which is where all voting should take place.";
        this.bidder = bidder;
        this.maxBid = new NrveValue(maxBid);
        this.nrveUsdPriceFields = nrveUsdPriceFields;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        /*
         * ActiveAuctionUpdateActionBase#checkRight
         */
        getAreaContext().getPrimaryRole().checkRegisteredUser();

        /*
         * From NicheActionBase#checkRightAfterParams
         */
        if (!isEqual(auction.getNiche().getPortfolio(), getAreaContext().getPortfolio())) {
            throw UnexpectedError.getRuntimeException("Portfolio miss match for niche!");
        }

        /*
         * From ActiveAuctionUpdateActionBase#checkRightAfterParams
         */
        // mk: lock the table so we can ensure bids consistency.
        NicheAuction.dao().lock(auction);

        /*
         * Paraphrased from BidOnAuctionAction#validate
         */
        auction.checkCanCurrentRoleBidOnAuction();

        // bl: if there is already a bid on the auction, then make sure that the price fields match!
        // bl: if there is no bid yet, then we need to validate the price fields
        boolean isNrvePriceValid;
        NrveValue minimumBidForAuction;
        if(exists(auction.getLeadingBid())) {
            // bl: if the price value doesn't match, then it's not valid.
            // bl: note that we don't care if the NrveUsdPriceFields is even valid; as long as the price is right, we're good to go
            isNrvePriceValid = auction.getNrveUsdPrice().compareTo(nrveUsdPriceFields.getNrveUsdPrice())==0;
            minimumBidForAuction = auction.getMinimumBidNrveForCurrentRole();
        } else {
            isNrvePriceValid = nrveUsdPriceFields.isValid();
            // bl: for the first bid, we have to get the minimum bid based off of the price passed in
            minimumBidForAuction = isNrvePriceValid ? NicheAuction.getStartingBidNrve(nrveUsdPriceFields.getNrveUsdPrice()) : null;
        }
        // bl: give the same validation error for both scenarios where the price has changed (either due to a first bid
        // or due to a price expiration).
        if(!isNrvePriceValid) {
            validationContext.addFieldError(NicheAuctionBidInputDTO.Fields.maxNrveBid, "bidOnAuctionAction.nrvePriceChanged");
        } else {
            assert minimumBidForAuction!=null : "Should always have a minimumBidForAuction at this point!";
            // bl: if the supplied NRVE price is valid, then we just need to verify the minimum bid
            // jw: if the bidder did not meet or exceed the minimum, let's inform them of that!
            if (maxBid.compareTo(minimumBidForAuction) < 0) {
                validationContext.addFieldError(NicheAuctionBidInputDTO.Fields.maxNrveBid, "bidOnAuctionAction.bidTooLow", minimumBidForAuction.getFormattedWithSuffix());
            }
        }
    }

    @Override
    protected void processNiche(Niche niche) {
        // jw: Since this is the first time the user has bid on this auction, let's go ahead and subscribe them to
        //     the niche. That will ensure they get notifications of Auction events going forward.
        // bl: let's just always make the bidder follow the niche. safe to renew every time since your bid implies
        // sufficient interest in the niche to follow it.
        getAreaContext().doGlobalTask(new UpdateFollowedChannelTask(niche.getChannel(), true));

        NicheAuctionBid previousLeadingBid = auction.getLeadingBid();
        Instant now = Instant.now();

        // jw: if this is the first bid, then that just made our lives so much simpler!
        if (!exists(previousLeadingBid)) {
            // this is the first bid, so create the association record for the bidder!
            createNicheAssociationForBidder(niche);

            // bl: lock the price of NRVE on the auction
            auction.setNrveUsdPrice(nrveUsdPriceFields.getNrveUsdPrice());

            // bl: the startingBid here will be calculated based on the nrveUsdPrice we just set above
            NrveValue minimumBid = auction.getStartingBid().getNrve();
            assert maxBid.compareTo(minimumBid) >= 0 : "Provided bid/" + maxBid + " did not meet minimum required bid for new auction/" + minimumBid;

            // jw: create the new bid and set it up as the leading bid.
            NicheAuctionBid bid = createBid(bidder, BidStatus.LEADING, minimumBid, maxBid, now, null);
            auction.setLeadingBid(bid);
            // jw: we also need to make sure that we set the end datetime for this auction, since this is the first bid!
            auction.setEndDatetime(new Timestamp(System.currentTimeMillis() + NicheAuction.MS_FROM_FIRST_BID_TO_END_AUCTION));

            // jw: schedule the job that will invoice the auction when it ends
            ProcessAuctionBeginningPaymentPeriodJob.schedule(auction);

            // jw: send a new notification that a bid has been made!
            getAreaContext().doAreaTask(new SendLeadingBidEmail(bid, null, getAreaContext().getAreaUserRlm()));

            //mk: schedule the expiring auction reminder upon placing first bid
            ProcessAuctionExpiringJob.schedule(auction);

            // jw: since this is the only bid, let's return!
            return;
        }

        AreaUserRlm previousLeadingBidder = previousLeadingBid.getBidder();

        // jw: if this is the current leading bidder, then let's just edit their existing bid
        if (isEqual(bidder, previousLeadingBidder)) {
            assert maxBid.compareTo(previousLeadingBid.getNrveBid()) >= 0 : "Should only ever specify a new maxBid that is equal to or greater than the current leading bid.";

            // jw: easy-peasy, just edit the current maxBid, and short out.
            previousLeadingBid.setMaxNrveBid(maxBid);

            // jw: since this is just an adjustment of the existing leading bids max, there is no need to send an email

            return;
        }

        // jw: no matter what, the current leading bid has been outbid, so update it.
        previousLeadingBid.setStatus(BidStatus.OUTBID);

        assert maxBid.compareTo(previousLeadingBid.getNrveBid().add(NicheAuctionBid.MIN_BID_INCREMENT_NRVE)) >= 0 : "The new bid/" + maxBid + " must be at least " + NicheAuctionBid.MIN_BID_INCREMENT_NRVE + " above the existing bid/" + previousLeadingBid.getNrveBid();

        // jw: at this point, there are three scenarios to cover, all based on this bids difference from the current bids max.
        //     With that in mind, let's go ahead and calculate the difference between these two bids.
        int biddingDifference = maxBid.subtract(previousLeadingBid.getMaxNrveBid()).compareTo(NrveValue.ZERO);

        NicheAuctionBid leadingBid;
        NicheAuctionBid outbid;

        // jw: first, let's handle where the current bid is lower than the existing max bid
        if (biddingDifference < 0) {
            // jw: create the bidders bid, since they are already outbid, we need to set it for their max.
            outbid = createBid(bidder, BidStatus.OUTBID, maxBid, maxBid, now, null);

            // jw: this one is a bit more tricky, we need to create this bid a minimum bid jump above the new bid
            //     but we should not go over the defined maximum. These automatic bids are the only times where we
            //     will create a bid below the minumum increase amount.
            leadingBid = createBid(previousLeadingBidder, BidStatus.LEADING, previousLeadingBid.getMaxNrveBid().min(maxBid.add(NicheAuctionBid.MIN_BID_INCREMENT_NRVE))
                    // jw: maintain their max!
                    , previousLeadingBid.getMaxNrveBid(), now.plus(1, ChronoUnit.MILLIS), previousLeadingBid);

            // jw: next, let's process for the scenario where the this person has bid over the existing maxBid.
            //     this is probably the easiest of all scenarios!
        } else if (biddingDifference > 0) {
            // jw: first, create a bid for the previous leading bid, assuming it still has room to grow!
            if (previousLeadingBid.getNrveBid().compareTo(previousLeadingBid.getMaxNrveBid()) != 0) {
                outbid = createBid(previousLeadingBidder, BidStatus.OUTBID, previousLeadingBid.getMaxNrveBid(), previousLeadingBid.getMaxNrveBid(), now, previousLeadingBid);
                now = now.plus(1, ChronoUnit.MILLIS);
            } else {
                outbid = previousLeadingBid;
            }

            // delete the previous bidder's association now that they have been outbid
            {
                NicheUserAssociation association = previousLeadingBidder.getNicheAssociation(auction.getNiche());

                assert exists(association) : "Should always have an association when someone is outbid! niche/" + auction.getNiche().getOid() + " previousLeadingBidder/" + previousLeadingBidder.getOid();
                assert association.getType().isBidder() : "We should only be processing a bidder association at this point! not/" + association.getType();

                // jw: remove the association from the previous bidder!
                NicheUserAssociation.dao().delete(association);
                previousLeadingBidder.getNicheUserAssociations().remove(association.getAssociationSlot());
            }

            // jw: similar to above, we will allow the incremented bid to be below the minimum increment
            //     if we hit this new bids max
            leadingBid = createBid(bidder, BidStatus.LEADING, maxBid.min(previousLeadingBid.getMaxNrveBid().add(NicheAuctionBid.MIN_BID_INCREMENT_NRVE)), maxBid, now, null);

            // jw: let's make sure that the new bidder has an association to this niche now!
            createNicheAssociationForBidder(niche);
        } else {
            // jw: in this case, the two bids have tied maxes, which means that the existing bidder wins.

            // jw: create a new bid by the bidder at the same value as the existing but, but it is not the leading bid.
            //     Since the original bidder was here first, they get that right.
            outbid = createBid(bidder, BidStatus.OUTBID, maxBid, maxBid, now, null);

            // jw: since they were tied, the new leading bid will be the specified max.
            leadingBid = createBid(previousLeadingBid.getBidder(), BidStatus.LEADING, maxBid, maxBid, now.plus(1, ChronoUnit.MILLIS), previousLeadingBid);
        }

        // jw: this is now the leading bid!
        auction.setLeadingBid(leadingBid);

        // jw: now we need to send emails to the new bidder, and the previous bidder.
        getAreaContext().doAreaTask(new SendOutbidEmail(outbid, leadingBid, getAreaContext().getAreaUserRlm()));
        getAreaContext().doAreaTask(new SendLeadingBidEmail(leadingBid, outbid, getAreaContext().getAreaUserRlm()));
    }

    private void createNicheAssociationForBidder(Niche niche) {
        NicheUserAssociation association = bidder.getNicheAssociation(niche);
        if (!exists(association)) {
            // jw: creating this association will assert that there is a free slot for this bidder,
            //     which should always be true by the time we get here.
            association = new NicheUserAssociation(niche, bidder, AssociationType.BIDDER);
            NicheUserAssociation.dao().save(association);
        }
    }

    private NicheAuctionBid createBid(AreaUserRlm bidder, BidStatus status, NrveValue bid, NrveValue maxBid, Instant bidDatetime, NicheAuctionBid createdByBid) {
        // jw: First, lets create the auctionBid.
        NicheAuctionBid auctionBid = new NicheAuctionBid(auction, bidder, status, bid, maxBid, bidDatetime);
        if (exists(createdByBid)) {
            auctionBid.setCreatedFromBid(createdByBid);
        }
        NicheAuctionBid.dao().save(auctionBid);

        // jw: Next, every bid should correspond to a ledger entry, so let's create that as well.
        LedgerEntry ledgerEntry = new LedgerEntry(bidder, LedgerEntryType.NICHE_BID);
        ledgerEntry.setChannelForConsumer(auction.getNiche());
        ledgerEntry.setAuction(auction);
        ledgerEntry.setAuctionBid(auctionBid);
        ledgerEntry.setEventDatetime(bidDatetime);
        networkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));

        // jw: Finally, lets return the bid!
        return auctionBid;
    }
}
