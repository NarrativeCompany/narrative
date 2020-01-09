package org.narrative.network.customizations.narrative.elections.services;

import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.ElectionNominee;
import org.narrative.network.customizations.narrative.elections.NomineeStatus;

import java.time.Instant;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 11/16/18
 * Time: 3:25 PM
 *
 * @author jonmark
 */
public class ConfirmElectionNomineeTask extends UpdateElectionNominationTaskBase {
    private final String personalStatement;

    public ConfirmElectionNomineeTask(Election election, User nominee, String personalStatement) {
        super(election, nominee);

        this.personalStatement = isEmpty(personalStatement) ? null : personalStatement;
    }

    @Override
    protected void validate(ValidationHandler handler) {
        super.validate(handler);

        handler.validateString(personalStatement, 0, ElectionNominee.MAX_PERSONAL_STATEMENT_SIZE, "personalStatement", "updateElectionNomineeTask.personalStatement");
    }

    @Override
    protected ElectionNominee updateElectionNominee(ElectionNominee electionNominee) {
        boolean isEdit = exists(electionNominee);

        if (isEdit) {
            // jw: if the nominee is already confirmed, then just update their personalStatement and short out.
            if (electionNominee.getStatus().isConfirmed()) {
                // jw: if it's a resubmit of CONFIRM, then let's update their personalStatement
                electionNominee.setPersonalStatement(personalStatement);

                return electionNominee;
            }

            // jw: since this is an existing nominee let's change its status.
            electionNominee.setStatus(NomineeStatus.CONFIRMED);
            // jw: if this is being confirmed, then let's set the nominationConfirmedDatetime
            electionNominee.setNominationConfirmedDatetime(Instant.now());

        } else {
            electionNominee = new ElectionNominee(election, nominee, NomineeStatus.CONFIRMED);
        }

        // jw: the validation will have ensured that the personal statement is accurate, so let's go ahead and set the
        //     personalStatement in all cases. That way it get's cleared if the person revokes their nomination.
        electionNominee.setPersonalStatement(personalStatement);

        if (!isEdit) {
            ElectionNominee.dao().save(electionNominee);
        }

        // jw: now that we have the object, let's create the appropriate ledger entry for this action.
        createLedgerEntryForStatus(NomineeStatus.CONFIRMED);

        return electionNominee;
    }
}
