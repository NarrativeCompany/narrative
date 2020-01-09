package org.narrative.network.customizations.narrative.service.impl.contentstream;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.InvalidParamError;
import org.narrative.config.cache.spring.CacheManagerDefaultConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.services.ContentList;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.controller.ContentStreamController;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.api.ContentStreamService;
import org.narrative.network.customizations.narrative.service.api.model.ContentStreamEntriesDTO;
import org.narrative.network.customizations.narrative.service.api.model.ContentStreamScrollParamsDTO;
import org.narrative.network.customizations.narrative.service.api.model.PostDTO;
import org.narrative.network.customizations.narrative.service.api.model.filter.ContentStreamSortOrder;
import org.narrative.network.customizations.narrative.service.api.model.filter.ContentStreamType;
import org.narrative.network.customizations.narrative.service.api.model.filter.QualityFilter;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.mapper.PostMapper;
import org.narrative.network.customizations.narrative.util.ContentStreamScrollable;
import org.narrative.network.shared.security.PrimaryRole;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
public class ContentStreamServiceImpl implements ContentStreamService {

    private static final int FEATURED_CONTENT_STREAM_COUNT = 300;
    private static final int CHANNEL_CONTENT_WIDGET_COUNT = 50;

    private final StaticMethodWrapper staticMethodWrapper;
    private final PostMapper postMapper;
    private final NarrativeProperties narrativeProperties;

    public ContentStreamServiceImpl(StaticMethodWrapper staticMethodWrapper, PostMapper postMapper, NarrativeProperties narrativeProperties) {
        this.staticMethodWrapper = staticMethodWrapper;
        this.postMapper = postMapper;
        this.narrativeProperties = narrativeProperties;
    }

    private ContentStreamEntriesDTO findContentAndBuildStreamDTO(ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder, QualityFilter qualityFilterOverride, ContentStreamSortOrder sortOrder, ContentStreamScrollable scrollable) {
        if (sortOrder == null) {
            sortOrder = ContentStreamSortOrder.MOST_RECENT;
        }

        int itemsPerPage = scrollable.getResolvedCount(narrativeProperties);

        List<Content> contents = getContentForScrollableContentStream(builder, sortOrder, scrollable, qualityFilterOverride, itemsPerPage);

        return getContentStreamPosts(contents, sortOrder, itemsPerPage).build();
    }

    private List<Content> getContentForScrollableContentStream(ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder, ContentStreamSortOrder sortOrder, ContentStreamScrollable scrollable, QualityFilter qualityFilterOverride, int itemsPerPage) {
        assert sortOrder.isScrollable() : "Should only be used with scrollable sort orders! not/" + sortOrder;
        builder.qualityFilter(qualityFilterOverride)
                .itemsPerPage(itemsPerPage)
                .contentSort(sortOrder.getContentSort());

        // apply scrollable params to the builder based on the scrollable
        if(scrollable.getScrollParams()!=null) {
            sortOrder.applyScrollParamFilters(builder, scrollable.getScrollParams());
        }

        List<OID> contentOids = getContentOids(builder.build());

        return Content.dao().getObjectsFromIDsWithCache(contentOids);
    }

    private Collection<OID> getOidsForFeaturedContentStream(ContentStreamType contentStreamType, ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder, QualityFilter qualityFilterOverride) {
        // bl: for now, let's always get 300 items in Featured streams
        int itemsPerPage = FEATURED_CONTENT_STREAM_COUNT;

        builder.qualityFilter(qualityFilterOverride)
                .itemsPerPage(itemsPerPage);

        // bl: all featured posts must have an avatar
        builder.hasAvatar(true);

        // bl: mark that this is for a cached featured content list so that we don't include any user-specific posts.
        if(!contentStreamType.isPersonalized()) {
            builder.forCachedFeaturedContentList(true);
        }

        // bl: also ensure that we only include quality posts that were made within the past 2 days on the network-wide stream
        // bl: we've decided to apply this to both the quality and trending lists; limit to recent content from the past 2 days only.
        if(contentStreamType.isNetworkWide()) {
            builder.postedAfter(Instant.now().minus(2, ChronoUnit.DAYS));
        }

        // bl: the featured sort is a combination of items from the trending list and items in the highest rated list.
        // so first, let's get the list of trending items.
        ContentStreamContentListFactory trendingFactory = builder.contentSort(ContentStreamSortOrder.TRENDING.getContentSort()).build();

        List<OID> trendingContentOids = getContentOids(trendingFactory);

        // bl: now, create a new builder to use the quality sort order.
        ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder highestRatedFactoryBuilder = trendingFactory.toBuilder().contentSort(ContentStreamSortOrder.HIGHEST_RATED.getContentSort());
        // bl: for non-network-wide streams, only include quality posts that were made within the past week
        if(!contentStreamType.isNetworkWide()) {
            highestRatedFactoryBuilder.postedAfter(Instant.now().minus(7, ChronoUnit.DAYS));
        }
        ContentStreamContentListFactory highestRatedFactory = highestRatedFactoryBuilder.build();

        List<OID> highestRatedContentOids = getContentOids(highestRatedFactory);

        // now we have to merge the two content lists.
        return new ContentListMerger(trendingContentOids, highestRatedContentOids, itemsPerPage).getMergedList();
    }

    @AllArgsConstructor
    private static class ContentListMerger {
        private final List<OID> trendingContentOids;
        private final List<OID> qualityContentOids;
        private final int maxSize;

        private final Random random = new Random();
        private final Set<OID> contentOids = new LinkedHashSet<>();


        Collection<OID> getMergedList() {
            if(contentOids.isEmpty()) {
                merge();
            }
            return contentOids;
        }

        private void merge() {
            // if we get to a point where either list is empty, then no longer need to add in chunks
            while(!trendingContentOids.isEmpty() && !qualityContentOids.isEmpty()) {
                if (addChunk()) {
                    return;
                }
            }

            // bl: if we get to the point where either list is empty, check if we need to fill the rest of the return
            // list from the list that still has elements
            if (isFull()) {
                return;
            }

            // only one of the lists should have elements at this point.
            if (!trendingContentOids.isEmpty()) {
                fillFromList(trendingContentOids);
            } else if (!qualityContentOids.isEmpty()) {
                fillFromList(qualityContentOids);
            }
        }

        private boolean addChunk() {
            // bl: each iteration, randomly pick which of 4 slots the quality post goes in
            int slot = random.nextInt(4);
            for (int i = 0; i < 4; i++) {
                List<OID> list = slot == i ? qualityContentOids : trendingContentOids;
                if (addNextContent(list)) {
                    return true;
                }
            }
            return false;
        }

        private boolean addNextContent(List<OID> list) {
            // short circuit if the supplied list doesn't have any elements
            if (list.isEmpty()) {
                return false;
            }
            // the list has an element to add, so add it!
            OID contentOid = list.remove(0);
            // if this content has already been added, then don't add it again!
            if (contentOids.contains(contentOid)) {
                return false;
            }
            contentOids.add(contentOid);
            // return true once the ret list is full! then we're done
            return isFull();
        }

        private boolean isFull() {
            return contentOids.size() >= maxSize;
        }

        private void fillFromList(List<OID> contents) {
            while (!contents.isEmpty()) {
                if (addNextContent(contents)) {
                    break;
                }
            }
        }
    }

    private List<OID> getContentOids(ContentStreamContentListFactory factory) {
        ContentList contentList = factory.buildContentList();
        contentList.fetchContentOidsOnly();

        staticMethodWrapper.getAreaContext().doAreaTask(contentList);

        return contentList.getContentOids();
    }

    @Override
    public ContentStreamEntriesDTO findChannelContentStreamPosts(@NotNull ChannelType channelType, @NotNull OID channelOid, OID nicheOid, QualityFilter qualityFilterOverride, ContentStreamSortOrder sortOrder, ContentStreamScrollable scrollable) {
        assert sortOrder==null || !sortOrder.isFeatured() : "Should never use this method for FEATURED sort anymore since it doesn't have caching!";
        ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder = getChannelContentStreamBuilder(channelType, channelOid);

        // bl: set the Niche to filter by, which is optional
        Niche niche = Niche.dao().get(nicheOid);
        builder.niche(niche);

        return findContentAndBuildStreamDTO(builder, qualityFilterOverride, sortOrder, scrollable);
    }

    @Cacheable(cacheNames = CacheManagerDefaultConfig.CacheName.CACHE_CONTENTSTREAMSERVICE_FEATURED_CHANNEL_POSTS)
    @Override
    public Collection<OID> findFeaturedChannelContentStreamPostOids(@NotNull ChannelType channelType, @NotNull OID channelOid, QualityFilter qualityFilter, boolean isIncludeAgeRestrictedContent) {
        ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder = getChannelContentStreamBuilder(channelType, channelOid);

        return getOidsForFeaturedContentStream(ContentStreamType.CHANNEL, builder, qualityFilter);
    }

    private ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder getChannelContentStreamBuilder(@NotNull ChannelType channelType, @NotNull OID channelOid) {
        Channel channel = Channel.dao().get(channelOid);
        if (!staticMethodWrapper.exists(channel)) {
            throw new InvalidParamError(ContentStreamController.CHANNEL_OID_PARAM, channelOid.toString());
        }
        if (!channel.getType().equals(channelType)) {
            throw new InvalidParamError(ContentStreamController.CHANNEL_TYPE_PARAM, channelType.toString());
        }
        // jw: if this is a publication we need to ensure that the publication has not expired.
        if (channelType.isPublication()) {
            Publication publication = channel.getPublication();
            // jw: owners have no reason to access the content stream, it will not help them renew.
            publication.assertNotExpired(false);
        }

        return ContentStreamContentListFactory.builder().channel(channel);
    }

    @Cacheable(cacheNames = CacheManagerDefaultConfig.CacheName.CACHE_CONTENTSTREAMSERVICE_CHANNEL_WIDGET_POSTS)
    @Override
    public Collection<OID> findChannelContentWidgetPostOids(@NotNull ChannelType channelType, @NotNull OID channelOid, ContentStreamSortOrder sortOrder, QualityFilter qualityFilter, boolean isIncludeAgeRestrictedContent) {
        assert sortOrder==null || !sortOrder.isFeatured() : "Should never use this method for FEATURED sorts!";
        ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder = getChannelContentStreamBuilder(channelType, channelOid);

        ContentStreamContentListFactory factory = builder
                .qualityFilter(qualityFilter)
                // bl: for now, let's always get 50 items in channel widgets
                .itemsPerPage(CHANNEL_CONTENT_WIDGET_COUNT)
                .contentSort(sortOrder!=null ? sortOrder.getContentSort() : null)
                .build();

        return getContentOids(factory);
    }

    @Override
    public ContentStreamEntriesDTO findPersonalizedContentStreamPosts(QualityFilter qualityFilterOverride, ContentStreamSortOrder sortOrder, ContentStreamScrollable scrollable) {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder = ContentStreamContentListFactory.builder()
                .followingUser(primaryRole.getUser());

        return findContentAndBuildStreamDTO(builder, qualityFilterOverride, sortOrder, scrollable);
    }

    @Override
    public Collection<OID> findFeaturedPersonalizedContentStreamPosts(QualityFilter qualityFilterOverride) {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder = ContentStreamContentListFactory.builder()
                .followingUser(primaryRole.getUser());

        return getOidsForFeaturedContentStream(ContentStreamType.PERSONALIZED, builder, qualityFilterOverride);
    }

    @Override
    public ContentStreamEntriesDTO findNetworkWideContentStreamPosts(QualityFilter qualityFilterOverride, ContentStreamSortOrder sortOrder, ContentStreamScrollable scrollable) {
        assert sortOrder==null || !sortOrder.isFeatured() : "Should never use this method for FEATURED sort anymore since it doesn't have caching!";
        // bl: this is a network-wide stream, so no additional filters necessary. just create an empty builder.
        ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder = ContentStreamContentListFactory.builder();

        return findContentAndBuildStreamDTO(builder, qualityFilterOverride, sortOrder, scrollable);
    }

    @Cacheable(cacheNames = CacheManagerDefaultConfig.CacheName.CACHE_CONTENTSTREAMSERVICE_FEATURED_NETWORK_POSTS)
    @Override
    public Collection<OID> findFeaturedNetworkWideContentStreamPostOids(QualityFilter qualityFilter, boolean isIncludeAgeRestrictedContent) {
        return getOidsForFeaturedContentStream(ContentStreamType.NETWORK_WIDE, ContentStreamContentListFactory.builder(), qualityFilter);
    }

    @Override
    public ContentStreamEntriesDTO getContentStreamPostsFromOids(Collection<OID> contentOids, ContentStreamSortOrder sortOrder, int count, boolean forWidget) {
        // bl: split the OIDs into the current page and the remaining ("next") OIDs
        List<OID> currentPageOids = new ArrayList<>(count);
        List<OID> nextOids = new ArrayList<>(Math.max(0, contentOids.size()-count));
        for (OID contentOid : contentOids) {
            if(currentPageOids.size()<count) {
                currentPageOids.add(contentOid);
            } else {
                nextOids.add(contentOid);
            }
        }
        List<Content> contents = getContentFromOids(currentPageOids);
        ContentStreamEntriesDTO.ContentStreamEntriesDTOBuilder builder = getContentStreamPosts(contents, sortOrder, count);
        // add the next OIDs to scroll params so the front end knows what OIDs to fetch next. (unless this is for a widget)
        if(!forWidget && !nextOids.isEmpty()) {
            ContentStreamScrollParamsDTO scrollParams = ContentStreamScrollParamsDTO.builder()
                        .nextItemOids(nextOids)
                        .build();
            builder.scrollParams(scrollParams)
                    .hasMoreItems(true);
        }

        return builder.build();
    }

    @Override
    public ContentStreamEntriesDTO getSecuredContentStreamPostsFromFeaturedOids(List<OID> contentOids) {
        List<Content> contents = getContentFromOids(contentOids);
        // bl: go through and make sure the user has rights to view each post
        contents.removeIf(content -> !content.isDoesCurrentUserHaveViewRight());
        return getContentStreamPosts(contents, ContentStreamSortOrder.FEATURED, contents.size()).build();
    }

    private List<Content> getContentFromOids(Collection<OID> contentOids) {
        List<Content> contents = Content.dao().getObjectsFromIDsWithCache(contentOids);
        // bl: in case any of the posts have been deleted, filter them out here
        return contents.stream().filter(CoreUtils::exists).collect(Collectors.toList());
    }

    public void populateFollowedChannelsForContentList(User user, Collection<Content> contents) {
        Set<Channel> publishedToChannels = contents.stream().map(Content::getPublishedToChannels).flatMap(Collection::stream).collect(Collectors.toSet());

        FollowedChannel.dao().populateChannelsFollowedByCurrentUserField(user, publishedToChannels);
    }

    private ContentStreamEntriesDTO.ContentStreamEntriesDTOBuilder getContentStreamPosts(List<Content> contents, ContentStreamSortOrder sortOrder, int itemsPerPage) {
        User user = staticMethodWrapper.networkContext().getUser();
        populateFollowedChannelsForContentList(user, contents);

        List<PostDTO> postDTOs = postMapper.mapContentListToPostDTOList(contents);

        // bl: only set hasMoreItems to true if the sort actually supports scrolling
        boolean hasMoreItems = sortOrder.isScrollable() && postDTOs.size() == itemsPerPage;

        ContentStreamEntriesDTO.ContentStreamEntriesDTOBuilder builder = ContentStreamEntriesDTO.builder()
                .items(postDTOs)
                .hasMoreItems(hasMoreItems);

        // if we have more items, then include the scroll params based on that last item.
        if(hasMoreItems) {
            assert !postDTOs.isEmpty() : "How is it possible we have more items, but we have an empty list of posts? Is the count 0? Should always be enforced to be positive. itemsPerPage/" + itemsPerPage;
            Content lastPost = contents.get(contents.size()-1);
            builder.scrollParams(sortOrder.getScrollParamsForLastContent(lastPost));
        }

        return builder;
    }
}
