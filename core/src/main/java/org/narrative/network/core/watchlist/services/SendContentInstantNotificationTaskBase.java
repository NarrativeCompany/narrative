package org.narrative.network.core.watchlist.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 9/21/11
 * Time: 3:29 PM
 *
 * @author brian
 */
public abstract class SendContentInstantNotificationTaskBase extends SendCompositionConsumerInstantNotificationTaskBase<Content> {
    protected SendContentInstantNotificationTaskBase(Content content, boolean isEdit, boolean isForReply) {
        super(content, isEdit, isForReply);
    }

    protected Collection<OID> getSkipMemberOids() {
        return Collections.emptyList();
    }

    protected List<OID> getUserOidsToNotifyAfterSkips(User author) {
        Collection<OID> userOids = getUserOidsToNotify(author);
        Collection<OID> skipUserOids = getSkipMemberOids();
        if (!isEmptyOrNull(skipUserOids)) {
            userOids = new HashSet<>(userOids);
            userOids.removeAll(skipUserOids);
        }
        return new ArrayList<>(userOids);
    }

    public Content getContent() {
        return getCompositionConsumer();
    }

}
