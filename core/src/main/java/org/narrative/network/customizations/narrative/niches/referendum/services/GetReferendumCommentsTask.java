package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/28/18
 * Time: 11:22 AM
 */
public class GetReferendumCommentsTask extends CompositionTaskImpl<List<Reply>> {
    private final Referendum referendum;
    private final int lastLikeCount;
    private final Timestamp lastReplyDatetime;

    public static final int RESULTS_PER_FETCH = 5;

    public GetReferendumCommentsTask(Referendum referendum, Integer lastLikeCount, Timestamp lastReplyDatetime) {
        super(false);
        this.referendum = referendum;
        this.lastLikeCount = lastLikeCount != null ? lastLikeCount : Integer.MAX_VALUE;
        this.lastReplyDatetime = lastReplyDatetime != null ? lastReplyDatetime : new Timestamp(0);
    }

    @Override
    protected List<Reply> doMonitoredTask() {
        List<Reply> replies = Reply.dao().getRepliesForReferendum(referendum, lastLikeCount, lastReplyDatetime, RESULTS_PER_FETCH);

        Reply.dao().primeUsersForReplies(replies);

        return replies;
    }
}
