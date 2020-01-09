package org.narrative.network.core.content.base.services;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.criteria.CriteriaList;
import org.narrative.common.persistence.hibernate.criteria.CriteriaSort;
import org.narrative.common.util.CoreUtils;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentStats;
import org.narrative.network.core.content.base.ContentStatus;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.TrendingContent;
import org.narrative.network.core.moderation.ModerationStatus;
import org.narrative.network.core.rating.model.QualityRatingFields;
import org.narrative.network.core.settings.global.GlobalSettings;
import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import lombok.AllArgsConstructor;
import org.hibernate.Criteria;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/14/14
 * Time: 5:32 PM
 */
public abstract class ContentListBase<T extends DAOObject, S extends CriteriaSort> extends AreaTaskImpl<List<T>> implements CriteriaList<T, S> {
    public static final int DEFAULT_ROWS_PER_PAGE = 20;

    public ContentListBase(Portfolio portfolio, S defaultSort, ContentType contentType) {
        this(portfolio, defaultSort, Collections.singleton(contentType));
    }

    public ContentListBase(Portfolio portfolio, S defaultSort, Collection<ContentType> contentTypes) {
        this.portfolio = portfolio;
        this.sort = defaultSort;
        this.contentTypes = newHashSet(contentTypes);
    }

    private final Portfolio portfolio;

    private int page = 1;
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;

    private final Set<ContentType> contentTypes;
    private AreaUserRlm author;
    private Collection<OID> contentOidIn;

    private String subject;

    private S sort;
    private boolean sortAsc = false;

    private Boolean moderated = false;

    private Boolean hasLiveReplies;
    private Boolean hasAvatar;

    private Boolean allowReplies = null;
    private Boolean draft = false;

    private Timestamp postedAfter;
    private Timestamp postedBefore;
    private Timestamp postedBeforeNotEqual;

    private Timestamp lastUpdateAfter = null;
    private boolean lastUpdateAfterInclusive = false;
    private Timestamp lastUpdateBefore = null;

    private Instant trendingBuildTime;
    private Long maxTrendingScore;
    private Integer maxQualityScore;
    private Integer maxQualityLikePoints;

    private OID secondarySortOidBefore;

    protected Criteria contentCriteria;
    protected Criteria trendingContentCriteria;
    private Criteria contentStatsCriteria;

    private Criteria moderatedContentCriteria;

    private Criteria futureContentCriteria;

    private boolean isDoesQueryRequireDistinctContent = false;

    public boolean isDoesQueryRequireDistinctContent() {
        return isDoesQueryRequireDistinctContent;
    }

    protected void setRequireDistinctContent() {
        isDoesQueryRequireDistinctContent = true;
    }

    private boolean doCount = false;
    protected Integer count;
    protected Integer pageCount;

    public boolean isForceWritable() {
        return false;
    }

    protected Criteria getContentCriteria() {
        return contentCriteria;
    }

    protected Criteria getTrendingContentCriteria() {
        if (trendingContentCriteria == null) {
            // bl: join to TrendingContent using the filter based off of the current build time value
            Instant trendingBuildTime;
            // bl: if a trendingBuildTime was passed in, use it!
            if(this.trendingBuildTime!=null) {
                trendingBuildTime = this.trendingBuildTime;
            } else {
                // otherwise, default to the current global setting
                GlobalSettings globalSettings = GlobalSettingsUtil.getGlobalSettings();
                trendingBuildTime = globalSettings.getCurrentTrendingContentBuildTime();
            }
            trendingContentCriteria = getContentCriteria().createCriteria(Content.FIELD__TRENDING_CONTENTS__NAME,
                    "tc",
                    JoinType.INNER_JOIN,
                    Restrictions.eq(TrendingContent.FIELD__BUILD_TIME, trendingBuildTime)
            );
        }
        return trendingContentCriteria;
    }

    protected Criteria getContentStatsCriteria() {
        if (contentStatsCriteria == null) {
            contentStatsCriteria = getContentCriteria().createCriteria(Content.FIELD__CONTENT_STATS__NAME, "cs");
        }
        return contentStatsCriteria;
    }

    protected Criteria getModeratedContentCriteria() {
        if (moderatedContentCriteria == null) {
            moderatedContentCriteria = getContentCriteria().createCriteria(Content.FIELD__MODERATED_CONTENTS__NAME, "moderatedContents");
        }
        return moderatedContentCriteria;
    }

    protected Criteria getFutureContentCriteria() {
        if (futureContentCriteria == null) {
            futureContentCriteria = getContentCriteria().createCriteria("c." + Content.FIELD__FUTURE_CONTENT__NAME, "future_c");
        }
        return futureContentCriteria;
    }

    protected void resetCriteriaObjectsForReRun() {
        contentCriteria = null;
        contentStatsCriteria = null;
        futureContentCriteria = null;

        isDoesQueryRequireDistinctContent = false;
    }

    protected boolean isSingleContentType() {
        return contentTypes != null && contentTypes.size() == 1;
    }

    protected ContentType getSingleContentType() {
        return isSingleContentType() ? contentTypes.iterator().next() : null;
    }

    protected final List<T> doMonitoredTask() {
        if (contentCriteria != null) {
            resetCriteriaObjectsForReRun();
        }

        return runContentList();
    }

    protected abstract List<T> fetchCriteriaResults();

    protected List<T> runContentList() {
        Criteria cc = getContentCriteria();

        // bl: tweak to improve query performance.  when we include the AreaRlm parameter, MySQL thinks it should start
        // with the areaRlm_oid criteria for optimization, which makes the query use a temporary table using filesort.
        // when we are rendering purely a topic list, we don't need this anymore since we always limit the returned
        // ForumContent by Forum.  NOTE: ForumContent highlighted across all forums may have been an issue here
        // if we weren't limiting by forum, but when including ForumContent highlighted across all forums,
        // we DO include a list of available forums in the query, which means that we never need to rely on
        // the AreaRlm where clause.
        Map<ContentType, Collection<Portfolio>> portfoliosByContentType = newHashMap();

        // first we need to iterate over all relevant content types based on the passed in values or what the site
        // can possibly support.
        for (ContentType contentType : !isEmptyOrNull(contentTypes) ? contentTypes : EnumSet.allOf(ContentType.class)) {
            Collection<Portfolio> portfolios = Collections.singleton(getAreaContext().getAreaRlm().getDefaultPortfolio());

            if (!isEmptyOrNull(portfolios)) {
                portfoliosByContentType.put(contentType, portfolios);
            }
        }

        // If we did not find any enabled portfolios then there is nothing to do, lets return a empty set.
        if (isEmptyOrNull(portfoliosByContentType)) {
            return getEmptyResult();
        }

        // Guess we have values.  If we only identified one available content type lets go ahead and use a simpler
        // filter for that.
        final Criterion portfolioCriterion;
        if (portfoliosByContentType.size() == 1) {
            ContentType contentType = portfoliosByContentType.keySet().iterator().next();

            portfolioCriterion = getContentTypePortfoliosConjunction(contentType, portfoliosByContentType.get(contentType));

            // since we have multiple content types lets get results using a disjunction for each content type
        } else {
            Disjunction dis = Restrictions.disjunction();

            for (Map.Entry<ContentType, Collection<Portfolio>> contentTypePortfolios : portfoliosByContentType.entrySet()) {
                dis.add(getContentTypePortfoliosConjunction(contentTypePortfolios.getKey(), contentTypePortfolios.getValue()));
            }
            portfolioCriterion = dis;
        }
        cc.add(portfolioCriterion);

        //authors
        if (exists(author)) {
            getContentCriteria().add(Restrictions.eq(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__AREA_USER_RLM__NAME), author));
        }

        if (!CoreUtils.isEmptyOrNull(contentOidIn)) {
            getContentCriteria().add(Restrictions.in(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__OID__NAME), contentOidIn));
        }

        // bl: going to do filtering by subject in ContentList now, too.
        if (!isEmpty(subject)) {
            cc.add(Restrictions.ilike(HibernateUtil.makeName(cc, Content.FIELD__SUBJECT__NAME), PersistenceUtil.MySQLUtils.getStringAfterEscapingWildcardCharsForHqlLikePattern(subject), MatchMode.ANYWHERE));
        }

        Conjunction includes = Restrictions.conjunction();
        getContentCriteria().add(includes);

        // bl: don't include deleted content at all.  the only time we want to select deleted content
        // is when we're going to go through and do the actual deletions.
        includes.add(Restrictions.sqlRestriction("{alias}." + Content.FIELD__CONTENT_STATUS__NAME + " < " + ContentStatus.DISABLED.getBitmask()));

        if (allowReplies != null) {
            includes.add(Restrictions.eq("c." + Content.FIELD__ALLOW_REPLIES__NAME, allowReplies));
        }

        //create the draft, hiddenForModeration and future pub restrictions
        if (draft != null) {
            includes.add(Restrictions.sqlRestriction("{alias}." + Content.FIELD__CONTENT_STATUS__NAME + " & " + ContentStatus.DRAFT.getBitmask() + (draft ? "!=" : "=") + " 0"));
        }

        if (moderated != null) {
            if (moderated) {
                // bl: use moderatedContentCriteria to ensure that we join to ModeratedContent for additional performance
                getModeratedContentCriteria();
                // bl: no longer filtering moderated posts by Content.moderationStatus. we'll now purely use ChannelContent.status
                // to do moderated post filters.
            } else {
                getContentCriteria().add(Restrictions.ne("c." + Content.FIELD__MODERATION_STATUS__NAME, ModerationStatus.MODERATED));
            }
        }

        if (hasLiveReplies != null) {
            // bl: ContentStats.liveReplyCount is equal to (replyCount - moderatedReplyCount),
            if (hasLiveReplies) {
                // a topic has live replies if the replyCount is greater than the moderatedReplyCount
                getContentStatsCriteria().add(Restrictions.gtProperty(HibernateUtil.makeName(getContentStatsCriteria(), ContentStats.FIELD__REPLY_COUNT__NAME), HibernateUtil.makeName(getContentStatsCriteria(), ContentStats.FIELD__MODERATED_REPLY_COUNT__NAME)));
            } else {
                // a topic does not have live replies if the replyCount is less than or equal to the moderatedReplyCount
                getContentStatsCriteria().add(Restrictions.leProperty(HibernateUtil.makeName(getContentStatsCriteria(), ContentStats.FIELD__REPLY_COUNT__NAME), HibernateUtil.makeName(getContentStatsCriteria(), ContentStats.FIELD__MODERATED_REPLY_COUNT__NAME)));
            }
        }

        if (hasAvatar != null) {
            if (hasAvatar) {
                getContentCriteria().add(Restrictions.isNotNull(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__AVATAR_IMAGE_ON_DISK__NAME)));
            } else {
                getContentCriteria().add(Restrictions.isNull(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__AVATAR_IMAGE_ON_DISK__NAME)));
            }
        }

        if (postedAfter != null) {
            addPostedAfterCriteria(postedAfter);
        }

        if (postedBefore != null) {
            if (secondarySortOidBefore != null) {
                addLessThanFilterWithOidSort(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__LIVE_DATETIME__NAME), postedBefore);
            } else {
                addPostedBeforeCriteria(postedBefore);
            }
        }

        if (postedBeforeNotEqual != null) {
            addPostedBeforeNotEqualCriteria(postedBeforeNotEqual);
        }

        if (lastUpdateAfter != null) {
            addLastUpdateAfterCriteria(lastUpdateAfter, lastUpdateAfterInclusive);
        }

        if (lastUpdateBefore != null) {
            addLastUpdateBeforeCriteria(lastUpdateBefore);
        }

        if (maxTrendingScore != null) {
            addLessThanFilterWithOidSort(HibernateUtil.makeName(getTrendingContentCriteria(), TrendingContent.Fields.score), maxTrendingScore);
        }

        if (maxQualityScore != null && maxQualityLikePoints != null) {
            List<LessThanSortProps<?>> sortProps = new ArrayList<>(3);
            sortProps.add(new LessThanSortProps<>(
                    HibernateUtil.makeName(getContentCriteria(), Content.FIELD__QUALITY_RATING_FIELDS, QualityRatingFields.Fields.score),
                    maxQualityScore
            ));
            sortProps.add(new LessThanSortProps<>(
                    HibernateUtil.makeName(getContentCriteria(), Content.FIELD__QUALITY_RATING_FIELDS, QualityRatingFields.Fields.likePoints),
                    maxQualityLikePoints
            ));
            addSecondaryOidSort(sortProps);
            addLessThanCriteriaFilters(sortProps);
        }

        return fetchCriteriaResults();
    }

    protected List<T> getEmptyResult() {
        if (isDoCount()) {
            count = 0;
        }
        return Collections.emptyList();
    }

    private void addLessThanFilterWithOidSort(String propertyName, Object lessThanValue) {
        List<LessThanSortProps<?>> sortProps = new ArrayList<>(2);
        // always add the specified sort
        sortProps.add(new LessThanSortProps<>(propertyName, lessThanValue));
        // add the secondary OID sort if we have one
        addSecondaryOidSort(sortProps);
        // finally, add the criteria filters
        addLessThanCriteriaFilters(sortProps);
    }

    private void addSecondaryOidSort(List<LessThanSortProps<?>> sortProps) {
        // if we don't have an OID, then nothing to add
        if(secondarySortOidBefore==null) {
            return;
        }
        sortProps.add(new LessThanSortProps<>(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__OID__NAME), secondarySortOidBefore));
    }

    private void addLessThanCriteriaFilters(List<LessThanSortProps<?>> sortProps) {
        if(isEmptyOrNull(sortProps)) {
            return;
        }
        // bl: we need to start from the innermost/last filter and work our way back, so just reverse the list
        List<LessThanSortProps<?>> reversedProps = new ArrayList<>(sortProps);
        Collections.reverse(reversedProps);
        Criterion criterion = null;
        for (LessThanSortProps<?> props : reversedProps) {
            // bl: the first time around, we just add the base less than sort
            if(criterion==null) {
                criterion = props.getLessThanCriterion();
            } else {
                criterion = getLessThanFilterWithSecondarySort(props, criterion);
            }
        }
        getContentCriteria().add(criterion);
    }

    private Criterion getLessThanFilterWithSecondarySort(LessThanSortProps firstProps, Criterion innerCriterion) {
        LogicalExpression secondaryCriterion = Restrictions.and(
                firstProps.getEqualsCriterion(),
                innerCriterion
        );
        // content is before the lessThanValue object
        // OR
        // content is the same as lessThanValue and the OID is less than the previous OID
        return Restrictions.or(firstProps.getLessThanCriterion(), secondaryCriterion);
    }

    @AllArgsConstructor
    private static class LessThanSortProps<T> {
        private final String propertyName;
        private final T lessThanValue;

        Criterion getLessThanCriterion() {
            return Restrictions.lt(propertyName, lessThanValue);
        }

        Criterion getEqualsCriterion() {
            return Restrictions.eq(propertyName, lessThanValue);
        }
    }

    protected void addPostedBeforeCriteria(Timestamp postedBefore) {
        getContentCriteria().add(getPostedBeforeCriteria(postedBefore));
    }

    protected Criterion getPostedBeforeCriteria(Timestamp postedBefore) {
        return Restrictions.le(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__LIVE_DATETIME__NAME), postedBefore);
    }

    protected void addPostedBeforeNotEqualCriteria(Timestamp postedBeforeNotEqual) {
        getContentCriteria().add(getPostedBeforeNotEqualCriteria(postedBeforeNotEqual));
    }

    protected Criterion getPostedBeforeNotEqualCriteria(Timestamp postedBeforeNotEqual) {
        return Restrictions.lt(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__LIVE_DATETIME__NAME), postedBeforeNotEqual);
    }

    protected void addPostedAfterCriteria(Timestamp postedAfter) {
        getContentCriteria().add(Restrictions.ge(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__LIVE_DATETIME__NAME), postedAfter));
    }

    protected void addLastUpdateBeforeCriteria(Timestamp lastUpdateBefore) {
        // jw: we have to be certain to filter by the same field that we are sorting by.
        getContentCriteria().add(Restrictions.lt(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__LAST_UPDATE_DATETIME__NAME), lastUpdateBefore));
    }

    protected void addLastUpdateAfterCriteria(Timestamp lastUpdateAfter, boolean lastUpdateAfterInclusive) {
        if (lastUpdateAfterInclusive) {
            getContentCriteria().add(Restrictions.ge(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__LAST_UPDATE_DATETIME__NAME), lastUpdateAfter));

        } else {
            getContentCriteria().add(Restrictions.gt(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__LAST_UPDATE_DATETIME__NAME), lastUpdateAfter));
        }
    }

    private Conjunction getContentTypePortfoliosConjunction(ContentType contentType, Collection<Portfolio> portfolios) {
        Conjunction con = Restrictions.conjunction();

        con.add(Restrictions.eq(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__CONTENT_TYPE__NAME), contentType));
        con.add(Restrictions.in(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__PORTFOLIO__NAME), portfolios));

        return con;
    }

    @Nullable
    public S getSort() {
        return sort;
    }

    public void setSort(S sort) {
        this.sort = sort;
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

    @Nullable
    public Boolean getModerated() {
        return moderated;
    }

    public void setModerated(Boolean moderated) {
        this.moderated = moderated;
    }

    @Nullable
    public Boolean getAllowReplies() {
        return allowReplies;
    }

    public void setAllowReplies(Boolean allowReplies) {
        this.allowReplies = allowReplies;
    }

    @Nullable
    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    @Nullable
    public Boolean getHasLiveReplies() {
        return hasLiveReplies;
    }

    public void setHasLiveReplies(Boolean hasLiveReplies) {
        this.hasLiveReplies = hasLiveReplies;
    }

    public void setHasAvatar(Boolean hasAvatar) {
        this.hasAvatar = hasAvatar;
    }

    public Set<ContentType> getContentTypes() {
        return contentTypes;
    }

    @Nullable
    public ContentType getContentType() {
        return contentTypes != null && contentTypes.size() == 1 ? contentTypes.iterator().next() : null;
    }

    @Nullable
    public AreaUserRlm getAuthor() {
        return author;
    }

    public void setAuthor(AreaUserRlm author) {
        this.author = author;
    }

    public Collection<OID> getContentOidIn() {
        return contentOidIn;
    }

    public void setContentOidIn(Collection<OID> contentOidIn) {
        this.contentOidIn = contentOidIn;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

    /**
     * The total number of pages for this criteria.  Only supplied if doCount=true.  Otherwise null
     *
     * @return
     */
    @Nullable
    public Integer getPageCount() {
        return pageCount;
    }

    public void doCount(boolean doCount) {
        this.doCount = doCount;
    }

    public boolean isDoCount() {
        return this.doCount;
    }

    public boolean isSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(boolean sortAsc) {
        this.sortAsc = sortAsc;
    }

    public Timestamp getPostedAfter() {
        return postedAfter;
    }

    public void setPostedAfter(Timestamp postedAfter) {
        this.postedAfter = postedAfter;
    }

    public Timestamp getPostedBefore() {
        return postedBefore;
    }

    public void setPostedBefore(Timestamp postedBefore) {
        this.postedBefore = postedBefore;
    }

    public void setPostedBeforeNotEqual(Timestamp postedBeforeNotEqual) {
        this.postedBeforeNotEqual = postedBeforeNotEqual;
    }

    public Timestamp getLastUpdateAfter() {
        return lastUpdateAfter;
    }

    public void setLastUpdateAfter(Timestamp lastUpdateAfter) {
        this.lastUpdateAfter = lastUpdateAfter;
    }

    public void setLastUpdateAfterInclusive(boolean lastUpdateAfterInclusive) {
        this.lastUpdateAfterInclusive = lastUpdateAfterInclusive;
    }

    public Timestamp getLastUpdateBefore() {
        return lastUpdateBefore;
    }

    public void setLastUpdateBefore(Timestamp lastUpdateBefore) {
        this.lastUpdateBefore = lastUpdateBefore;
    }

    public Portfolio getPortfolio() {
        return this.portfolio;
    }

    public void setTrendingBuildTime(Instant trendingBuildTime) {
        this.trendingBuildTime = trendingBuildTime;
    }

    public void setMaxTrendingScore(Long maxTrendingScore) {
        this.maxTrendingScore = maxTrendingScore;
    }

    public void setMaxQualityScore(Integer maxQualityScore) {
        this.maxQualityScore = maxQualityScore;
    }

    public void setMaxQualityLikePoints(Integer maxQualityLikePoints) {
        this.maxQualityLikePoints = maxQualityLikePoints;
    }

    public void setSecondarySortOidBefore(OID secondarySortOidBefore) {
        this.secondarySortOidBefore = secondarySortOidBefore;
    }
}
