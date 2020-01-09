package org.narrative.network.customizations.narrative.elections.services;

import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.ElectionStatus;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Date: 11/13/18
 * Time: 12:32 PM
 *
 * @author jonmark
 */
public abstract class CancelElectionTaskBase extends AreaTaskImpl<Object> {
    private final Election election;

    protected CancelElectionTaskBase(Election election) {
        super(true);
        this.election = election;
    }

    protected abstract void performTypeSpecificCancellation();

    @Override
    protected Object doMonitoredTask() {
        assert election.getStatus().isOpen() : "Should only ever get here for elections that are in a open state!  not/" + election.getStatus();

        performTypeSpecificCancellation();

        election.setStatus(ElectionStatus.CANCELED);

        // jw: Should we notify people who were nominees in the election that it was cancelled?

        // jw: are there any other generic tasks we should handle as part of this task?

        return null;
    }

    protected Election getElection() {
        return election;
    }
}
