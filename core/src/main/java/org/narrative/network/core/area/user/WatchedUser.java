package org.narrative.network.core.area.user;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.area.user.dao.WatchedUserDAO;
import org.narrative.network.core.user.User;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 5/31/12
 * Time: 10:58 AM
 * User: jonmark
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {WatchedUser.FIELD__WATCHER_USER__COLUMN, WatchedUser.FIELD__WATCHED_USER__COLUMN})})
public class WatchedUser implements DAOObject<WatchedUserDAO> {
    private OID oid;

    private User watcherUser;
    private User watchedUser;

    private Timestamp watchDatetime;
    private boolean blocked;

    public static final String FIELD__WATCHER_USER__NAME = "watcherUser";
    public static final String FIELD__WATCHER_USER__COLUMN = FIELD__WATCHER_USER__NAME + "_" + User.FIELD__OID__NAME;
    public static final String FIELD__WATCHED_USER__NAME = "watchedUser";
    public static final String FIELD__WATCHED_USER__COLUMN = FIELD__WATCHED_USER__NAME + "_" + User.FIELD__OID__NAME;
    public static final String FIELD__BLOCKED__NAME = "blocked";

    @Deprecated
    public WatchedUser() {}

    public WatchedUser(User watcherUser, User watchedUser) {
        assert isEqual(watcherUser.getAuthZone(), watchedUser.getAuthZone()) : "A user should only watch a user on the same AuthZone.";
        assert !isEqual(watcherUser, watchedUser) : "A user should never watch themselves!";

        this.watcherUser = watcherUser;
        this.watchedUser = watchedUser;
        this.watchDatetime = new Timestamp(System.currentTimeMillis());
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_watchedUser_watcherUser")
    public User getWatcherUser() {
        return watcherUser;
    }

    public void setWatcherUser(User watcherUser) {
        this.watcherUser = watcherUser;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_watchedUser_watchedUser")
    public User getWatchedUser() {
        return watchedUser;
    }

    public void setWatchedUser(User watchedUser) {
        this.watchedUser = watchedUser;
    }

    @NotNull
    public Timestamp getWatchDatetime() {
        return watchDatetime;
    }

    public void setWatchDatetime(Timestamp watchDatetime) {
        this.watchDatetime = watchDatetime;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public static WatchedUserDAO dao() {
        return DAOImpl.getDAO(WatchedUser.class);
    }
}
