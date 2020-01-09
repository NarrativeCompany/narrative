package org.narrative.network.customizations.narrative.elections.services;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.ElectionNominee;
import org.narrative.network.customizations.narrative.elections.NomineeStatus;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 11/19/18
 * Time: 2:26 PM
 *
 * @author jonmark
 */
public class WithdrawFromElectionTask extends UpdateElectionNominationTaskBase {
    public WithdrawFromElectionTask(Election election, User nominee) {
        super(election, nominee);
    }

    @Override
    protected ElectionNominee updateElectionNominee(ElectionNominee existing) {
        boolean isEdit = exists(existing);

        NomineeStatus status;
        if (!isEdit) {
            // jw: there is nothing to do if the user is not even associated to thie election yet.
            return null;

            // jw: if this is already a negative response, then short out since there is nothing else to do.
        } else if (existing.getStatus().isNegativeType()) {
            return existing;

        } else if (existing.getStatus().isConfirmed()) {
            status = NomineeStatus.WITHDRAWN;

        } else {
            assert existing.getStatus().isPending() : "Expected PENDING status, but got/" + existing.getStatus();

            status = NomineeStatus.WITHDRAWN;
        }

        // jw: update the existing object with the new status, and clear any personal statement that might have been set before.
        existing.setStatus(status);
        existing.setPersonalStatement(null);

        // jw: now that we have the object, let's create the appropriate ledger entry for this action.
        createLedgerEntryForStatus(status);

        return existing;
    }
}
