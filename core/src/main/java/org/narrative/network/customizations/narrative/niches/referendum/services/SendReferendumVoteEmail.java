package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.NarrativeCircleType;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.customizations.narrative.niches.services.SendBulkNarrativeEmailTaskBase;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 4/3/18
 * Time: 9:08 AM
 */
public class SendReferendumVoteEmail extends SendBulkNarrativeEmailTaskBase {
    private ReferendumVote vote;

    public SendReferendumVoteEmail(ReferendumVote vote) {
        this.vote = vote;
    }

    @Override
    protected List<OID> getUserOidsToNotify() {
        assert vote.getReferendum().getType().isTribunalReferendum() : "Should only ever send vote emails for tribunal referendums!";

        // jw: the baseline of this is anyone watching the referendum for votes
        List<OID> watcherUserOids = FollowedChannel.dao().getUserOidsInCircleFollowing(
                vote.getReferendum().getChannel(),
                NarrativeCircleType.TRIBUNAL.getCircle(getNetworkContext().getAuthZone())
        );
        // jw: do not notify the user about their own vote!
        watcherUserOids.remove(vote.getVoter().getUser().getOid());

        return watcherUserOids;
    }

    @Override
    protected void setupForChunk(List<User> users) {
        vote = ReferendumVote.dao().get(vote.getOid());
    }

    public ReferendumVote getVote() {
        return vote;
    }
}
