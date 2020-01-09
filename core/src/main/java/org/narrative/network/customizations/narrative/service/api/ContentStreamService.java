package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.service.api.model.ContentStreamEntriesDTO;
import org.narrative.network.customizations.narrative.service.api.model.PostDTO;
import org.narrative.network.customizations.narrative.service.api.model.filter.ContentStreamSortOrder;
import org.narrative.network.customizations.narrative.service.api.model.filter.QualityFilter;
import org.narrative.network.customizations.narrative.util.ContentStreamScrollable;

import javax.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Content stream related services.
 */
public interface ContentStreamService {
    /**
     * Find channel content stream posts for the specified channels.
     *
     * @param channelType           {@link ChannelType} of the channel of interest
     * @param channelOid            {@link OID} of the channel of interest
     * @param nicheOid              {@link OID} of a Niche to further filter by. optional.
     * @param qualityFilterOverride Optional quality filter override - if not included will default to the user preference quality filter
     * @param sortOrder             Optional sort order for the results - if not specified will default to {@link ContentStreamSortOrder#MOST_RECENT}
     * @param scrollable    Infinite scroll information for this request
     * @return {@link ContentStreamEntriesDTO} of {@link PostDTO} found
     */
    ContentStreamEntriesDTO findChannelContentStreamPosts(@NotNull ChannelType channelType, @NotNull OID channelOid, OID nicheOid, QualityFilter qualityFilterOverride, ContentStreamSortOrder sortOrder, ContentStreamScrollable scrollable);

    /**
     * Find featured content stream post OIDs from a channel. Always returns up to 300 results.
     * @param channelType the channel type
     * @param channelOid the channel OID
     * @param qualityFilter QualityFilter to filter on. Required since this method is cached.
     * @param isIncludeAgeRestrictedContent boolean flag purely for caching purposes so there are separate lists including vs. excluding restricted content.
     * @return {@link List} of post OIDs.
     */
    Collection<OID> findFeaturedChannelContentStreamPostOids(@NotNull ChannelType channelType, @NotNull OID channelOid, QualityFilter qualityFilter, boolean isIncludeAgeRestrictedContent);

    /**
     * Find content stream post OIDs from a channel for a widget. Always returns up to 50 results.
     * @param channelType the channel type
     * @param channelOid the channel OID
     * @param sortOrder the sort order to use for the posts
     * @param qualityFilter QualityFilter to filter on. Required since this method is cached.
     * @param isIncludeAgeRestrictedContent boolean flag purely for caching purposes so there are separate lists including vs. excluding restricted content.
     * @return {@link List} of post OIDs.
     */
    Collection<OID> findChannelContentWidgetPostOids(@NotNull ChannelType channelType, @NotNull OID channelOid, ContentStreamSortOrder sortOrder, QualityFilter qualityFilter, boolean isIncludeAgeRestrictedContent);

    /**
     * Find content stream posts for the specified user's content stream.  This stream is a chronological listing of
     * content from all of the niches, publication channels, and members that the viewing user is following.
     *
     * @param qualityFilterOverride Optional quality filter override - if not included will default to the user preference quality filter
     * @param sortOrder             Optional sort order for the results - if not specified will default to {@link ContentStreamSortOrder#MOST_RECENT}
     * @param scrollable    Infinite scroll information for this request
     * @return {@link ContentStreamEntriesDTO} of {@link PostDTO} found
     */
    ContentStreamEntriesDTO findPersonalizedContentStreamPosts(QualityFilter qualityFilterOverride, ContentStreamSortOrder sortOrder, ContentStreamScrollable scrollable);

    /**
     * Find featured content stream post OIDs for a user's personalized content stream.
     * @param qualityFilterOverride QualityFilter override
     * @return {@link List} of post OIDs.
     */
    Collection<OID> findFeaturedPersonalizedContentStreamPosts(QualityFilter qualityFilterOverride);

    /**
     * Find trending content stream posts.  This stream is an ordered listing of content from all of the niches sorted
     * by the specified sort order.
     *
     * @param qualityFilterOverride Optional quality filter override - if not included will default to the user preference quality filter
     * @param sortOrder             Optional sort order for the results - if not specified will default to {@link ContentStreamSortOrder#MOST_RECENT}
     * @param scrollable    Infinite scroll information for this request
     * @return {@link ContentStreamEntriesDTO} of {@link PostDTO} found
     */
    ContentStreamEntriesDTO findNetworkWideContentStreamPosts(QualityFilter qualityFilterOverride, ContentStreamSortOrder sortOrder, ContentStreamScrollable scrollable);

    /**
     * Find featured content stream post OIDs across the network.
     * @param qualityFilter QualityFilter to filter on. Required since this method is cached.
     * @param isIncludeAgeRestrictedContent boolean flag purely for caching purposes so there are separate lists including vs. excluding restricted content.
     * @return {@link List} of post OIDs.
     */
    Collection<OID> findFeaturedNetworkWideContentStreamPostOids(QualityFilter qualityFilter, boolean isIncludeAgeRestrictedContent);

    /**
     * Get {@link ContentStreamEntriesDTO} from a list of content OIDs.
     * @param contentOids the content OIDs
     * @param sortOrder the sort order used for the posts
     * @param count the number of results to return
     * @param forWidget true if this request is for a widget. if so, we won't include the next OIDs since there is no load more for widgets.
     * @return {@link ContentStreamEntriesDTO} with the results
     */
    ContentStreamEntriesDTO getContentStreamPostsFromOids(Collection<OID> contentOids, ContentStreamSortOrder sortOrder, int count, boolean forWidget);

    /**
     * Get {@link ContentStreamEntriesDTO} from a list of content OIDs.
     * @param contentOids the content OIDs
     * @return {@link ContentStreamEntriesDTO} with the results
     */
    ContentStreamEntriesDTO getSecuredContentStreamPostsFromFeaturedOids(List<OID> contentOids);

    /**
     * Populate the followed Niches data on a {@link List} of {@link Content}
     * @param user the current user
     * @param contents the list of {@link Content} objects to populate
     */
    void populateFollowedChannelsForContentList(User user, Collection<Content> contents);
}
