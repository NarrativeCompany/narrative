package org.narrative.network.customizations.narrative.service.impl.referendum;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.customizations.narrative.comments.services.CreateCommentTask;
import org.narrative.network.customizations.narrative.controller.postbody.referendum.ReferendumVoteInputDTO;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.metadata.ReferendumVoteLedgerEntryMetadata;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.customizations.narrative.niches.referendum.services.EndReferendumTask;
import org.narrative.network.customizations.narrative.niches.referendum.services.ReferendumVoteInstantEmailJob;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

public class VoteOnReferendumTask extends AreaTaskImpl<ReferendumVote> {
    private final OID referendumOid;
    private final ReferendumVoteInputDTO referendumVoteInputDTO;
    private Referendum referendum;

    VoteOnReferendumTask(OID referendumOid, ReferendumVoteInputDTO referendumVoteInputDTO) {
        super();
        this.referendumOid = referendumOid;
        this.referendumVoteInputDTO = referendumVoteInputDTO;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        this.referendum = Referendum.dao().getForApiParam(referendumOid, "referendumId");

        this.referendum.checkCanRoleVote(areaContext().getAreaRole());

        if (!referendumVoteInputDTO.getVotedFor() && referendum.getType().isRequireReasonWhenVotingAgainst() && this.referendumVoteInputDTO.getReason() == null) {
            validationContext.addRequiredFieldError("voteReason");
        }
    }

    @Override
    protected ReferendumVote doMonitoredTask() {
        ReferendumVote vote = ReferendumVote.dao().getForReferendumAndVoter(referendum, getAreaContext().getAreaUserRlm());

        if (!exists(vote)) {
            writeLedgerEntry();
            vote = new ReferendumVote(referendum, getAreaContext().getAreaUserRlm(), referendumVoteInputDTO.getVotedFor());
            ReferendumVote.dao().save(vote);
            ReferendumVoteInstantEmailJob.schedule(vote);
        } else if (!vote.getVotedFor().equals(referendumVoteInputDTO.getVotedFor())) {
            writeLedgerEntry();
            vote.changeVote(referendumVoteInputDTO.getVotedFor());
        }

        if (!referendumVoteInputDTO.getVotedFor() && referendum.getType().isRequireReasonWhenVotingAgainst()) {
            saveVoteReason(vote);
        } else {
            vote.setReason(null);
            vote.setCommentReplyOid(null);
        }

        if (referendum.getType().isTribunalReferendum() && referendum.isWasVotedOnByAllTribunalMembers()) {
            endTribunalReferendum();
        }

        return vote;
    }

    protected void endTribunalReferendum() {
        referendum.setEndDatetime(now());

        getAreaContext().doAreaTask(new EndReferendumTask(referendum));
    }

    protected void saveVoteReason(ReferendumVote vote) {
        vote.setReason(referendumVoteInputDTO.getReason());

        if (!isEmpty(referendumVoteInputDTO.getComment())) {
            OID replyOid = getNetworkContext().doCompositionTask(referendum.getCompositionPartition(), new CompositionTaskImpl<OID>() {
                @Override
                protected OID doMonitoredTask() {
                    Composition composition = Composition.dao().get(referendum.getOid());
                    Reply reply = new Reply(composition);
                    reply.setBody(referendumVoteInputDTO.getComment());

                    return getAreaContext().doAreaTask(new CreateCommentTask(referendum, reply, null));
                }
            });
            vote.setCommentReplyOid(replyOid);

        } else {
            vote.setCommentReplyOid(null);
        }
    }

    protected void writeLedgerEntry() {
        LedgerEntry entry = new LedgerEntry(getAreaContext().getAreaUserRlm(), referendum.getType().isTribunalReferendum() ? LedgerEntryType.ISSUE_REFERENDUM_VOTE : LedgerEntryType.NICHE_REFERENDUM_VOTE);
        entry.setChannel(referendum.getChannel());
        entry.setReferendum(referendum);
        entry.setActor(getAreaContext().getAreaUserRlm());
        ReferendumVoteLedgerEntryMetadata metadata = entry.getMetadata();
        metadata.setVoteForReferendum(referendumVoteInputDTO.getVotedFor());
        networkContext().doGlobalTask(new SaveLedgerEntryTask(entry));
    }
}
