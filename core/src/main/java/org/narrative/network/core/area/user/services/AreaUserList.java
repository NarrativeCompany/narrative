package org.narrative.network.core.area.user.services;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jetbrains.annotations.Nullable;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.LengthOrder;
import org.narrative.common.persistence.hibernate.MentionsFieldOrder;
import org.narrative.common.persistence.hibernate.MentionsLengthOrder;
import org.narrative.common.persistence.hibernate.StartsWithOrder;
import org.narrative.common.persistence.hibernate.StringPositionOrder;
import org.narrative.common.persistence.hibernate.criteria.CriteriaList;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserStats;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.security.area.community.advanced.AreaCircleUser;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserFields;
import org.narrative.network.core.user.UserStatus;
import org.narrative.network.core.user.services.UserSortField;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Mar 13, 2006
 * Time: 2:16:10 PM
 */
public class AreaUserList extends GlobalTaskImpl<List<AreaUser>> implements CriteriaList<AreaUser, AreaUserList.SortField> {
    public static enum SortField implements UserSortField {
        JOIN_DATETIME,
        LAST_LOGIN_DATETIME,
        DISPLAY_NAME,
        MENTIONS_ORDERING,
        DISPLAY_NAME_LENGTH,
        MATCH_ACCURACY;

        public String getNameForDisplay() {
            return wordlet("areaUserList.sortField." + this);
        }

        public boolean isSortDescByDefault() {
            // bl: sort by newest members first for join and last login. everything else is asc by default.
            return isJoinDatetime() || isLastLoginDatetime();
        }

        public boolean isJoinDatetime() {
            return this == JOIN_DATETIME;
        }

        public boolean isLastLoginDatetime() {
            return this == LAST_LOGIN_DATETIME;
        }

        public boolean isDisplayName() {
            return this == DISPLAY_NAME;
        }

    }

    private SortField sort = SortField.JOIN_DATETIME;
    private boolean sortAsc = false;
    private final Area area;

    // be sure to add any new fields to the isHasFilterSet() implementation
    private String displayNameLike;

    private String displayNameAfter;

    private String mentionsFilter;

    private String displayNameStartsWith;
    private String generalNameLike;
    private boolean includeEmailAddressInGeneralNameSearch;

    private Timestamp lastLoginAfter;
    private Timestamp lastLoginBefore;
    private Timestamp registeredAfter;
    private Timestamp registeredBefore;
    private Collection<AreaCircle> areaCircles;
    private Boolean inAllAreaCircles;

    private Criteria areaUserCriteria;
    private Criteria userCriteria;
    private Criteria userEmailAddressCriteria;
    private Criteria areaUserStatsCriteria;
    private Criteria profileCriteria;
    private Criteria areaCircleUserCriteria;

    private boolean forSystemProcessing = false;

    private boolean doCount = false;
    private boolean fetchCountOnly = false;
    private Integer count;

    private int page = 1;
    private int rowsPerPage;

    private boolean fetchOidsOnly = false;
    private List<OID> areaUserOids;

    private boolean fetchUserOidsOnly = false;
    private List<OID> userOids;

    public AreaUserList(Area area) {
        this.area = area;
    }

    public boolean isForceWritable() {
        return false;
    }

    protected Criteria getAreaUserCriteria() {
        if (areaUserCriteria == null) {
            areaUserCriteria = getNetworkContext().getGlobalSession().getSession().createCriteria(AreaUser.class, "au");
        }
        return areaUserCriteria;
    }

    protected Criteria getUserCriteria() {
        if (userCriteria == null) {
            Criteria c = getAreaUserCriteria();
            userCriteria = c.createCriteria(HibernateUtil.makeName(c, AreaUser.FIELD__USER__NAME), "u");
        }
        return userCriteria;
    }

    protected Criteria getUserEmailAddressCriteria() {
        if (userEmailAddressCriteria == null) {
            userEmailAddressCriteria = getUserCriteria().createCriteria(HibernateUtil.makeName(getUserCriteria(), User.getUserFieldNestedPropertyName(UserFields.FIELD__EMAIL_ADDRESS__NAME)), "u_email");
        }
        return userEmailAddressCriteria;
    }

    protected Criteria getAreaUserStatsCriteria() {
        if (areaUserStatsCriteria == null) {
            Criteria c = getAreaUserCriteria();
            areaUserStatsCriteria = c.createCriteria(HibernateUtil.makeName(c, AreaUser.FIELD__AREA_USER_STATS__NAME), "aus");
        }
        return areaUserStatsCriteria;
    }

    protected Criteria getProfileCriteria() {
        if (profileCriteria == null) {
            Criteria c = getUserCriteria();
            profileCriteria = c.createCriteria(HibernateUtil.makeName(c, User.FIELD__PROFILE__NAME), "p");
        }
        return profileCriteria;
    }

    protected Criteria getAreaCircleUserCriteria() {
        if (areaCircleUserCriteria == null) {
            Criteria c = getAreaUserCriteria();
            areaCircleUserCriteria = c.createCriteria(HibernateUtil.makeName(c, AreaUser.FIELD__AREA_CIRCLE_USERS__NAME), "agu");
        }
        return areaCircleUserCriteria;
    }

    protected void addAreaCircleRestriction(AreaCircle areaCircle) {
        if (!exists(areaCircle)) {
            return;
        }

        getAreaUserCriteria().add(Restrictions.sqlRestriction(" {alias}.oid in ( select " + AreaCircleUser.FIELD__AREA_USER__COLUMN + " from " + AreaCircleUser.class.getSimpleName() + " acu" + " where acu." + AreaCircleUser.FIELD__AREA_CIRCLE__COLUMN + " = " + areaCircle.getOid() + " )"));
    }

    protected List<AreaUser> doMonitoredTask() {
        Criteria auc = getAreaUserCriteria();
        auc.add(Restrictions.eq(AreaUser.FIELD__AREA__NAME, area));

        getUserCriteria().add(Restrictions.sqlRestriction("{alias}." + User.FIELD__USER_STATUS__NAME + "=" + UserStatus.ACTIVE.getBitmask()));

        if (!isEmpty(displayNameLike)) {
            auc.add(Restrictions.ilike(HibernateUtil.makeName(auc, AreaUser.FIELD__DISPLAY_NAME__NAME), PersistenceUtil.MySQLUtils.getStringAfterEscapingWildcardCharsForHqlLikePattern(displayNameLike), MatchMode.ANYWHERE));
        }

        if (!isEmpty(displayNameAfter)) {
            if (sort == null || !sort.isDisplayName()) {
                throw UnexpectedError.getRuntimeException("Should only ever use displayNameAfter when sorting by displayName");
            }

            if (sortAsc) {
                auc.add(Restrictions.gt(HibernateUtil.makeName(auc, AreaUser.FIELD__DISPLAY_NAME__NAME), displayNameAfter));
            } else {
                auc.add(Restrictions.lt(HibernateUtil.makeName(auc, AreaUser.FIELD__DISPLAY_NAME__NAME), displayNameAfter));
            }
        }

        if (!isEmpty(generalNameLike)) {
            Disjunction d = Restrictions.disjunction();
            String filterString = PersistenceUtil.MySQLUtils.getStringAfterEscapingWildcardCharsForHqlLikePattern(generalNameLike);
            d.add(Restrictions.ilike(HibernateUtil.makeName(auc, AreaUser.FIELD__DISPLAY_NAME__NAME), filterString, MatchMode.ANYWHERE));
            if (includeEmailAddressInGeneralNameSearch) {
                d.add(Restrictions.ilike(HibernateUtil.makeName(getUserEmailAddressCriteria(), EmailAddress.Fields.emailAddress), filterString, MatchMode.START));
            }
            auc.add(d);
        }

        if (!isEmpty(mentionsFilter)) {
            String filterString = PersistenceUtil.MySQLUtils.getStringAfterEscapingWildcardCharsForHqlLikePattern(mentionsFilter);

            Disjunction filterCriteria = Restrictions.disjunction();
            filterCriteria.add(Restrictions.ilike(HibernateUtil.makeName(getUserCriteria(), User.FIELD__USERNAME__NAME), filterString, MatchMode.START));
            filterCriteria.add(Restrictions.ilike(HibernateUtil.makeName(getUserCriteria(), User.FIELD__DISPLAY_NAME__NAME), filterString, MatchMode.START));
            getUserCriteria().add(filterCriteria);
        }

        if (!isEmpty(displayNameStartsWith)) {
            String filterString = PersistenceUtil.MySQLUtils.getStringAfterEscapingWildcardCharsForHqlLikePattern(displayNameStartsWith);
            auc.add(Restrictions.ilike(HibernateUtil.makeName(auc, AreaUser.FIELD__DISPLAY_NAME__NAME), filterString, MatchMode.START));
        }

        HibernateUtil.addDatetimeRangeCriteria(getAreaUserStatsCriteria(), AreaUserStats.FIELD__LAST_LOGIN_DATETIME__NAME, lastLoginAfter, lastLoginBefore);

        HibernateUtil.addDatetimeRangeCriteria(auc, AreaUser.FIELD__CREATED_DATETIME__NAME, registeredAfter, registeredBefore);

        if (!isEmptyOrNull(areaCircles)) {
            // bl: we only need to take this approach if there is more than one areaCircle!
            if (inAllAreaCircles != null && inAllAreaCircles && areaCircles.size() > 1) {
                for (AreaCircle areaCircle : areaCircles) {
                    addAreaCircleRestriction(areaCircle);
                }
            } else {
                getAreaCircleUserCriteria().add(Restrictions.in(HibernateUtil.makeName(getAreaCircleUserCriteria(), AreaCircleUser.FIELD__AREA_CIRCLE__NAME), areaCircles));
            }
        }

        //do we need a count
        if (doCount) {
            // jw: as mentioned in the comment within isHasFilterSet below, we need to check the status here, if results
            //     are restricted to a specific "Queue" then we also need to generate the count here from the criteria.
            if (isHasFilterSet()) {
                auc.setProjection(Projections.rowCount());
                count = ((Number) auc.uniqueResult()).intValue();
                auc.setProjection(null);
            } else {
                // bl: optimization so that if there is no filter set, we'll just use the site's memberCount instead
                // of having to query for it.  this will improve performance dramatically on large sites.
                count = area.getAreaStats().getMemberCount();
            }
            if (fetchCountOnly) {
                return null;
            }
        }

        if (sort != null) {
            switch (sort) {
                case JOIN_DATETIME:
                    auc.addOrder(HibernateUtil.getOrder(HibernateUtil.makeName(auc, AreaUser.FIELD__CREATED_DATETIME__NAME), sortAsc));
                    break;
                case LAST_LOGIN_DATETIME:
                    getAreaUserStatsCriteria().addOrder(HibernateUtil.getOrder(HibernateUtil.makeName(getAreaUserStatsCriteria(), AreaUserStats.FIELD__LAST_LOGIN_DATETIME__NAME), sortAsc));
                    break;
                case DISPLAY_NAME:
                    doDisplayNameOrder(auc);
                    break;
                case DISPLAY_NAME_LENGTH:
                    // jw: first, order by display name length (so that the closest matches are first
                    auc.addOrder(LengthOrder.order(HibernateUtil.makeName(auc, AreaUser.FIELD__DISPLAY_NAME__NAME), sortAsc));
                    // jw: then order the names with the same length alphabetically.
                    doDisplayNameOrder(auc);
                    break;
                case MENTIONS_ORDERING:
                    assert !isEmpty(mentionsFilter) : "Should always have a filter when we get here!";

                    // jw: first, since we could have matched on either username or display name, we need to order the results
                    //     first by those that matched on the username, and give those preferential treatment.
                    getUserCriteria().addOrder(new StartsWithOrder(HibernateUtil.makeName(getUserCriteria(), User.FIELD__USERNAME__NAME), mentionsFilter
                            // jw: we want to make sure that when the names are going from a->z, that the username matches
                            //     come first, followed by the displayname matches. Since if the username matches it will be
                            //     a 1 value, and if not a 0 value, we need to reverse this so that its appropriate.
                            , !sortAsc));

                    // jw: next, we need to filter to either the length of the most relevant field
                    getUserCriteria().addOrder(new MentionsLengthOrder(getUserCriteria(), mentionsFilter, sortAsc));

                    // jw: then order the names with the same length alphabetically.
                    getUserCriteria().addOrder(new MentionsFieldOrder(getUserCriteria(), mentionsFilter, sortAsc));
                    break;
                case MATCH_ACCURACY:
                    assert !isEmpty(generalNameLike) : "Should only ever use this sort with the generalNameLike";

                    // jw: this gets much more complicated if we are sorting by email and displayName
                    if (includeEmailAddressInGeneralNameSearch) {
                        Criteria uc = getUserCriteria();
                        // jw: give preference to matches for display name
                        auc.addOrder(StringPositionOrder.order(HibernateUtil.makeName(auc, AreaUser.FIELD__DISPLAY_NAME__NAME), generalNameLike, sortAsc));
                        auc.addOrder(LengthOrder.order(HibernateUtil.makeName(auc, AreaUser.FIELD__DISPLAY_NAME__NAME), generalNameLike, sortAsc));

                        // jw: then order any exacts that are left by email address.
                        getUserEmailAddressCriteria().addOrder(StringPositionOrder.order(HibernateUtil.makeName(getUserEmailAddressCriteria(), EmailAddress.Fields.emailAddress), generalNameLike, sortAsc));
                        getUserEmailAddressCriteria().addOrder(LengthOrder.order(HibernateUtil.makeName(getUserEmailAddressCriteria(), EmailAddress.Fields.emailAddress), generalNameLike, sortAsc));

                    } else {
                        auc.addOrder(StringPositionOrder.order(HibernateUtil.makeName(auc, AreaUser.FIELD__DISPLAY_NAME__NAME), generalNameLike, sortAsc));
                        auc.addOrder(LengthOrder.order(HibernateUtil.makeName(auc, AreaUser.FIELD__DISPLAY_NAME__NAME), sortAsc));
                    }

                    // jw: first, order by display name length (so that the closest matches are first
                    auc.addOrder(LengthOrder.order(HibernateUtil.makeName(auc, AreaUser.FIELD__DISPLAY_NAME__NAME), sortAsc));

                    // jw: After all of that, lets just end on sorting the displayName alphabetically if anything manages to get here and still be tied.
                    doDisplayNameOrder(auc);
                    break;
            }
        }

        auc.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        auc.setFetchMode(AreaUser.FIELD__AREA_USER_STATS__NAME, FetchMode.JOIN);

        assert rowsPerPage > 0 : "Should never use AreaUserList without first setting rowsPerPage!";

        //limits
        auc.setFirstResult((page - 1) * rowsPerPage);
        auc.setMaxResults(rowsPerPage);

        if (fetchUserOidsOnly) {
            auc.setProjection(Projections.property(HibernateUtil.makeName(getUserCriteria(), User.FIELD__OID__NAME)));

            userOids = auc.list();
            return null;
        }

        if (fetchOidsOnly) {
            auc.setProjection(Projections.id());

            areaUserOids = auc.list();
            return null;
        }

        return auc.list();
    }

    private void doDisplayNameOrder(Criteria auc) {
        String fieldName = HibernateUtil.makeName(auc, AreaUser.FIELD__DISPLAY_NAME__NAME);
        auc.addOrder(sortAsc ? Order.asc(fieldName) : Order.desc(fieldName));
    }

    public boolean isHasFilterSet() {
        return !isEmptyOrNull(areaCircles)
                // jw: we are purposefully excluding status from these checks because this method is used to determine
                //     if the admin has specified any criteria, and the status is only ever set by the code to restrict
                //     results to certain queues.
                || !isEmpty(displayNameLike) || !isEmpty(displayNameAfter) || !isEmpty(mentionsFilter) || !isEmpty(displayNameStartsWith) || !isEmpty(generalNameLike) || lastLoginAfter != null || lastLoginBefore != null || registeredAfter != null || registeredBefore != null;
    }

    public void setAreaCircle(AreaCircle areaCircle) {
        assert exists(areaCircle) : "Should never call this without specifying a areaCircle!";
        setAreaCircles(Collections.singleton(areaCircle));
    }

    public void setAreaCircles(Collection<AreaCircle> areaCircles) {
        assert !isEmptyOrNull(areaCircles) : "Should always provide at least one circle";

        this.areaCircles = areaCircles;
    }

    public Boolean getInAllAreaCircles() {
        return inAllAreaCircles;
    }

    public void setInAllAreaCircles(Boolean inAllAreaCircles) {
        this.inAllAreaCircles = inAllAreaCircles;
    }

    public String getDisplayNameLike() {
        return displayNameLike;
    }

    public void setDisplayNameLike(String displayNameLike) {
        this.displayNameLike = displayNameLike;
    }

    public String getDisplayNameAfter() {
        return displayNameAfter;
    }

    public void setDisplayNameAfter(String displayNameAfter) {
        this.displayNameAfter = displayNameAfter;
    }

    public void setMentionsFilter(String mentionsFilter) {
        this.mentionsFilter = mentionsFilter;
    }

    public void setDisplayNameStartsWith(String displayNameStartsWith) {
        this.displayNameStartsWith = displayNameStartsWith;
    }

    public void setGeneralNameLike(String generalNameLike) {
        this.generalNameLike = generalNameLike;
    }

    public void setIncludeEmailAddressInGeneralNameSearch(boolean includeEmailAddressInGeneralNameSearch) {
        this.includeEmailAddressInGeneralNameSearch = includeEmailAddressInGeneralNameSearch;
    }

    public Timestamp getLastLoginAfter() {
        return lastLoginAfter;
    }

    public void setLastLoginAfter(Timestamp lastLoginAfter) {
        this.lastLoginAfter = lastLoginAfter;
    }

    public Timestamp getLastLoginBefore() {
        return lastLoginBefore;
    }

    public void setLastLoginBefore(Timestamp lastLoginBefore) {
        this.lastLoginBefore = lastLoginBefore;
    }

    public Timestamp getRegisteredAfter() {
        return registeredAfter;
    }

    public void setRegisteredAfter(Timestamp registeredAfter) {
        this.registeredAfter = registeredAfter;
    }

    public Timestamp getRegisteredBefore() {
        return registeredBefore;
    }

    public void setRegisteredBefore(Timestamp registeredBefore) {
        this.registeredBefore = registeredBefore;
    }

    public SortField getSort() {
        return sort;
    }

    public void setSort(SortField sort) {
        this.sort = sort;
    }

    /**
     * The total number of rows for this criteria.  Only supplied if doCount=true.  Otherwise null
     *
     * @return
     */
    @Nullable
    public Integer getCount() {
        assert doCount : "Should only attempt to get the count if the count was requested!";
        return count;
    }

    public void doCount(boolean doCount) {
        this.doCount = doCount;
    }

    public void doFetchCountOnly() {
        doCount(true);
        fetchCountOnly = true;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public void doSetRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public boolean isSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(boolean sortAsc) {
        this.sortAsc = sortAsc;
    }

    public void doFetchOidsOnly() {
        fetchOidsOnly = true;
    }

    public List<OID> getAreaUserOids() {
        return areaUserOids;
    }

    public void doFetchUserOidsOnly() {
        fetchUserOidsOnly = true;
    }

    public List<OID> getUserOids() {
        return userOids;
    }

    public Area getArea() {
        return area;
    }

    protected boolean isForSystemProcessing() {
        return forSystemProcessing;
    }

    public void setForSystemProcessing() {
        forSystemProcessing = true;
    }

}


