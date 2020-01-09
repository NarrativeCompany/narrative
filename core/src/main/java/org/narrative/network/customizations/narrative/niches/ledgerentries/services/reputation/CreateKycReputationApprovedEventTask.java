package org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.CreateReputationEventsFromLedgerEntryTask;
import org.narrative.shared.event.reputation.KYCVerificationEvent;
import org.narrative.shared.event.reputation.ReputationEvent;

import java.util.Arrays;
import java.util.Collection;

public class CreateKycReputationApprovedEventTask extends CreateReputationEventsFromLedgerEntryTask {
    public CreateKycReputationApprovedEventTask(LedgerEntry entry) {
        super(entry);
    }

    @Override
    protected LedgerEntryType getExpectedLedgerEntryType() {
        return LedgerEntryType.KYC_CERTIFICATION_APPROVED;
    }

    @Override
    protected Collection<ReputationEvent> createEventsFromLedgerEntry(LedgerEntry entry) {
        return Arrays.asList(
        KYCVerificationEvent.builder()
                .userOid(entry.getActor().getUser().getOid().getValue())
                .isVerified(true)
                .build()
        );
    }
}
