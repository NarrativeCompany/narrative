package org.narrative.network.core.area.user.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.user.WatchedUser;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserStatus;
import org.narrative.network.customizations.narrative.service.api.model.FollowScrollParamsDTO;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 5/31/12
 * Time: 10:58 AM
 * User: jonmark
 */
public class WatchedUserDAO extends GlobalDAOImpl<WatchedUser, OID> {
    public WatchedUserDAO() {
        super(WatchedUser.class);
    }

    public WatchedUser getForUserWatchingUser(User watcherUser, User watchedUser) {
        return getUniqueBy(newNameValuePair(WatchedUser.FIELD__WATCHER_USER__NAME, watcherUser), newNameValuePair(WatchedUser.FIELD__WATCHED_USER__NAME, watchedUser));
    }

    public List<OID> getUserOidsBlockingUser(User user) {
        return getGSession().getNamedQuery("watchedUser.getAllUserOidsBlockingUser").setParameter("blockedUser", user).list();
    }

    public int deleteAllForUser(User user) {
        return getGSession().getNamedQuery("watchedUser.deleteAllForUser").setParameter("user", user).executeUpdate();
    }

    public List<User> getUsersWatchingUser(User watchedUser, FollowScrollParamsDTO params, int maxResults) {
        assert exists(watchedUser) : "Expect to get a user at this point!";

        return executeWatchQuery(
                getGSession().createNamedQuery("watchedUser.getUsersWatchingUser", User.class).setParameter("watchedUser", watchedUser),
                params,
                maxResults
        );
    }

    public List<User> getUsersWatchedByUser(User watcherUser, FollowScrollParamsDTO params, int maxResults) {
        assert exists(watcherUser) : "Expect to get a user at this point!";

        return executeWatchQuery(
                getGSession().createNamedQuery("watchedUser.getUsersWatchedByUser", User.class).setParameter("watcherUser", watcherUser),
                params,
                maxResults
        );
    }

    private List<User> executeWatchQuery(Query<User> query, FollowScrollParamsDTO params, int maxResults) {
        return query
                .setParameter("lastName", params == null ? null : params.getLastItemName())
                .setParameter("lastOid", params == null ? null : params.getLastItemOid())
                .setMaxResults(maxResults)
                .list();
    }

    public List<User> getUsersInListWatchedByUser(User watcher, List<User> users) {
        if (isEmptyOrNull(users)) {
            return Collections.emptyList();
        }

        return getGSession().createNamedQuery("watchedUser.getUsersInListWatchedByUser", User.class)
                .setParameter("watcher", watcher)
                .setParameterList("users", users)
                .list();
    }

    public int getTotalWatcherCountForUser(User watched) {
        return getGSession().createNamedQuery("watchedUser.getTotalWatcherCountForUser", Number.class)
                .setParameter("watched", watched)
                .uniqueResult().intValue();
    }
}
