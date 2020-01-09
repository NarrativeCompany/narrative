package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.invoices.services.HandlePaidChannelInvoiceTaskBase;
import org.narrative.network.customizations.narrative.niches.NarrativeCircleType;
import org.narrative.network.customizations.narrative.niches.elections.services.CreateNicheModeratorElectionTask;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.niches.nicheassociation.AssociationType;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheUserAssociation;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/21/18
 * Time: 11:06 AM
 */
public class HandlePaidNicheAuctionInvoiceTask extends HandlePaidChannelInvoiceTaskBase<NicheAuctionInvoice, Niche> {

    public HandlePaidNicheAuctionInvoiceTask(Invoice invoice) {
        super(InvoiceType.NICHE_AUCTION, invoice);
    }

    @Override
    protected Niche getChannelConsumer(NicheAuctionInvoice nicheInvoice) {
        return nicheInvoice.getAuction().getNiche();
    }

    @Override
    protected void handlePaidInvoice(NicheAuctionInvoice auctionInvoice, Niche niche) {
        NicheAuction auction = auctionInvoice.getAuction();

        assert isEqual(auction, niche.getActiveAuction()) : "The activeAuction/"+niche.getActiveAuction().getOid()+" for niche/"+niche.getOid()+" does not match the auction from the paid invoice/"+invoice.getOid();

        // jw: Let's gather some objects that we will use during this processing.
        NicheAuctionBid leadingBid = auctionInvoice.getBid();

        // jw: update the niche, by setting the bidder as the owner and clearing the activeAuction, since it is now complete!
        niche.setOwner(leadingBid.getBidder());
        niche.setActiveAuction(null);

        // jw: we need to update their association to be a OWNER, instead of a BIDDER
        NicheUserAssociation association = leadingBid.getBidder().getNicheAssociation(niche);
        assert exists(association) : "The leading bidder should always have an association to the niche that they have paid for. How was that removed? n/" + niche.getOid() + " b/" + leadingBid.getBidder().getOid();
        association.setType(AssociationType.OWNER);

        // jw: final piece for the owner, let's ensure that the owner is in the Niche Owners circle.
        NarrativeCircleType.NICHE_OWNERS.addUserToCircle(niche.getOwner().getUser());

        // jw: finally, we need to update the Niche to be Approved since it has been purchased!
        niche.updateStatus(NicheStatus.ACTIVE);

        // jw: we need to add a LedgerEntry for the invoice paid
        LedgerEntry ledgerEntry = new LedgerEntry(leadingBid.getBidder(), LedgerEntryType.NICHE_INVOICE_PAID);
        ledgerEntry.setChannelForConsumer(niche);
        ledgerEntry.setAuction(auction);
        ledgerEntry.setAuctionBid(leadingBid);
        networkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));

        // jw: now that we have elections, we need to create a moderator election for any open slots.
        if (niche.getOpenModeratorSlots() > 0) {
            getAreaContext().doAreaTask(new CreateNicheModeratorElectionTask(niche));
        }

        // jw: if the purchaser paid a deposit for this niche then we need to refund that now
        NicheAuctionSecurityDeposit securityDeposit = NicheAuctionSecurityDeposit.dao().getSecurityDeposit(auction, leadingBid.getBidder().getUser());
        if (exists(securityDeposit)) {
            getAreaContext().doAreaTask(new RefundNicheAuctionSecurityDepositTask(securityDeposit));
        }
    }
}
