package org.narrative.network.customizations.narrative.niches.ledgerentries.services;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.reputation.services.CreateEventMessageTask;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.shared.event.reputation.ReputationEvent;

import java.util.Collection;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2018-12-12
 * Time: 10:47
 *
 * @author jonmark
 */
public abstract class CreateReputationEventsFromLedgerEntryTask extends GlobalTaskImpl<Object> {

    private final LedgerEntry entry;

    public CreateReputationEventsFromLedgerEntryTask(LedgerEntry entry) {
        assert exists(entry) : "An entry should always be provided.";
        assert entry.getType() == getExpectedLedgerEntryType() : "The specified entry should always be of the same type handled by this task! entry/" + entry.getOid() + " type/" + entry.getType() + " expectedType/" + getExpectedLedgerEntryType();
        this.entry = entry;
    }

    protected abstract LedgerEntryType getExpectedLedgerEntryType();

    protected abstract Collection<ReputationEvent> createEventsFromLedgerEntry(LedgerEntry entry);

    @Override
    protected Object doMonitoredTask() {
        Collection<ReputationEvent> events = createEventsFromLedgerEntry(entry);

        // jw: if event were created we need to queue them for processing
        if (events != null) {
            for (ReputationEvent event : events) {
                // jw: there is no need to capture the result or do anything with it, since the event is already queued
                //     for processing.
                getNetworkContext().doGlobalTask(new CreateEventMessageTask(event));
            }
        }

        return null;
    }
}
