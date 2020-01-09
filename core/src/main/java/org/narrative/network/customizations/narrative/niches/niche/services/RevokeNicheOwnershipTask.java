package org.narrative.network.customizations.narrative.niches.niche.services;

import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.niches.elections.services.CancelNicheModeratorElectionTask;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-06-11
 * Time: 00:28
 *
 * @author jonmark
 */
public class RevokeNicheOwnershipTask extends AreaTaskImpl<Object> {
    private final Niche niche;

    public RevokeNicheOwnershipTask(Niche niche) {
        assert niche.getStatus().isActive() : "Should only ever call this for active niches!";

        this.niche = niche;
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: if there is an active moderator election, let's cancel it.
        if (exists(niche.getActiveModeratorElection())) {
            networkContext().doAreaTask(niche.getArea(), new CancelNicheModeratorElectionTask(niche));
        }
        // jw: since this niche is being put back up for sale, let's reset the moderatorSlots back to the default.
        niche.updateModeratorSlots(Niche.DEFAULT_MODERATOR_SLOT_COUNT);

        AreaUserRlm owner = niche.getOwner();
        assert exists(owner) : "Since the Niche is active we should always have an owner!";

        // jw: now that the offender has been taken care of, let's put the niche back up for sale.
        niche.setOwner(null);
        niche.getChannel().setPurchaseInvoice(null);

        // jw: now that we have stripped the owner from the niche, let's ensure that we update their associations as well.
        owner.removeExpectedNicheAssociation(niche, false);

        // jw: for both refunds and chargebacks we want to record that the owner was removed from the niche.
        LedgerEntry ledgerEntry = new LedgerEntry(owner, LedgerEntryType.NICHE_OWNER_REMOVED);
        ledgerEntry.setChannelForConsumer(niche);
        getNetworkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));

        // jw: finally, let's put it up for sale
        getAreaContext().doAreaTask(new PutNicheOnSaleTask(niche, null));

        return null;
    }
}
