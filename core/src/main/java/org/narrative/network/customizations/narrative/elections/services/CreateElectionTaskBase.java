package org.narrative.network.customizations.narrative.elections.services;

import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.ElectionType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Date: 11/13/18
 * Time: 1:57 PM
 *
 * @author jonmark
 */
public abstract class CreateElectionTaskBase extends AreaTaskImpl<Election> {
    private final ElectionType type;
    private final int availableSlots;

    protected CreateElectionTaskBase(ElectionType type, int availableSlots) {
        super(true);
        assert type != null : "Must always specify an election type to create!";
        assert availableSlots > 0 : "Must always specify a positive number of slots!";

        this.type = type;
        this.availableSlots = availableSlots;
    }

    protected abstract void onElectionCreated(Election election);

    protected abstract void addTypeSpecificLedgerEntryDetails(LedgerEntry entry);

    @Override
    protected Election doMonitoredTask() {
        // jw: first, let's create the base election
        Election election = new Election(type, availableSlots);
        Election.dao().save(election);

        // jw: now that we have the election, let's allow our implementor to perform whatever additional work it needs.
        onElectionCreated(election);

        // jw: finally, let's create the ledger entry if necessary
        if (type.getNominatingStartedEntryType() != null) {
            LedgerEntry entry = new LedgerEntry(null, type.getNominatingStartedEntryType());
            entry.setElection(election);

            // jw: now that we have setup the entry, let's allow our implementor to specify its details
            addTypeSpecificLedgerEntryDetails(entry);

            getNetworkContext().doGlobalTask(new SaveLedgerEntryTask(entry));
        }

        return election;
    }
}
