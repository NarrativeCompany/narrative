package org.narrative.network.customizations.narrative.service.impl.contentstream;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.services.ContentList;
import org.narrative.network.core.content.base.services.ContentSort;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.security.DataVisibilityContextManager;
import org.narrative.network.customizations.narrative.service.api.model.filter.QualityFilter;
import lombok.Builder;

import java.sql.Timestamp;
import java.time.Instant;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Factory to build a {@link ContentList} filter from a content stream request.
 */
public class ContentStreamContentListFactory {
    private final Channel channel;
    private final Niche niche;
    private final User followingUser;
    private final Boolean hasAvatar;
    private final QualityFilter qualityFilter;
    private final ContentSort contentSort;
    private final int itemsPerPage;
    private final Instant postedBefore;
    private final Instant postedAfter;
    private final Instant trendingBuildTime;
    private final Long maxTrendingScore;
    private final Integer maxQualityScore;
    private final Integer maxQualityLikePoints;
    private final OID oidBefore;
    private final Boolean forCachedFeaturedContentList;

    @Builder(toBuilder = true)
    public ContentStreamContentListFactory(Channel channel, Niche niche, User followingUser, Boolean hasAvatar, QualityFilter qualityFilter, ContentSort contentSort, int itemsPerPage, Instant postedBefore, Instant postedAfter, Instant trendingBuildTime, Long maxTrendingScore, Integer maxQualityScore, Integer maxQualityLikePoints, OID oidBefore, Boolean forCachedFeaturedContentList) {
        this.channel = channel;
        this.niche = niche;
        this.followingUser = followingUser;
        this.hasAvatar = hasAvatar;
        this.qualityFilter = qualityFilter;
        this.contentSort = contentSort;
        this.itemsPerPage = itemsPerPage;
        this.postedBefore = postedBefore;
        this.postedAfter = postedAfter;
        this.trendingBuildTime = trendingBuildTime;
        this.maxTrendingScore = maxTrendingScore;
        this.maxQualityScore = maxQualityScore;
        this.maxQualityLikePoints = maxQualityLikePoints;
        this.oidBefore = oidBefore;
        this.forCachedFeaturedContentList = forCachedFeaturedContentList;
    }

    ContentList buildContentList() {
        if (channel != null && followingUser != null) {
            throw new IllegalArgumentException("You must either specify channel OIDs or followingUser but not both!");
        }
        if (contentSort==null) {
            throw new IllegalArgumentException("You must specify contentSort!");
        }

        ContentList res = new ContentList(Area.dao().getNarrativePlatformArea().getPortfolio(), ContentType.NARRATIVE_POST);

        // Filter as specified
        res.setChannel(channel);
        res.setNiche(niche);
        res.setFollowingUser(followingUser);
        res.setHasAvatar(hasAvatar);

        // bl: always include quality filters by default. if we are filtering to a specific channel, then
        // we should only include the quality filter if the channel type supports it (publications don't).
        boolean includeQualityFilter = !exists(channel) || channel.getType().isContentStreamSupportsQualityFilter();
        if(includeQualityFilter) {
            // If a quality filter is not specified, use the user's preference
            QualityFilter derivedQualityFilter = qualityFilter != null ? qualityFilter : DataVisibilityContextManager.getContentQualityFilter();
            // bl: for trending searches, we NEVER want to include low quality content
            if(contentSort.isTrendingScore() && (derivedQualityFilter==null || derivedQualityFilter.isAnyQuality())) {
                derivedQualityFilter = QualityFilter.HIDE_LOW_QUALITY;
            }
            res.setQualityFilter(derivedQualityFilter);
        }

        if(postedBefore!=null) {
            res.setPostedBefore(new Timestamp(postedBefore.toEpochMilli()));
        }
        if(postedAfter!=null) {
            res.setPostedAfter(new Timestamp(postedAfter.toEpochMilli()));
        }
        if(maxTrendingScore!=null) {
            assert trendingBuildTime!=null : "Should always supply trendingBuildTime when passing in maxTrendingScore!";
            res.setTrendingBuildTime(trendingBuildTime);
            res.setMaxTrendingScore(maxTrendingScore);
        }
        if(maxQualityScore!=null) {
            res.setMaxQualityScore(maxQualityScore);
        }
        if(maxQualityLikePoints!=null) {
            res.setMaxQualityLikePoints(maxQualityLikePoints);
        }
        if(oidBefore!=null) {
            res.setSecondarySortOidBefore(oidBefore);
        }
        if(forCachedFeaturedContentList!=null) {
            res.setForCachedFeaturedContentList(forCachedFeaturedContentList);
        }

        // Sort order
        res.setSort(contentSort);

        // Add paging info
        res.setPage(1);
        res.doSetRowsPerPage(itemsPerPage);
        // since the results are scrollable, we don't actually need the total count. saves a query.

        return res;
    }
}
