package org.narrative.network.customizations.narrative.elections;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.elections.NicheModeratorElection;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 11/12/18
 * Time: 10:04 AM
 *
 * @author jonmark
 */
public enum ElectionType implements IntegerEnum {
    NICHE_MODERATOR(0, LedgerEntryType.NICHE_MODERATOR_NOMINATING_STARTED, LedgerEntryType.NICHE_MODERATOR_NOMINATED, LedgerEntryType.NICHE_MODERATOR_NOMINEE_WITHDRAWN) {
        @Override
        public void addTypeSpecificLedgerEntryData(Election election, LedgerEntry entry) {
            assert election.getType().isNicheModerator() : "Should only ever create this task with a NICHE_MODERATOR election!";

            NicheModeratorElection moderatorElection = NicheModeratorElection.dao().get(election.getOid());

            assert exists(moderatorElection) : "We should ALWAYS have a NicheModeratorElection that corresponds to election/" + election.getOid();

            entry.setChannelForConsumer(moderatorElection.getNiche());
        }
    },
    TRIBUNAL(1, null, null, null),
    COMMITTEE(2, null, null, null) {
        @Override
        public boolean isCanUserBeNominated(User user) {
            return user.isCanParticipateInTribunalIssues();
        }
    },
    ;

    private final int id;
    private final LedgerEntryType nominatingStartedEntryType;
    private final LedgerEntryType nomineeConfirmedEntryType;
    private final LedgerEntryType nomineeWithdrawnEntryType;

    ElectionType(int id, LedgerEntryType nominatingStartedEntryType, LedgerEntryType nomineeConfirmedEntryType, LedgerEntryType nomineeDeclinedEntryType) {
        this.id = id;
        this.nominatingStartedEntryType = nominatingStartedEntryType;
        this.nomineeConfirmedEntryType = nomineeConfirmedEntryType;
        this.nomineeWithdrawnEntryType = nomineeDeclinedEntryType;
    }

    @Override
    public int getId() {
        return id;
    }

    public LedgerEntryType getNominatingStartedEntryType() {
        return nominatingStartedEntryType;
    }

    public LedgerEntryType getNomineeConfirmedEntryType() {
        return nomineeConfirmedEntryType;
    }

    public LedgerEntryType getNomineeWithdrawnEntryType() {
        return nomineeWithdrawnEntryType;
    }

    public boolean isNicheModerator() {
        return this == NICHE_MODERATOR;
    }

    public boolean isCanUserBeNominated(User user) {
        // jw: let's default to all members can be nominated. This will be overridden by any types that have narrower requirements
        return true;
    }

    public void addTypeSpecificLedgerEntryData(Election election, LedgerEntry entry) {
        // jw: by default, there is nothing to do here.
    }
}