package org.narrative.network.core.area.user;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.Proxy;
import org.hibernate.validator.constraints.Length;
import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.user.dao.AreaUserDAO;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.security.area.community.advanced.AreaCircleUser;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.preferences.AreaNotificationType;
import org.narrative.network.core.user.services.preferences.NotificationSettingsHelper;
import org.narrative.network.shared.security.PrimaryRole;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:26:59 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {AreaUser.FIELD__USER__COLUMN, AreaUser.FIELD__AREA__COLUMN})})
public class AreaUser extends AreaRole implements DAOObject<AreaUserDAO> {
    private OID oid;
    private User user;
    private Area area;
    private AreaUserStats areaUserStats;
    // todo: change the name of createdDatetime.  it now really is an "activity" datetime.
    private Timestamp createdDatetime;

    private Map<AreaCircle, AreaCircleUser> areaCircleUsers;

    private int followerCount;

    // bl: denormed from User for performance when querying member list
    private String displayName;

    private AreaUserPreferences preferences;

    private transient NotificationSettingsHelper<AreaNotificationType> notificationSettingsHelper;

    public static final String FIELD__USER__NAME = "user";
    public static final String FIELD__AREA_USER_STATS__NAME = "areaUserStats";
    public static final String FIELD__AREA__NAME = "area";
    public static final String FIELD__CREATED_DATETIME__NAME = "createdDatetime";
    public static final String FIELD__DISPLAY_NAME__NAME = "displayName";

    public static final String FIELD__AREA_CIRCLE_USERS__NAME = "areaCircleUsers";

    public static final String FIELD__USER__COLUMN = FIELD__USER__NAME + "_" + User.FIELD__OID__NAME;
    public static final String FIELD__AREA__COLUMN = FIELD__AREA__NAME + "_" + Area.FIELD__OID__NAME;

    /**
     * @deprecated for hibernate use only
     */
    public AreaUser() {}

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    public AreaUser(Area area, User user) {
        assert isEqual(area.getAuthZone(), user.getAuthZone()) : "Should never have an AuthZone mismatch when creating an AreaUser!";
        this.area = area;
        this.user = user;
        // NO! this is VERY bad. the default admin for metadata sites has nearly 40k AreaUser records.
        // we should never attempt to get all of the AreaUser records for a single user!
        //this.user.getAreaUsers().add(this);
        this.areaUserStats = new AreaUserStats(this);
        this.createdDatetime = new Timestamp(System.currentTimeMillis());
        this.areaCircleUsers = new HashMap<>();
        // bl: only invalidate if the area and user already exist.  if they don't exist yet,
        // then there's no possible way there could be anything to invalidate.
        if (area.getOid() != null && user.getOid() != null) {
            // invalidating the cache will ensure that any previous cached null value
            // will be cleared and the new AreaUser record will be cached.
            AreaUserDAO.invalidateAreaUserCache(area.getOid(), user.getOid());
        }
        this.displayName = user.getDisplayName();

        this.preferences = new AreaUserPreferences(true);

        getNotificationSettingsHelper().setDefaultNotificationPreferences();
    }

    /**
     * needs to be an optional association in order to avoid a circular, not-null foreign key relationship in the database
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @ForeignKey(name = "FKA08B86187EB27D35")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @OneToMany(mappedBy = AreaCircleUser.FIELD__AREA_USER__NAME, fetch = FetchType.LAZY, cascade = javax.persistence.CascadeType.ALL)
    @MapKey(name = AreaCircleUser.FIELD__AREA_CIRCLE__NAME)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Map<AreaCircle, AreaCircleUser> getAreaCircleUsers() {
        return areaCircleUsers;
    }

    public void setAreaCircleUsers(Map<AreaCircle, AreaCircleUser> areaCircleUsers) {
        this.areaCircleUsers = areaCircleUsers;
    }

    @Transient
    public Map<AreaCircle, AreaCircleUser> getAreaCircleUsersInited() {
        return initHibernateMap(getAreaCircleUsers());
    }

    @Transient
    public void addToAreaCircle(AreaCircle areaCircle) {
        assert isEqual(areaCircle.getArea(), getArea()) : "Area mismatch when adding user to group!";

        if (!exists(areaCircle)) {
            return;
        }

        Map<AreaCircle, AreaCircleUser> areaCircleUsers = getAreaCircleUsersInited();
        if (areaCircleUsers.containsKey(areaCircle)) {
            return;
        }

        AreaCircleUser areaCircleUser = new AreaCircleUser(areaCircle, this);
        AreaCircleUser.dao().save(areaCircleUser);
        areaCircleUsers.put(areaCircle, areaCircleUser);

        // jw: lets clear the effectiveAreaCircles in case something needs to use that after this change is made!
        effectiveAreaCircles = null;
    }

    @Transient
    public boolean removeFromAreaCircle(AreaCircle areaCircle) {
        assert isEqual(areaCircle.getArea(), getArea()) : "Area mismatch when removing user from group!";

        AreaCircleUser areaCircleUser = getAreaCircleUsersInited().remove(areaCircle);
        if (!exists(areaCircleUser)) {
            return false;
        }

        AreaCircleUser.dao().delete(areaCircleUser);

        // jw: lets clear the effectiveAreaCircles in case something needs to use that after this change is made!
        effectiveAreaCircles = null;

        return true;
    }

    @Transient
    public PrimaryRole getPrimaryRole() {
        return getUser();
    }

    private transient Set<AreaCircle> effectiveAreaCircles;

    @Override
    @Transient
    public Set<AreaCircle> getEffectiveAreaCircles() {
        if (effectiveAreaCircles == null) {
            Set<AreaCircle> results = newLinkedHashSet();
            if (isActiveRegisteredAreaUser()) {
                // jw: add any circles the member was explicitly joined to.
                results.addAll(getAreaCircleUsersInited().keySet());
            }

            effectiveAreaCircles = Collections.unmodifiableSet(results);
        }

        return effectiveAreaCircles;
    }

    @Transient
    public static AreaUserRlm getAreaUserRlm(AreaUser areaUser) {
        return AreaUserRlm.dao().get(areaUser.getOid());
    }

    /**
     * @return
     * @deprecated Use AreaUser.getAreaUserRlm(areaUser) or NetworkCoreUtils.getAreaUserRlm(areaUser).  This will
     * prevent a proxy read in the case where AreaUser is an unresolved proxy
     */
    @Transient
    public AreaUserRlm getAreaUserRlm() {
        return AreaUserRlm.dao().get(getOid());
    }

    @NotNull
    @Index(name = "area_user_created_datetime_idx")
    //@Temporal(TemporalType.TIMESTAMP)
    public Timestamp getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(Timestamp createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

    @Transient
    public boolean isWatchForSomeoneFollowingMe() {
        return getNotificationSettingsHelper().isNotificationSet(AreaNotificationType.SOMEONE_FOLLOWED_ME);
    }

    /**
     * get the AreaUserStats associated with this AreaUser. note that since we can lock AreaUser objects
     * at any point in time (e.g. to update stats synchronously), we do _not_ want to cascade here on refresh.
     * this was causing issues where we'd load and lock an AreaUserStats object, then we'd attempt to refresh
     * the AreaUser object for LockMode.PESSIMISTIC_WRITE. the problem is that the refresh command was cascading through
     * to AreaUserStats (due to CascadeType.ALL), which was ultimately causing AreaUserStats to be refreshed
     * to the state in the database with a LockMode.READ. then, we'd subsequently change the AreaUserStats object
     * and ultimately try to lock it again. then, we'd get a dirty object error.
     * so, the real fix is to just never cascade from AreaUser->AreaUserStats on refresh. and boom; done!
     *
     * @return the AreaUserStats associated with this AreaUser.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @JoinColumn(name = AreaUser.FIELD__OID__NAME)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    public AreaUserStats getAreaUserStats() {
        return areaUserStats;
    }

    public void setAreaUserStats(AreaUserStats areaUserStats) {
        this.areaUserStats = areaUserStats;
    }

    @Transient
    public boolean isRegisteredUser() {
        return true;
    }

    @Transient
    public AreaUser getAreaUser() {
        return this;
    }

    @Transient
    public boolean isAnAreaUser() {
        return true;
    }

    @Transient
    public boolean isActiveRegisteredAreaUser() {
        return getUser().isActive();
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "FKA08B861810FFDA37")
    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    @Transient
    public AreaRlm getAreaRlm() {
        return Area.getAreaRlm(area);
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    @Index(name = "areaUser_displayName_idx")
    @NotNull
    @Length(min = User.MIN_DISPLAY_NAME_LENGTH, max = User.MAX_DISPLAY_NAME_LENGTH)
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @NotNull
    public AreaUserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(AreaUserPreferences preferences) {
        this.preferences = preferences;
    }

    @Transient
    public NotificationSettingsHelper<AreaNotificationType> getNotificationSettingsHelper() {
        if (notificationSettingsHelper == null) {
            notificationSettingsHelper = new NotificationSettingsHelper<>(getPreferences());
        }
        return notificationSettingsHelper;
    }

    @Override
    @Transient
    public String getDisplayNameResolved() {
        return getUser().getDisplayNameResolved();
    }

    public static AreaUserDAO dao() {
        return DAOImpl.getDAO(AreaUser.class);
    }
}
