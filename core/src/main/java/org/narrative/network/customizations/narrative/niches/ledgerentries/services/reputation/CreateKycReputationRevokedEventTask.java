package org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.CreateReputationEventsFromLedgerEntryTask;
import org.narrative.shared.event.reputation.KYCVerificationEvent;
import org.narrative.shared.event.reputation.ReputationEvent;

import java.util.Arrays;
import java.util.Collection;

public class CreateKycReputationRevokedEventTask extends CreateReputationEventsFromLedgerEntryTask {
    public CreateKycReputationRevokedEventTask(LedgerEntry entry) {
        super(entry);
    }

    @Override
    protected LedgerEntryType getExpectedLedgerEntryType() {
        return LedgerEntryType.KYC_CERTIFICATION_REVOKED;
    }

    @Override
    protected Collection<ReputationEvent> createEventsFromLedgerEntry(LedgerEntry entry) {
        return Arrays.asList(
        KYCVerificationEvent.builder()
                .userOid(entry.getActor().getUser().getOid().getValue())
                .isVerified(false)
                .build()
        );
    }
}
