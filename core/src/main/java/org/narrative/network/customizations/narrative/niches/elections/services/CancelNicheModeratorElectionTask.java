package org.narrative.network.customizations.narrative.niches.elections.services;

import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.services.CancelElectionTaskBase;
import org.narrative.network.customizations.narrative.niches.elections.NicheModeratorElection;
import org.narrative.network.customizations.narrative.niches.niche.Niche;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 11/13/18
 * Time: 12:40 PM
 *
 * @author jonmark
 */
public class CancelNicheModeratorElectionTask extends CancelElectionTaskBase {
    private final Niche niche;

    public CancelNicheModeratorElectionTask(Niche niche) {
        super(getActiveModeratorElection(niche));

        this.niche = niche;
    }

    @Override
    protected void performTypeSpecificCancellation() {
        // jw: first, let's clear the active moderator election!
        niche.setActiveModeratorElection(null);

        // jw: the parent will handle cancelling the election itself, so there is nothing else for us to do here.
        //     likely, we will also be removing all moderators when this task would be called, but that will need to
        //     happen independently since a site can be rejected at any time, including when there is no moderator election
        //     under way. So...
    }

    private static Election getActiveModeratorElection(Niche niche) {
        assert exists(niche) : "Should always have a niche by the time we get here!";

        NicheModeratorElection moderatorElection = niche.getActiveModeratorElection();
        assert exists(moderatorElection) : "The provided niche should always have an active moderator election!";

        Election election = moderatorElection.getElection();
        assert election.getStatus().isOpen() : "The moderator election should always be in a open state.  Not/" + election.getStatus();

        return election;
    }
}
