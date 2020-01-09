package org.narrative.network.customizations.narrative.niches.elections.services;

import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.ElectionType;
import org.narrative.network.customizations.narrative.elections.services.CreateElectionTaskBase;
import org.narrative.network.customizations.narrative.niches.elections.NicheModeratorElection;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.niche.Niche;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 11/13/18
 * Time: 2:07 PM
 *
 * @author jonmark
 */
public class CreateNicheModeratorElectionTask extends CreateElectionTaskBase {
    private final Niche niche;

    public CreateNicheModeratorElectionTask(Niche niche) {
        super(ElectionType.NICHE_MODERATOR, niche.getOpenModeratorSlots());

        assert exists(niche) : "Should always be given a niche!";
        assert niche.getStatus().isActive() : "Specified niche must be active!";
        assert exists(niche.getOwner()) : "Specified niche must have an owner!";

        // jw: I considered checking that the current user is the same as the owner, but let's not worry about that here
        //     and instead assume that the caller took care of that check.

        this.niche = niche;
    }

    @Override
    protected void onElectionCreated(Election election) {
        // jw: first, create the moderator election
        NicheModeratorElection moderatorElection = new NicheModeratorElection(niche, election);
        NicheModeratorElection.dao().save(moderatorElection);

        // jw: next, let's set that as the active moderator election on the niche!
        niche.setActiveModeratorElection(moderatorElection);
    }

    @Override
    protected void addTypeSpecificLedgerEntryDetails(LedgerEntry entry) {
        entry.setChannelForConsumer(niche);
    }
}
