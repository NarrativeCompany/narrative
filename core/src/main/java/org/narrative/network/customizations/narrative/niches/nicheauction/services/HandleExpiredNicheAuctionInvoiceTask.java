package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheUserAssociation;
import org.narrative.network.customizations.narrative.niches.nicheauction.BidStatus;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-02-04
 * Time: 16:33
 *
 * @author jonmark
 */
public class HandleExpiredNicheAuctionInvoiceTask extends AreaTaskImpl<Object> {
    private final Invoice invoice;

    public HandleExpiredNicheAuctionInvoiceTask(Invoice invoice) {
        assert invoice.getType().isNicheAuction() : "Should only ever call this for Niche Purchase invoices. Not/"+invoice.getType();

        this.invoice = invoice;
    }

    @Override
    protected Object doMonitoredTask() {
        NicheAuctionInvoice auctionInvoice = invoice.getInvoiceConsumer();
        NicheAuction auction = auctionInvoice.getAuction();

        NicheAuctionBid leadingBid = auction.getLeadingBid();
        AreaUserRlm bidder = leadingBid.getBidder();

        assert isEqual(leadingBid, auctionInvoice.getBid()) : "We expect that the activeInvoice for a auction should always have the same bid as the leadingBid for that auction! auction/" + auction.getOid() + " invoice/" + invoice.getOid() + " invoice.bid/" + auctionInvoice.getBid().getOid() + " leadingBid/" + leadingBid.getOid();

        // jw: first things first, let's process this users bids on this auction since we know we have to do that.
        leadingBid.setStatus(BidStatus.FAILED_TO_PAY);
        NicheAuctionBid.dao().updateStatusForAllBids(auction, bidder, BidStatus.FAILED_TO_PAY);

        // jw: Since this user is no longer bidding, we can clean up their NicheUserAssociation
        NicheUserAssociation association = bidder.getNicheAssociation(auction.getNiche());
        if (exists(association)) {
            NicheUserAssociation.dao().delete(association);
            bidder.getNicheUserAssociations().remove(association.getAssociationSlot());
        }

        // jw: before we move on let's update the deposit invoice based on the current nrve value if we had one.
        NicheAuctionSecurityDeposit securityDeposit = NicheAuctionSecurityDeposit.dao().getSecurityDeposit(auction, bidder.getUser());
        if (exists(securityDeposit) && !securityDeposit.getInvoice().getFiatPayment().getStatus().isPaid()) {
            BigDecimal pricePerNrve = GlobalSettingsUtil.getGlobalSettings().getNrveUsdPrice();
            Invoice depositInvoice = securityDeposit.getInvoice();

            // jw: calculate the nrveAmount using the same rounding as we use for minimum bid calculations.
            NrveValue nrveAmount = new NrveValue(depositInvoice.getUsdAmount().divide(pricePerNrve, 8, RoundingMode.CEILING));

            depositInvoice.setNrveAmount(nrveAmount);
            depositInvoice.getFiatPayment().setNrveAmount(nrveAmount);
        }

        auction.saveLedgerEntry(leadingBid, LedgerEntryType.NICHE_INVOICE_FAILED);

        Niche niche = auction.getNiche();

        niche.updateStatus(NicheStatus.FOR_SALE);

        // jw: Alright, now we need to create a new auction.
        NicheAuction newAuction = new NicheAuction(niche);
        NicheAuction.dao().save(newAuction);

        niche.setActiveAuction(newAuction);

        // jw: before we do the cleanup, let's send the auction failure email
        areaContext().doAreaTask(new SendAuctionFailureEmail(auction, newAuction));

        newAuction.saveLedgerEntry(null, LedgerEntryType.NICHE_AUCTION_RESTARTED, 1);

        // bl: must flush so that the NicheAuction is written to the database before the next update statement runs
        NicheAuction.dao().getGSession().flushSession();

        return null;
    }
}
