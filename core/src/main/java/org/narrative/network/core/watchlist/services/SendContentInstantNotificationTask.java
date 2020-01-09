package org.narrative.network.core.watchlist.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.mentions.MentionsUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Jun 13, 2006
 * Time: 10:35:24 AM
 */
public class SendContentInstantNotificationTask extends SendContentInstantNotificationTaskBase {
    private Composition composition;

    public SendContentInstantNotificationTask(Content content, boolean isEdit) {
        super(content, isEdit, false);
    }

    @Override
    protected void reassociateCachedObjectsAfterSessionClear() {
        super.reassociateCachedObjectsAfterSessionClear();
        composition = Composition.dao().get(composition.getOid());
    }

    protected void sendInstantNotification() {
        composition = Composition.dao().get(getContent().getOid());

        if (!exists(composition)) {
            return;
        }

        List<OID> userOids = getUserOidsToNotifyAfterSkips(getContent().getUser());
        sendChunkedNotification(userOids);
    }

    @Override
    protected Collection<OID> getSkipMemberOids() {
        if (!isEdit() && getContent().getCompositionConsumerType().isSupportsMentions()) {
            // jw: never send new content notifications to members who were mentioned in the post (those members who should
            //     receive a mention notification)
            return MentionsUtil.getMentionedMemberOidsToNotify(composition.getBody());
        }
        return Collections.emptyList();
    }

    public Composition getComposition() {
        return composition;
    }

    @Override
    public String getNotificationMessage() {
        Content content = getContent();

        return wordlet("jsp.site.email.emailNewContent.newPostByAuthorSubject", content.getPrimaryRole().getDisplayNameResolved(), content.getShortDisplaySubject());
    }

    @Override
    public String getTargetUrl() {
        return getContent().getPermalinkUrl();
    }

}
