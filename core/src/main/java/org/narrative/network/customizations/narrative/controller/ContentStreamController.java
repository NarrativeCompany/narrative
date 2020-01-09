package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.security.DataVisibilityContextManager;
import org.narrative.network.customizations.narrative.service.api.ContentStreamService;
import org.narrative.network.customizations.narrative.service.api.model.ContentStreamEntriesDTO;
import org.narrative.network.customizations.narrative.service.api.model.ContentStreamScrollParamsDTO;
import org.narrative.network.customizations.narrative.service.api.model.filter.ContentStreamSortOrder;
import org.narrative.network.customizations.narrative.service.api.model.filter.QualityFilter;
import org.narrative.network.customizations.narrative.util.ContentStreamScrollable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.Collection;

import static org.narrative.common.util.CoreUtils.*;

@RestController
@Validated
@RequestMapping("/content-streams")
public class ContentStreamController {
    public static final String CHANNEL_TYPE_PARAM = "channelType";
    public static final String CHANNEL_OID_PARAM = "channelOid";
    private static final String QUALITY_FILTER = "qualityFilter";
    private static final String SORT_ORDER = "sortOrder";
    private static final String FOR_WIDGET = "forWidget";

    private final ContentStreamService contentStreamService;
    private final NarrativeProperties narrativeProperties;

    public ContentStreamController(ContentStreamService contentStreamService, NarrativeProperties narrativeProperties) {
        this.contentStreamService = contentStreamService;
        this.narrativeProperties = narrativeProperties;
    }

    /**
     * Individual content stream access
     */
    @GetMapping("/{" + CHANNEL_TYPE_PARAM + "}/{" + CHANNEL_OID_PARAM + "}")
    public ContentStreamEntriesDTO findChannelContentStreamPosts(@PathVariable(CHANNEL_TYPE_PARAM) @NotNull ChannelType channelType,
                                                                 @PathVariable(CHANNEL_OID_PARAM) @NotNull OID channelOid,
                                                                 @RequestParam(name = "nicheOid", required = false) OID nicheOid,
                                                                 @RequestParam(name = QUALITY_FILTER, required = false) QualityFilter qualityFilterOverride,
                                                                 @RequestParam(name = SORT_ORDER, required = false) ContentStreamSortOrder sortOrder,
                                                                 @RequestParam(name = FOR_WIDGET, required = false) boolean forWidget,
                                                                 @Valid ContentStreamScrollable scrollable,
                                                                 ContentStreamScrollParamsDTO scrollParams) {
        // bl: publications don't support sort orders
        if(!channelType.isContentStreamSupportsSortOrder()) {
            // bl: but we will support trending for publications for the widget
            if(sortOrder!=null && sortOrder.isTrending() && forWidget) {
                // bl: have to explicitly get the resolved QualityFilter since we use method caching
                QualityFilter qualityFilter = qualityFilterOverride != null ? qualityFilterOverride : DataVisibilityContextManager.getContentQualityFilter();
                int count = scrollable.getResolvedCount(narrativeProperties);
                Collection<OID> postOids = contentStreamService.findChannelContentWidgetPostOids(channelType, channelOid, sortOrder, qualityFilter, !DataVisibilityContextManager.isRestrictedContentFiltered());
                return contentStreamService.getContentStreamPostsFromOids(postOids, ContentStreamSortOrder.TRENDING, count, true);
            } else {
                // bl: otherwise, set to null here so that the default will be used at the service layer, which is most recent
                sortOrder = null;
            }
        }
        if(sortOrder!=null && sortOrder.isFeatured()) {
            // bl: if requesting specific OIDs, then just return those directly!
            if(scrollParams!=null && !isEmptyOrNull(scrollParams.getNextItemOids())) {
                return contentStreamService.getSecuredContentStreamPostsFromFeaturedOids(scrollParams.getNextItemOids());
            }

            // bl: have to explicitly get the resolved QualityFilter since we use method caching
            QualityFilter qualityFilter = qualityFilterOverride != null ? qualityFilterOverride : DataVisibilityContextManager.getContentQualityFilter();
            int count = scrollable.getResolvedCount(narrativeProperties);
            // bl: always request 300 posts so that we have a single cache of the list rather than a separate cache
            // for all of the different sized lists we might have.
            Collection<OID> postOids = contentStreamService.findFeaturedChannelContentStreamPostOids(channelType, channelOid, qualityFilter, !DataVisibilityContextManager.isRestrictedContentFiltered());
            return contentStreamService.getContentStreamPostsFromOids(postOids, ContentStreamSortOrder.FEATURED, count, forWidget);
        }
        if(scrollParams!=null) {
            scrollable.setScrollParams(scrollParams);
        }
        return contentStreamService.findChannelContentStreamPosts(channelType, channelOid, nicheOid, qualityFilterOverride, sortOrder, scrollable);
    }

    /**
     * Personalized content stream
     */
    @GetMapping("/current-user")
    public ContentStreamEntriesDTO findPersonalizedContentStreamPosts(@RequestParam(name = QUALITY_FILTER, required = false) QualityFilter qualityFilterOverride,
                                                                      @RequestParam(name = SORT_ORDER, required = false) ContentStreamSortOrder sortOrder,
                                                                      @RequestParam(name = FOR_WIDGET, required = false) boolean forWidget,
                                                                      @Valid ContentStreamScrollable scrollable,
                                                                      ContentStreamScrollParamsDTO scrollParams) {
        if(sortOrder!=null && sortOrder.isFeatured()) {
            // bl: if requesting specific OIDs, then just return those directly!
            if(scrollParams!=null && !isEmptyOrNull(scrollParams.getNextItemOids())) {
                return contentStreamService.getSecuredContentStreamPostsFromFeaturedOids(scrollParams.getNextItemOids());
            }

            // bl: have to explicitly get the resolved QualityFilter since we use method caching
            QualityFilter qualityFilter = qualityFilterOverride != null ? qualityFilterOverride : DataVisibilityContextManager.getContentQualityFilter();
            int count = scrollable.getResolvedCount(narrativeProperties);
            Collection<OID> postOids = contentStreamService.findFeaturedPersonalizedContentStreamPosts(qualityFilter);
            return contentStreamService.getContentStreamPostsFromOids(postOids, ContentStreamSortOrder.FEATURED, count, forWidget);
        }
        if(scrollParams!=null) {
            scrollable.setScrollParams(scrollParams);
        }
        return contentStreamService.findPersonalizedContentStreamPosts(qualityFilterOverride, sortOrder, scrollable);
    }

    /**
     * Trending content
     */
    @GetMapping("/network-wide")
    public ContentStreamEntriesDTO findNetworkWideContentStreamPosts(@RequestParam(name = QUALITY_FILTER, required = false) QualityFilter qualityFilterOverride,
                                                                     @RequestParam(name = SORT_ORDER, required = false) ContentStreamSortOrder sortOrder,
                                                                     @RequestParam(name = FOR_WIDGET, required = false) boolean forWidget,
                                                                     @Valid ContentStreamScrollable scrollable,
                                                                     ContentStreamScrollParamsDTO scrollParams) {
        if(sortOrder!=null && sortOrder.isFeatured()) {
            // bl: if requesting specific OIDs, then just return those directly!
            if(scrollParams!=null && !isEmptyOrNull(scrollParams.getNextItemOids())) {
                return contentStreamService.getSecuredContentStreamPostsFromFeaturedOids(scrollParams.getNextItemOids());
            }

            // bl: have to explicitly get the resolved QualityFilter since we use method caching
            QualityFilter qualityFilter = qualityFilterOverride != null ? qualityFilterOverride : DataVisibilityContextManager.getContentQualityFilter();
            int count = scrollable.getResolvedCount(narrativeProperties);
            Collection<OID> postOids = contentStreamService.findFeaturedNetworkWideContentStreamPostOids(qualityFilter, !DataVisibilityContextManager.isRestrictedContentFiltered());
            return contentStreamService.getContentStreamPostsFromOids(postOids, ContentStreamSortOrder.FEATURED, count, forWidget);
        }

        if(scrollParams!=null) {
            scrollable.setScrollParams(scrollParams);
        }
        return contentStreamService.findNetworkWideContentStreamPosts(qualityFilterOverride, sortOrder, scrollable);
    }

    /**
     * Converter to convert a channel type ID to a {@link ChannelType}.
     */
    @Component
    public static class StringToChannelTypeConverter implements Converter<String, ChannelType> {
        @Override
        public ChannelType convert(@Nonnull String resourcePath) {
            return ChannelType.getChannelTypeForRestResourcePath(resourcePath);
        }
    }
}
