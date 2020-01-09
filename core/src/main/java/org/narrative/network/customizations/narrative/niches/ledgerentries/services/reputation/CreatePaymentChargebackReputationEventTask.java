package org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.CreateReputationEventsFromLedgerEntryTask;
import org.narrative.shared.event.reputation.ConductEventType;
import org.narrative.shared.event.reputation.ReputationEvent;

import java.util.Collection;
import java.util.Collections;

/**
 * Date: 2019-01-29
 * Time: 14:58
 *
 * @author jonmark
 */
public class CreatePaymentChargebackReputationEventTask extends CreateReputationEventsFromLedgerEntryTask {
    public CreatePaymentChargebackReputationEventTask(LedgerEntry entry) {
        super(entry);
    }

    @Override
    protected LedgerEntryType getExpectedLedgerEntryType() {
        return LedgerEntryType.PAYMENT_CHARGEBACK;
    }

    @Override
    protected Collection<ReputationEvent> createEventsFromLedgerEntry(LedgerEntry entry) {
        return Collections.singleton(entry.getActor().getUser().createConductStatusEvent(
                entry.getEventDatetime(),
                ConductEventType.PAYMENT_CHARGEBACK
        ));
    }
}
