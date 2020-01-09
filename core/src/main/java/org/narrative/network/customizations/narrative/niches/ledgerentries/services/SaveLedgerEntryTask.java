package org.narrative.network.customizations.narrative.niches.ledgerentries.services;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2018-12-14
 * Time: 08:24
 *
 * @author jonmark
 */
public class SaveLedgerEntryTask extends GlobalTaskImpl<LedgerEntry> {
    private final LedgerEntry entry;

    public SaveLedgerEntryTask(LedgerEntry entry) {
        this.entry = entry;
    }

    @Override
    protected LedgerEntry doMonitoredTask() {
        // jw: next, let's save the entry prior to doing any common post processing
        LedgerEntry.dao().save(entry);

        // jw: now, let's see if there is a reputation event task for this entry and process it if present.
        CreateReputationEventsFromLedgerEntryTask reputationEventTask = entry.getCreateReputationEventTask();
        if (reputationEventTask != null) {
            networkContext().doGlobalTask(reputationEventTask);
        }

        return entry;
    }
}
