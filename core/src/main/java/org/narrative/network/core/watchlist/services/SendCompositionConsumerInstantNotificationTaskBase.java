package org.narrative.network.core.watchlist.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.SubListIterator;
import org.narrative.common.util.Timer;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.user.WatchedUser;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.MobilePushNotificationTask;
import org.narrative.network.shared.context.NetworkContextInternal;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Jun 13, 2006
 * Time: 10:30:03 AM
 */
public abstract class SendCompositionConsumerInstantNotificationTaskBase<T extends CompositionConsumer> extends CompositionTaskImpl<Object> implements MobilePushNotificationTask {
    private static final NetworkLogger logger = new NetworkLogger(SendCompositionConsumerInstantNotificationTaskBase.class);

    protected T compositionConsumer;
    protected final boolean isEdit;

    private final boolean isForReply;

    protected User user;

    protected SendCompositionConsumerInstantNotificationTaskBase(T compositionConsumer, boolean isEdit, boolean isForReply) {
        super(false);
        this.compositionConsumer = compositionConsumer;
        this.isEdit = isEdit;
        this.isForReply = isForReply;
    }

    protected void reassociateCachedObjectsAfterSessionClear() {
        compositionConsumer = ((NetworkDAOImpl<T, OID>) (compositionConsumer.getCompositionConsumerType().getCompositionType().getDAO())).get(compositionConsumer.getOid());
        // bl: the user is only set on each iteration of emails being sent, so just clear it out
        user = null;
    }

    public final Object doMonitoredTask() {
        AuthZone authZone = compositionConsumer.getAuthZone();
        sendInstantNotification();
        return null;
    }

    protected abstract void sendInstantNotification();

    /**
     * bl: we ran into performance issues with huge sessions when sending notifications to tens of
     * thousands of users. this method allows us to chunk through the users while clearing the session
     * along the way to solve the issue of performance and session explosion.
     *
     * @param userOids the complete list of userOids to send the notification to
     */
    protected void sendChunkedNotification(List<OID> userOids) {
        Timer timer = new Timer(logger, getProcess());
        timer.start("Sending notification to " + userOids.size() + " users for compositionConsumer/" + compositionConsumer.getOid() + " isForReply/" + isForReply);
        Iterator<List<OID>> iter = new SubListIterator<>(userOids, 200);
        int count = 0;
        while (iter.hasNext()) {
            List<OID> userOidChunk = iter.next();
            List<User> users = User.dao().getObjectsFromIDsWithCache(userOidChunk);
            for (User user : users) {
                this.user = user;
                user.sendInstantNotification(this);
            }

            // bl: this should be a read-only session. nevertheless, let's flush just in case. doesn't hurt.
            PartitionGroup.getCurrentPartitionGroup().flushAllSessions();
            PartitionGroup.getCurrentPartitionGroup().clearAllSessions();
            // for each chunk, we need to reassociate the cached context on the NetworkContext
            ((NetworkContextInternal) getNetworkContext()).reassociateCachedContextAfterSessionClear();
            // bl: any properties that may have been set on the task should be refreshed now that
            // we have cleared the session and evicted those objects from the session.
            reassociateCachedObjectsAfterSessionClear();
            count += userOidChunk.size();
            getProcess().updateStatusMessageAndLog(logger, "Sent notification to " + count + " users out of " + userOids.size() + " for compositionConsumer/" + compositionConsumer.getOid() + " isForReply/" + isForReply);
        }
        timer.finish();
    }

    protected Set<OID> getGloballyWatchingUsers(Area area, User author, boolean isForReply) {
        return null;
    }

    protected Collection<OID> getUserOidsToNotify(final User author) {
        if (!compositionConsumer.isLive()) {
            return Collections.emptyList();
        }

        final Area area = compositionConsumer.getArea();

        Set<OID> allUserOids = new HashSet<>();
        {
            Set<OID> watchingUserOids = getGloballyWatchingUsers(area, author, isForReply);
            if (watchingUserOids != null) {
                allUserOids.addAll(watchingUserOids);
            }
        }

        // jw: remove any blocking users so that they do not receive this email notification!
        if (exists(author)) {
            // jw: now lets handle removing any subscribers that are blocking this author!
            Collection<OID> userOids = WatchedUser.dao().getUserOidsBlockingUser(author);
            if (!userOids.isEmpty()) {
                allUserOids.removeAll(userOids);
            }
        }

        return new HashSet<>(allUserOids);
    }

    public T getCompositionConsumer() {
        return compositionConsumer;
    }

    public boolean isEdit() {
        return isEdit;
    }

    @Override
    public String getNotificationMessage() {
        return null;
    }

}
