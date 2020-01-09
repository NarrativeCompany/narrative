package org.narrative.network.core.watchlist.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.mentions.MentionsUtil;

import java.util.Collection;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Jun 13, 2006
 * Time: 10:35:31 AM
 */
public class SendReplyInstantNotificationTask extends SendContentInstantNotificationTaskBase {
    private OID replyOid;
    private Reply reply;

    public SendReplyInstantNotificationTask(Content content, OID replyOid) {
        super(content, false, true);
        this.replyOid = replyOid;
    }

    @Override
    protected void reassociateCachedObjectsAfterSessionClear() {
        super.reassociateCachedObjectsAfterSessionClear();
        reply = Reply.dao().get(replyOid);
    }

    protected void sendInstantNotification() {
        reply = Reply.dao().get(replyOid);

        if (!exists(reply)) {
            return;
        }

        List<OID> userOids = getUserOidsToNotifyAfterSkips(reply.getUser());
        sendChunkedNotification(userOids);
    }

    @Override
    protected Collection<OID> getSkipMemberOids() {
        // jw: by default, never send a notification of a reply to someone who should receive a mention email.
        return MentionsUtil.getMentionedMemberOidsToNotify(reply.getBody());
    }

    public Reply getReply() {
        return reply;
    }

    @Override
    public String getNotificationMessage() {
        return wordlet("jsp.site.email.emailNewReply.subjectForEmail", getReply().getPrimaryRole().getDisplayNameResolved(), getContent().getShortDisplaySubject());
    }

    @Override
    public String getTargetUrl() {
        return getCompositionConsumer().getDisplayReplyUrl(getReply().getOid());
    }

}
