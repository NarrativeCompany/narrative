package org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.CreateReputationEventsFromLedgerEntryTask;
import org.narrative.shared.event.reputation.ConductEventType;
import org.narrative.shared.event.reputation.ReputationEvent;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Date: 2019-03-18
 * Time: 09:26
 *
 * @author brian
 */
public class CreateAupViolationReputationEventTask extends CreateReputationEventsFromLedgerEntryTask {
    public CreateAupViolationReputationEventTask(LedgerEntry entry) {
        super(entry);
    }

    @Override
    protected LedgerEntryType getExpectedLedgerEntryType() {
        return LedgerEntryType.USER_HAD_POST_OR_COMMENT_DELETED_FOR_AUP_VIOLATION;
    }

    @Override
    protected Collection<ReputationEvent> createEventsFromLedgerEntry(LedgerEntry entry) {
        Collection<ReputationEvent> events = new LinkedList<>();
        // bl: also we need to record a Conduct Negative event for the user
        events.add(entry.getActor().getUser().createConductStatusEvent(entry.getEventDatetime(), ConductEventType.CONTENT_REMOVED_FOR_AUP_VIOLATION));
        return events;
    }
}
