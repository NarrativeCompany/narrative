package org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.CreateReputationEventsFromLedgerEntryTask;
import org.narrative.shared.event.reputation.ConductEventType;
import org.narrative.shared.event.reputation.ReputationEvent;

import java.util.Collection;
import java.util.Collections;

/**
 * Date: 2018-12-14
 * Time: 15:14
 *
 * @author jonmark
 */
public class CreateNichePurchaseFailureReputationEventsTask extends CreateReputationEventsFromLedgerEntryTask {

    public CreateNichePurchaseFailureReputationEventsTask(LedgerEntry entry) {
        super(entry);
    }

    @Override
    protected LedgerEntryType getExpectedLedgerEntryType() {
        return LedgerEntryType.NICHE_INVOICE_FAILED;
    }

    @Override
    protected Collection<ReputationEvent> createEventsFromLedgerEntry(LedgerEntry entry) {
        return Collections.singleton(entry.getActor().getUser().createConductStatusEvent(entry.getEventDatetime(), ConductEventType.FAILURE_TO_PAY_FOR_NICHE));
    }
}
