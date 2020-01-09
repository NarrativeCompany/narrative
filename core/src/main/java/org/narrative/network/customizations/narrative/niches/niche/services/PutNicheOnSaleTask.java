package org.narrative.network.customizations.narrative.niches.niche.services;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/15/18
 * Time: 10:29 AM
 */
public class PutNicheOnSaleTask extends NicheTaskBase {
    private final TribunalIssue dueToIssue;

    public PutNicheOnSaleTask(Niche niche, TribunalIssue dueToIssue) {
        super(niche);

        this.dueToIssue = dueToIssue;
    }

    @Override
    protected void processNiche(Niche niche) {
        niche.updateStatus(NicheStatus.FOR_SALE);

        NicheAuction auction = new NicheAuction(niche);
        NicheAuction.dao().save(auction);

        // jw: now that we have a auction, let's associate it with the Niche.
        assert !exists(niche.getActiveAuction()) : "We should never have an active auction associated with the Niche before it has been approved!";
        niche.setActiveAuction(auction);

        LedgerEntry ledgerEntry = new LedgerEntry(null, LedgerEntryType.NICHE_AUCTION_STARTED);
        ledgerEntry.setChannelForConsumer(niche);
        ledgerEntry.setAuction(auction);
        getNetworkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));

        // jw: Notify the suggester that their niche has been approved!
        getAreaContext().doAreaTask(new SendNicheApprovedEmail(niche, dueToIssue));
    }
}
