package org.narrative.network.customizations.narrative.service.impl.referendum;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.controller.postbody.referendum.ReferendumVoteInputDTO;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.customizations.narrative.niches.referendum.dao.ReferendumDAO;
import org.narrative.network.customizations.narrative.niches.referendum.dao.ReferendumVoteDAO;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.shared.context.AreaContext;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.util.NetworkCoreUtils;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.jupiter.api.Test;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VoteOnReferendumTaskTest {

    @Mocked
    private Referendum referendum;

    @Mocked
    private ReferendumVoteInputDTO referendumVoteInputDTO;

    @Test
    void validate_openAndVoteFor_cleanValidation(@Mocked ValidationContext validationContext, @Mocked ReferendumDAO referendumDAO) {
        validateExpectations(referendumDAO);

        new Expectations(Referendum.class) {{
            referendum.isOpen();
            result = true;

            referendumVoteInputDTO.getVotedFor();
            result = true;
        }};

        VoteOnReferendumTask voteOnReferendumTask = new VoteOnReferendumTask(null, referendumVoteInputDTO);
        voteOnReferendumTask.validate(validationContext);

    }

    @Test
    void validate_closed_throwsAccessViolation(@Mocked ValidationContext validationContext, @Mocked ReferendumDAO referendumDAO) {
        final String CLOSED_WORDLET_KEY = "nicheVoteAjaxAction.approvalClosed";
        final String CLOSED_WORDLET = "closed";
        final String ERROR_TITLE_WORDLET_KEY = "error.title";
        final String ERROR_WORDLET = "Error";

        validateExpectations(referendumDAO);

        new Expectations(Referendum.class) {{
            referendum.isOpen();
            result = false;

            wordlet(CLOSED_WORDLET_KEY);
            result = CLOSED_WORDLET;

            wordlet(ERROR_TITLE_WORDLET_KEY);
            result = ERROR_WORDLET;
        }};

        try {
            VoteOnReferendumTask voteOnReferendumTask = new VoteOnReferendumTask(null, referendumVoteInputDTO);
            voteOnReferendumTask.validate(validationContext);
        } catch(AccessViolation av) {
            // assert that we got the expected AccessViolation that the approval is closed
            assertEquals(av.getMessage(), CLOSED_WORDLET);
        }
    }

    @Test
    void validate_requiredReasonMissing_addsFieldError(@Mocked ValidationContext validationContext, @Mocked ReferendumDAO referendumDAO) {

        final String REASON_WORDLET_KEY = "referendumServiceImpl.voteReason";
        final String REASON_WORDLET = "REASON";

        validateExpectations(referendumDAO);

        new Expectations(Referendum.class) {{
            referendum.isOpen();
            result = true;

            referendumVoteInputDTO.getVotedFor();
            result = false;

            referendum.getType().isRequireReasonWhenVotingAgainst();
            result = true;

            referendumVoteInputDTO.getReason();
            result = null;
        }};

        VoteOnReferendumTask voteOnReferendumTask = new VoteOnReferendumTask(null, referendumVoteInputDTO);
        voteOnReferendumTask.validate(validationContext);

        // Verifies that validationContext.addMeaddRequiredFieldError thodError was called with the correct wordlet
        new Verifications() {{
            validationContext.addRequiredFieldError("voteReason");
        }};
    }

    @Test
    void doMonitoredTask_newVote_returnsVote(@Mocked ReferendumDAO referendumDAO, @Mocked ReferendumVoteDAO referendumVoteDAO, @Mocked ValidationContext validationContext, @Mocked AreaContext areaContext, @Mocked AreaUserRlm areaUserRlm) {
        VoteOnReferendumTask voteOnReferendumTask = new VoteOnReferendumTask(null, referendumVoteInputDTO);

        validateExpectations(referendumDAO);

        new Expectations(ReferendumVote.class, voteOnReferendumTask) {
            {
                ReferendumVote.dao();
                result = referendumVoteDAO;

                areaContext.getAreaUserRlm();
                result = areaUserRlm;

                referendumVoteDAO.getForReferendumAndVoter((Referendum) any, (AreaUserRlm) any);
                result = null;

                // A For vote
                referendumVoteInputDTO.getVotedFor();
                result = true;

                // Partially mock VoteOnReferendumTask
                voteOnReferendumTask.getAreaContext();
                result = areaContext;

                voteOnReferendumTask.writeLedgerEntry();

            }
        };

        // Call the method under test
        voteOnReferendumTask.validate(validationContext);
        ReferendumVote vote = voteOnReferendumTask.doMonitoredTask();

        new Verifications() {{
            // Verify that writeLedgerEntry was called
            voteOnReferendumTask.writeLedgerEntry();

            // Verify that save was called
            referendumVoteDAO.save((ReferendumVote) any);
        }};
    }

    @Test
    void doMonitoredTask_changeVote_returnsVote(@Mocked ReferendumDAO referendumDAO, @Mocked ReferendumVoteDAO referendumVoteDAO, @Mocked ValidationContext validationContext, @Mocked ReferendumVote vote, @Mocked AreaContext areaContext, @Mocked AreaUserRlm areaUserRlm, @Mocked OID referendumOid) {
        VoteOnReferendumTask voteOnReferendumTask = new VoteOnReferendumTask(referendumOid, referendumVoteInputDTO);

        validateExpectations(referendumDAO);

        new Expectations(ReferendumVote.class, voteOnReferendumTask) {
            {
                ReferendumVote.dao();
                result = referendumVoteDAO;

                areaContext.getAreaUserRlm();
                result = areaUserRlm;

                referendumVoteDAO.getForReferendumAndVoter((Referendum) any, (AreaUserRlm) any);
                result = vote;

                // Was an Against vote
                vote.getVotedFor();
                result = false;

                // Now a For vote
                referendumVoteInputDTO.getVotedFor();
                result = true;

                // Vote was changed to true
                vote.changeVote(true);

                // Partially mock VoteOnReferendumTask
                voteOnReferendumTask.getAreaContext();
                result = areaContext;

                voteOnReferendumTask.writeLedgerEntry();

            }
        };

        // Call the method under test
        voteOnReferendumTask.validate(validationContext);
        ReferendumVote returnVote = voteOnReferendumTask.doMonitoredTask();

        new Verifications() {{
            // Verify that writeLedgerEntry was called
            voteOnReferendumTask.writeLedgerEntry();

            // Verify that changeVote was called
            vote.changeVote(anyBoolean);
        }};
    }

    private void validateExpectations(ReferendumDAO referendumDAO) {
        new Expectations(NetworkCoreUtils.class) {
            {
                NetworkCoreUtils.areaContext();

                Referendum.dao();
                result = referendumDAO;

                referendumDAO.getForApiParam((OID) any, "referendumId");
                result = referendum;

                referendum.getType().isTribunalReferendum();
                result = false;
            }
        };
    }
}