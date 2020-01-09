package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.invoices.services.SendInvoiceEmail;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/21/18
 * Time: 9:26 AM
 * <p>
 * jw: we will only ever invoice the leading bid, so it's important that we always recalculate the leadingBid if we are invoicing a new bidder.
 */
public class InvoiceLeadingBidderTask extends AreaTaskImpl<Object> {
    private final NicheAuction auction;

    public InvoiceLeadingBidderTask(NicheAuction auction) {
        this.auction = auction;

        assert exists(auction.getLeadingBid()) : "Auction should have a leading bid! auction/" + auction.getOid();
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: first things first, let's lock the auction so that we are certain we are the only one manipulating this.
        NicheAuction.dao().lock(auction);

        // jw: everything will be based on the leading bidder
        NicheAuctionBid leadingBid = auction.getLeadingBid();

        // jw: create the invoice
        Invoice invoice = new Invoice(
                InvoiceType.NICHE_AUCTION,
                leadingBid.getBidder().getUser(),
                leadingBid.getNrveBid(),
                NicheAuctionInvoice.PAYMENT_PERIOD_IN_MS
        );

        // jw: create the AuctionInvoice from the Invoice
        NicheAuctionInvoice auctionInvoice = new NicheAuctionInvoice(invoice, auction, leadingBid);
        NicheAuctionInvoice.dao().save(auctionInvoice);

        // jw: associate the invoice with the auction
        auction.setActiveInvoice(auctionInvoice);

        // jw: let's update the lastStatusUpdateDatetime so that this will bubble up in the purchase list.
        auction.getNiche().setLastStatusChangeDatetime(now());

        // jw: send invoice email
        getAreaContext().doAreaTask(new SendInvoiceEmail(invoice));

        //mk: also schedule expiring invoice reminder
        ProcessInvoiceExpiringJob.schedule(invoice);

        return null;
    }
}
