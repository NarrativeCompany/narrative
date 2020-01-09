package org.narrative.network.core.watchlist.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.Reply;

import java.util.ArrayList;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 9/28/11
 * Time: 1:22 PM
 *
 * @author Jonmark Weber
 */
public abstract class SendCompositionConsumerCommentInstantNotificationTaskBase<T extends CompositionConsumer> extends SendCompositionConsumerInstantNotificationTaskBase<T> {
    private final OID replyOid;
    private Reply reply;

    public SendCompositionConsumerCommentInstantNotificationTaskBase(T compositionConsumer, OID replyOid) {
        // bl: we never send edited comment/reply notifications
        super(compositionConsumer, false, true);
        assert replyOid != null : "Should always have a reply for composition consumer comment notifications!";
        this.replyOid = replyOid;
    }

    @Override
    protected void reassociateCachedObjectsAfterSessionClear() {
        super.reassociateCachedObjectsAfterSessionClear();
        reply = Reply.dao().get(replyOid);
    }

    @Override
    protected void sendInstantNotification() {
        reply = Reply.dao().get(replyOid);

        if (!exists(reply)) {
            return;
        }

        List<OID> userOids = new ArrayList<>(getUserOidsToNotify(reply.getUser()));
        sendChunkedNotification(userOids);
    }

    public Reply getReply() {
        return reply;
    }

    @Override
    public String getTargetUrl() {
        return getCompositionConsumer().getDisplayReplyUrl(getReply().getOid());
    }
}
