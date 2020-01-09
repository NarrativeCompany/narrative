package org.narrative.network.core.content.base.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.persistence.hibernate.FeaturedPostOrder;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.criteria.CriteriaListFieldOrderBy;
import org.narrative.common.persistence.hibernate.criteria.CriteriaListOrderBy;
import org.narrative.common.persistence.hibernate.criteria.FirstResultTransformer;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.network.core.area.base.ItemHourTrendingStats;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.area.user.WatchedUser;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentStats;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.FutureContent;
import org.narrative.network.core.content.base.TrendingContent;
import org.narrative.network.core.rating.model.QualityRatingFields;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.posts.ChannelContent;
import org.narrative.network.customizations.narrative.posts.NarrativePostStatus;
import org.narrative.network.customizations.narrative.security.DataVisibilityContextManager;
import org.narrative.network.customizations.narrative.service.api.model.filter.QualityFilter;
import org.narrative.network.shared.likes.LikeFields;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Feb 14, 2006
 * Time: 3:39:25 PM
 */
public class ContentList extends ContentListBase<Content, ContentSort> {
    private boolean doFetchContent = true;

    private boolean fetchContentOidsOnly = false;
    private List<OID> contentOids;

    private Channel channel;
    private Niche niche;
    private User followingUser;
    private QualityFilter qualityFilter;

    private boolean forCachedFeaturedContentList;

    public ContentList(Portfolio portfolio, ContentType contentType) {
        super(portfolio, ContentSort.LIVE_DATETIME, contentType);
    }

    public ContentList(Portfolio portfolio, Collection<ContentType> contentTypes) {
        super(portfolio, ContentSort.LIVE_DATETIME, contentTypes);
    }

    private Criteria channelContentCriteria;
    private Criteria nicheChannelContentCriteria;
    private Criteria channelCriteria;
    private Criteria followedChannelCriteria;
    private Criteria areaUserRlmCriteria;
    private Criteria areaUserCriteria;
    private Criteria userCriteria;
    private Criteria watchedUserCriteria;
    private Criteria itemHourTrendingStatsCriteria;

    @Override
    protected void resetCriteriaObjectsForReRun() {
        super.resetCriteriaObjectsForReRun();

        itemHourTrendingStatsCriteria = null;
    }

    @Override
    protected Criteria getContentCriteria() {
        if (contentCriteria == null) {
            if (getSort().isTrending()) {
                getItemHourTrendingStatsCriteria();
                return contentCriteria;
            }
            contentCriteria = Content.dao().getGSession().getSession().createCriteria(Content.class, "c");
        }
        return contentCriteria;
    }

    private Criteria getChannelContentCriteria(boolean innerJoin) {
        if(channelContentCriteria==null) {
            channelContentCriteria = getContentCriteria().createCriteria(
                    HibernateUtil.makeName(getContentCriteria(), Content.FIELD__CHANNEL_CONTENTS),
                    "chcc",
                    innerJoin ? JoinType.INNER_JOIN : JoinType.LEFT_OUTER_JOIN,
                    // bl: if we're getting a list of moderated posts, then filter to only moderated posts
                    Restrictions.eq(ChannelContent.Fields.status, getModerated()!=null && getModerated() ? NarrativePostStatus.MODERATED : NarrativePostStatus.APPROVED)
            );
        }
        return channelContentCriteria;
    }

    private Criteria getNicheChannelContentCriteria() {
        // bl: we have a secondary nicheChannelContentCriteria so that we can filter first by the primary channel/Publication,
        // and do further filtering by Niche (within the Publication)
        if(nicheChannelContentCriteria==null) {
            nicheChannelContentCriteria = getContentCriteria().createCriteria(
                    HibernateUtil.makeName(getContentCriteria(), Content.FIELD__NICHE_CHANNEL_CONTENTS),
                    "nchcc",
                    JoinType.INNER_JOIN,
                    Restrictions.eq(ChannelContent.Fields.status, NarrativePostStatus.APPROVED)
            );
        }
        return nicheChannelContentCriteria;
    }

    private Criteria getChannelCriteria() {
        if(channelCriteria==null) {
            Criteria channelContentCriteria = getChannelContentCriteria(false);
            channelCriteria = channelContentCriteria.createCriteria(
                    HibernateUtil.makeName(channelContentCriteria, ChannelContent.FIELD__CHANNEL),
                    "ch",
                    JoinType.LEFT_OUTER_JOIN
            );
        }
        return channelCriteria;
    }

    private Criteria getFollowedChannelCriteria() {
        if(followedChannelCriteria==null) {
            assert followingUser!=null : "Should only get followedChannelCriteria when followingUser is specified!";
            Criteria channelCriteria = getChannelCriteria();
            followedChannelCriteria = channelCriteria.createCriteria(
                    HibernateUtil.makeName(channelCriteria, Channel.Fields.followers),
                    "fch",
                    JoinType.LEFT_OUTER_JOIN,
                    Restrictions.eq(FollowedChannel.FIELD__FOLLOWER__NAME, followingUser)
            );
        }
        return followedChannelCriteria;
    }

    private Criteria getAreaUserRlmCriteria() {
        if(areaUserRlmCriteria==null) {
            areaUserRlmCriteria = getContentCriteria().createCriteria(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__AREA_USER_RLM__NAME), "aur", JoinType.INNER_JOIN);
        }
        return areaUserRlmCriteria;
    }

    private Criteria getAreaUserCriteria() {
        if(areaUserCriteria==null) {
            areaUserCriteria = getAreaUserRlmCriteria().createCriteria(HibernateUtil.makeName(getAreaUserRlmCriteria(), AreaUserRlm.FIELD__AREA_USER__NAME), "au", JoinType.INNER_JOIN);
        }
        return areaUserCriteria;
    }

    private Criteria getUserCriteria() {
        if(userCriteria==null) {
            userCriteria = getAreaUserCriteria().createCriteria(HibernateUtil.makeName(getAreaUserCriteria(), AreaUser.FIELD__USER__NAME), "u", JoinType.INNER_JOIN);
        }
        return userCriteria;
    }

    private Criteria getWatchedUserCriteria() {
        if(watchedUserCriteria==null) {
            assert followingUser!=null : "Should only get watchedUserCriteria when followingUser is specified!";
            watchedUserCriteria = getUserCriteria().createCriteria(
                    HibernateUtil.makeName(getUserCriteria(), User.FIELD__USERS_WATCHING__NAME),
                    "wu",
                    JoinType.LEFT_OUTER_JOIN,
                    Restrictions.eq(WatchedUser.FIELD__WATCHER_USER__NAME, followingUser)
            );
        }
        return watchedUserCriteria;
    }

    protected Criteria getItemHourTrendingStatsCriteria() {
        if (itemHourTrendingStatsCriteria == null) {
            itemHourTrendingStatsCriteria = ItemHourTrendingStats.dao().getCriteria();
            assert contentCriteria == null : "Should never use ItemHourTrendingStats Criteria after the Content Criteria has already been created!";
            contentCriteria = itemHourTrendingStatsCriteria.createCriteria(HibernateUtil.makeName(itemHourTrendingStatsCriteria, ItemHourTrendingStats.Fields.content), "c");
        }
        return itemHourTrendingStatsCriteria;
    }

    @Override
    protected List<Content> fetchCriteriaResults() {
        ContentSort sort = getSort();

        if (sort.isTrending()) {
            assert sort.getTrendingOverXDays() <= ItemHourTrendingStats.CUTOFF_IN_DAYS : "Should never attempt to find Content trending over a larger period of time than we keep!";

            long hoursSinceEpoch = ItemHourTrendingStats.getHoursSinceTheEpoch() - (sort.getTrendingOverXDays() * 24);

            getItemHourTrendingStatsCriteria().add(Restrictions.ge(HibernateUtil.makeName(getItemHourTrendingStatsCriteria(), ItemHourTrendingStats.Fields.hoursSinceEpoch), hoursSinceEpoch));
        }

        //do we need a count
        // bl: do the count before we add the order by so that the count doesn't also have to do an order query.
        if (isDoCount()) {
            Criteria criteria = sort.isTrending() ? getItemHourTrendingStatsCriteria() : getContentCriteria();

            criteria.setFirstResult(0);
            criteria.setMaxResults(1);
            // bl: for multi-forum queries, we need to get a count of distinct OIDs, not just a straight row count,
            // since there may be duplicate rows returned for a single topic (e.g. when a topic has shortcuts in multiple forums).
            if (isDoesQueryRequireDistinctContent() || sort.isTrending()) {
                getContentCriteria().setProjection(Projections.countDistinct(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__OID__NAME)));
            } else {
                getContentCriteria().setProjection(Projections.rowCount());
            }
            count = ((Number) criteria.uniqueResult()).intValue();
            pageCount = (int) Math.ceil(count.doubleValue() / getRowsPerPage());
            getContentCriteria().setProjection(null);
        }

        // jw: if we are not fetching results let go ahead and just return a empty collection.
        if (!isFetchContent() && !fetchContentOidsOnly) {
            assert isDoCount() : "The only time it makes sense to skip fetching results is if all we care about is calculating counts.";

            return Collections.emptyList();
        }

        //order by
        List<OID> contentOids;
        if (sort.isTrending()) {
            assert !isSortAsc() : "Should never be sorting ascending when using a Trending sort!";
            Criteria statsCriteria = getItemHourTrendingStatsCriteria();
            ItemHourTrendingStats.dao().addCriteriaProjection(getContentCriteria(), Content.FIELD__OID__NAME, statsCriteria);
            getContentCriteria().addOrder(HibernateUtil.getOrder(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__LIVE_DATETIME__NAME), isSortAsc()));
            getContentCriteria().addOrder(HibernateUtil.getOrder(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__OID__NAME), isSortAsc()));

            statsCriteria.setFirstResult((getPage() - 1) * getRowsPerPage());
            statsCriteria.setMaxResults(getRowsPerPage());

            // jw: Because we are using the trending ranking projection from ItemHourTrendingStatsDAO.addCriteriaProjection
            //     the ranking would otherwise be the second parameter here.
            // bl: using FirstResultTransformer here now so that we only get the first item in the result (the OID)
            getContentCriteria().setResultTransformer(FirstResultTransformer.INSTANCE);

            contentOids = statsCriteria.list();
        } else {
            List<CriteriaListOrderBy> orders = new LinkedList<>();
            if (sort.isLastUpdateDatetime()) {
                addLastUpdateSortOrder(orders);
            } else if (sort.isLiveDatetime()) {
                // bl: when filtering to a channel that supports featured posts, add that as the first sort
                if(isSpecificChannelQuery() && channel.getType().isSupportsFeaturedPosts()) {
                    Criteria criteria = getChannelContentCriteria(true);
                    // bl: first sort by whether the post is still featured or not (featuredUntilDatetime is in the future)
                    orders.add(() -> {
                        // bl: if the featuredUntilDatetime is in the future, sort by featuredDatetime
                        criteria.addOrder(FeaturedPostOrder.sort(HibernateUtil.makeName(criteria, ChannelContent.Fields.featuredUntilDatetime), HibernateUtil.makeName(criteria, ChannelContent.Fields.featuredDatetime), isSortAsc()));
                    });
                }
                orders.add(new CriteriaListFieldOrderBy(getContentCriteria(), isSortAsc(), Content.FIELD__LIVE_DATETIME__NAME));

            } else if (sort.isChannelModerationDatetime()) {
                assert isSpecificChannelQuery() : "Should only sort by channel moderation datetime for channel-specific queries!";
                orders.add(new CriteriaListFieldOrderBy(getChannelContentCriteria(true), isSortAsc(), ChannelContent.Fields.moderationDatetime));

            } else if (sort.isSaveDatetime()) {
                orders.add(new CriteriaListFieldOrderBy(getFutureContentCriteria(), isSortAsc(), FutureContent.FIELD__SAVE_DATETIME__NAME));

            } else if (sort.isComments()) {
                orders.add(new CriteriaListFieldOrderBy(getContentStatsCriteria(), isSortAsc(), ContentStats.FIELD__REPLY_COUNT__NAME));

                // jw: for popularity lets add a last update sort order after the reply order so that there is reliability
                //     in order.
                addLastUpdateSortOrder(orders);

            } else if (sort.isPopularity()) {
                orders.add(new CriteriaListFieldOrderBy(getContentStatsCriteria(), isSortAsc(), ContentStats.FIELD__AREA_LIKE_FIELDS__NAME, LikeFields.FIELD__LIKE_COUNT__NAME));

                // jw: for popularity lets add a last update sort order after the like order so that there is reliability
                //     in order.
                addLastUpdateSortOrder(orders);
            } else if (sort.isAlphabetical()) {
                orders.add(new CriteriaListFieldOrderBy(getContentCriteria(), isSortAsc(), Content.FIELD__SUBJECT__NAME));
            } else if (sort.isQualityScore()) {
                orders.add(new CriteriaListFieldOrderBy(getContentCriteria(), false, Content.FIELD__QUALITY_RATING_FIELDS, QualityRatingFields.Fields.score));
                orders.add(new CriteriaListFieldOrderBy(getContentCriteria(), false, Content.FIELD__QUALITY_RATING_FIELDS, QualityRatingFields.Fields.likePoints));
            } else if (sort.isTrendingScore()) {
                orders.add(new CriteriaListFieldOrderBy(getTrendingContentCriteria(), false, TrendingContent.Fields.score));
            }

            // bl: always sort by OID descending as the final sort order
            orders.add(new CriteriaListFieldOrderBy(getContentCriteria(), false, Content.FIELD__OID__NAME));

            //limits
            getContentCriteria().setFirstResult((getPage() - 1) * getRowsPerPage());
            // bl: no limit if the limit is Integer.MAX_VALUE
            if (getRowsPerPage() < Integer.MAX_VALUE) {
                getContentCriteria().setMaxResults(getRowsPerPage());
            }

            // bl: if we are selecting topics across multiple forums here, there still is potential for
            // the topic list pagination to be out of whack (e.g. if you view the entire list of all topics
            // across the site or you view a list of topics within a category -- and that list contains topics
            // that have shortcuts).
            // to address those problems, we need to select just the distinct Content OIDs and then do another query
            // to load Content objects from those OIDs.
            if (isDoesQueryRequireDistinctContent()) {
                ProjectionList projectionList = Projections.projectionList();
                Set<Criteria> criteriasInGroupBy = new HashSet<>();
                projectionList.add(Projections.groupProperty(HibernateUtil.makeName(getContentCriteria(), Content.FIELD__OID__NAME)));
                criteriasInGroupBy.add(getContentCriteria());
                // bl: in order to work around MySQL 5.7 issues, we need to make sure that we include any other tables from the "order by"
                // clause also in the "group by" clause to avoid errors like:
                // Expression #1 of ORDER BY clause is not in GROUP BY clause and contains nonaggregated column 'realm1.fc2_.liveDatetime' which is not functionally dependent on columns in GROUP BY clause; this is incompatible with sql_mode=only_full_group_by
                for (CriteriaListOrderBy order : orders) {
                    order.addToCriteria(criteriasInGroupBy, projectionList);
                }
                getContentCriteria().setProjection(projectionList);
                // bl: there could potentially be multiple items selected in the result, but we really only want the first result (the Content OID)
                getContentCriteria().setResultTransformer(FirstResultTransformer.INSTANCE);
            } else {
                // bl: add the basic order by clause. no special ProjectionList required since we are selecting the unique ID
                for (CriteriaListOrderBy order : orders) {
                    order.addBasicOrder();
                }
                // bl: changing so that we always just select the content OIDs. this seems to dramatically
                // improve performance on some of the more complex queries (particularly forum topic queries).
                // this means that we will do two queries: one to get the OIDs, then another query to
                // get content objects. with caching of Content objects, this shouldn't have a terrible impact
                // on performance (and on sites like HarperCollins, it should actually improve performance quite a bit.
                getContentCriteria().setProjection(Projections.id());
            }

            contentOids = getContentCriteria().list();
        }

        if (fetchContentOidsOnly) {
            this.contentOids = contentOids;

            return Collections.emptyList();
        }

        List<Content> list = Content.dao().getObjectsFromIDsWithCache(contentOids);

        return list;
    }

    @Override
    protected List<Content> runContentList() {
        Criteria cc = getContentCriteria();

        validateChannelFilterCriteria();

        // Handle age rating filtering of all content
        if (DataVisibilityContextManager.isRestrictedContentFiltered()) {
            Criterion ageRatingCriterion = Restrictions.in(
                    HibernateUtil.makeName(contentCriteria, Content.FIELD__AGE_RATING),
                    DataVisibilityContextManager.getPermittedAgeRatingSet()
            );
            // bl: if the user is registered, always allow them to see their own posts, even if those that are restricted.
            // bl: if we're generating results for a cached featured content list, then do not allow the author to view
            // their own posts so that we don't get results specific to an individual user cached.
            if(getAreaContext().getAreaRole().isActiveRegisteredAreaUser() && !forCachedFeaturedContentList) {
                contentCriteria.add(Restrictions.or(
                        ageRatingCriterion,
                        Restrictions.eq(HibernateUtil.makeName(contentCriteria, Content.FIELD__AREA_USER_RLM__NAME), getAreaContext().getAreaUserRlm())
                ));
            } else {
                contentCriteria.add(ageRatingCriterion);
            }
        }

        // Handle quality filtering of all content - only filter if necessary
        if (qualityFilter != null && !qualityFilter.isAnyQuality()) {
            addQualityFilterCriteria();
        }

        // Handle channel specific queries
        if (isChannelQuery()) {
            addChannelCriteria();
        }

        if(exists(niche)) {
            getContentCriteria().add(Restrictions.eq(HibernateUtil.makeName(getNicheChannelContentCriteria(), ChannelContent.FIELD__CHANNEL), niche.getChannel()));
        }

        return super.runContentList();
    }

    private void addLastUpdateSortOrder(List<CriteriaListOrderBy> orders) {
        orders.add(new CriteriaListFieldOrderBy(getContentCriteria(), isSortAsc(), Content.FIELD__LAST_UPDATE_DATETIME__NAME));
    }

    protected boolean isFetchContent() {
        return doFetchContent;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setNiche(Niche niche) {
        this.niche = niche;
    }

    public void setFollowingUser(User followingUser) {
        this.followingUser = followingUser;
    }

    public void setQualityFilter(QualityFilter qualityFilter) {
        this.qualityFilter = qualityFilter;
    }

    public void setForCachedFeaturedContentList(boolean forCachedFeaturedContentList) {
        this.forCachedFeaturedContentList = forCachedFeaturedContentList;
    }

    public void fetchContentOidsOnly() {
        fetchContentOidsOnly = true;
        doFetchContent = false;
    }

    public List<OID> getContentOids() {
        assert fetchContentOidsOnly : "Should only ever attempt to access contentOids when the list is flagged to fetch them only.";
        assert contentOids != null : "contentOids should never be null unless you have not ran this list yet!";

        return contentOids;
    }

    @Override
    protected List<Content> getEmptyResult() {
        if (fetchContentOidsOnly) {
            contentOids = Collections.emptyList();
        }

        return super.getEmptyResult();
    }

    private void addQualityFilterCriteria() {
        contentCriteria.add(Restrictions.ge(
                HibernateUtil.makeName(getContentCriteria(), Content.FIELD__QUALITY_RATING_FIELDS, QualityRatingFields.Fields.score),
                qualityFilter.getMinimumQualityLevel().getMinimumScore())
        );
    }

    private void validateChannelFilterCriteria() {
        if (isUserFollowsQuery() && isSpecificChannelQuery()) {
            throw new IllegalArgumentException("You must either specify channel OIDs or followingUser but not both!");
        }
    }

    private boolean isUserFollowsQuery() {
        return followingUser != null;
    }

    private boolean isSpecificChannelQuery() {
        return exists(channel);
    }

    private boolean isChannelQuery() {
        return isUserFollowsQuery() || isSpecificChannelQuery();
    }

    private void addChannelCriteria() {
        if (isSpecificChannelQuery()) {
            getContentCriteria().add(Restrictions.eq(HibernateUtil.makeName(getChannelContentCriteria(true), ChannelContent.FIELD__CHANNEL), channel));
        } else if (isUserFollowsQuery()) {
            addUserFollowsCriteria();
        }
    }

    /**
     * Unfortunately Hibernate Criteria does not support inline views so we need to use EXISTS subqueries/outer joins
     * to dig out followed content and niches.  Consensus opinion seems to be that  EXISTS is generally more
     * performant for MySQL even for small subquery row counts with a large outer query rowset even though
     * it uses a correlated subquery.
     */
    private void addUserFollowsCriteria() {
        // For the personalized content stream list, we just need to filter to results where the user is either
        // following one of the channels that the content is posted to OR is following the author
        // bl: this is TOTALLY AWESOME. Hibernate has a bug where it doesn't apply the with/in clause parameters
        // in the right order. they had a bug open for it: https://hibernate.atlassian.net/browse/HHH-5645
        // but it has since been closed. the issue still exists. the solution is to add the with/in clauses
        // in the correct order that Hibernate will add the parameters in. so, this means we have to first add
        // the WatchedUser criteria, followed by the ChannelContent.status criteria, and then the FollowedChannel
        // criteria. the order of fetching/initializing the Criteria objects here fixes it.
        Criteria watchedUserCriteria = getWatchedUserCriteria();
        Criteria followedChannelCriteria = getFollowedChannelCriteria();
        getContentCriteria().add(Restrictions.or(
                Restrictions.isNotNull(HibernateUtil.makeName(followedChannelCriteria, FollowedChannel.FIELD__OID__NAME)),
                // bl: exclude blocked/blacklisted users
                Restrictions.and(
                        Restrictions.isNotNull(HibernateUtil.makeName(watchedUserCriteria, WatchedUser.FIELD__OID__NAME)),
                        Restrictions.eq(HibernateUtil.makeName(watchedUserCriteria, WatchedUser.FIELD__BLOCKED__NAME), false)
                )
            )
        );

        // The outer joins OR'ed together may produce overlapping results so force DISTINCT
        setRequireDistinctContent();
    }
}
