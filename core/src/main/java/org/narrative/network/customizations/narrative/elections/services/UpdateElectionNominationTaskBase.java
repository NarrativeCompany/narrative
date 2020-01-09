package org.narrative.network.customizations.narrative.elections.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.ElectionNominee;
import org.narrative.network.customizations.narrative.elections.NomineeStatus;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 11/19/18
 * Time: 2:33 PM
 *
 * @author jonmark
 */
public abstract class UpdateElectionNominationTaskBase extends AreaTaskImpl<ElectionNominee> {
    protected final Election election;
    protected final User nominee;

    UpdateElectionNominationTaskBase(Election election, User nominee) {
        assert exists(election) : "Should always be provided with an election!";
        assert exists(nominee) : "Should always be provided with a user!";

        this.election = election;
        this.nominee = nominee;
    }

    @Override
    protected void validate(ValidationHandler handler) {
        if (!election.getStatus().isNominating()) {
            handler.addActionError(wordlet("updateElectionNomineeTask.nominationChangesNotAllowed"));

        } else if (!election.getType().isCanUserBeNominated(nominee)) {
            throw UnexpectedError.getRuntimeException("Should never have attempted to nominate a user who cannot be nominated!");
        }
    }

    protected abstract ElectionNominee updateElectionNominee(ElectionNominee existing);

    @Override
    protected ElectionNominee doMonitoredTask() {
        // jw: first, let's look for the nominee for the specified user.
        ElectionNominee electionNominee = ElectionNominee.dao().getForUser(election, nominee);

        // jw: let's pass that to the implementor for processing.
        return updateElectionNominee(electionNominee);
    }

    protected void createLedgerEntryForStatus(NomineeStatus status) {
        LedgerEntryType entryType = status.getLedgerEntryType(election.getType());

        // jw: if there is no entryType for this status, then short out.
        if (entryType == null) {
            return;
        }

        // jw: create the ledger entry for this event.
        LedgerEntry entry = new LedgerEntry(AreaUser.getAreaUserRlm(nominee.getLoneAreaUser()), entryType);
        entry.setElection(election);

        // jw: now that we have setup the entry, let's apply type specific data to it.
        election.getType().addTypeSpecificLedgerEntryData(election, entry);

        getNetworkContext().doGlobalTask(new SaveLedgerEntryTask(entry));
    }
}
