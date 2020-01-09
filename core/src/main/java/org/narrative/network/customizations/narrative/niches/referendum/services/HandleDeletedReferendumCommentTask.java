package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.customizations.narrative.service.impl.comment.CommentTaskBase;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-14
 * Time: 11:10
 *
 * @author jonmark
 */
public class HandleDeletedReferendumCommentTask extends CommentTaskBase<Object> {
    private final Referendum referendum;

    public HandleDeletedReferendumCommentTask(Referendum referendum, Reply reply) {
        super(referendum, reply);
        this.referendum = referendum;
    }

    @Override
    protected Object doMonitoredTask() {
        ReferendumVote commentReferendumVote = ReferendumVote.dao().getForReferendumAndComment(referendum, reply);
        // if there is a vote associated with this comment, clear it out
        if(exists(commentReferendumVote)) {
            commentReferendumVote.setCommentReplyOid(null);
        }

        return null;
    }
}
