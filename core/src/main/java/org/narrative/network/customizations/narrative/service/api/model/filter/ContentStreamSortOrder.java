package org.narrative.network.customizations.narrative.service.api.model.filter;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.TrendingContent;
import org.narrative.network.core.content.base.services.ContentSort;
import org.narrative.network.customizations.narrative.service.api.model.ContentStreamScrollParamsDTO;
import org.narrative.network.customizations.narrative.service.impl.contentstream.ContentStreamContentListFactory;

/**
 * Enum representing generalized sort order options.
 */
public enum ContentStreamSortOrder implements IntegerEnum {
    MOST_RECENT(0, ContentSort.LIVE_DATETIME) {
        @Override
        public ContentStreamScrollParamsDTO getScrollParamsForLastContent(Content content) {
            return ContentStreamScrollParamsDTO.builder()
                    .lastItemDatetime(content.getLiveDatetime().toInstant())
                    .lastItemOid(content.getOid())
                    .build();
        }

        @Override
        public void applyScrollParamFilters(ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder, ContentStreamScrollParamsDTO scrollParams) {
            // if there isn't a lastItemDatetime, then nothing to apply
            if(scrollParams.getLastItemDatetime()==null) {
                return;
            }
            builder.postedBefore(scrollParams.getLastItemDatetime())
                    .oidBefore(scrollParams.getLastItemOid());
        }
    },
    TRENDING(1, ContentSort.TRENDING_SCORE) {
        @Override
        public ContentStreamScrollParamsDTO getScrollParamsForLastContent(Content content) {
            // bl: we should always have a TrendingContent if content is returned in the Trending list!
            // this will result in a one-off lookup of TrendingContent for the last content
            TrendingContent trendingContent = content.getCurrentTrendingContent();
            return ContentStreamScrollParamsDTO.builder()
                    .trendingBuildTime(trendingContent.getBuildTime())
                    .lastItemTrendingScore(trendingContent.getScore())
                    .lastItemOid(content.getOid())
                    .build();
        }

        @Override
        public void applyScrollParamFilters(ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder, ContentStreamScrollParamsDTO scrollParams) {
            // if there isn't a lastItemTrendingScore, then nothing to apply
            if(scrollParams.getLastItemTrendingScore()==null) {
                return;
            }
            builder.trendingBuildTime(scrollParams.getTrendingBuildTime())
                    .maxTrendingScore(scrollParams.getLastItemTrendingScore())
                    .oidBefore(scrollParams.getLastItemOid());
        }
    },
    FEATURED(2, null) {
        @Override
        public ContentStreamScrollParamsDTO getScrollParamsForLastContent(Content content) {
            throw UnexpectedError.getRuntimeException("Featured doesn't support scrolling, so this should never be called!");
        }

        @Override
        public void applyScrollParamFilters(ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder, ContentStreamScrollParamsDTO scrollParams) {
            throw UnexpectedError.getRuntimeException("Featured doesn't support scrolling, so this should never be called!");
        }
    },
    HIGHEST_RATED(3, ContentSort.QUALITY_SCORE) {
        @Override
        public ContentStreamScrollParamsDTO getScrollParamsForLastContent(Content content) {
            return ContentStreamScrollParamsDTO.builder()
                    .lastItemQualityScore(content.getQualityRatingFields().getScore())
                    .lastItemSecondaryQualityValue(content.getQualityRatingFields().getLikePoints())
                    .lastItemOid(content.getOid())
                    .build();
        }

        @Override
        public void applyScrollParamFilters(ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder, ContentStreamScrollParamsDTO scrollParams) {
            // if there isn't a lastItemQualityScore, then nothing to apply
            if(scrollParams.getLastItemQualityScore()==null) {
                return;
            }
            builder.maxQualityScore(scrollParams.getLastItemQualityScore())
                    // bl: obfuscating the like points value into a generic lastItemSecondaryQualityValue as a request
                    // param and DTO value so that it's not so obvious what it is for
                    .maxQualityLikePoints(scrollParams.getLastItemSecondaryQualityValue())
                    .oidBefore(scrollParams.getLastItemOid());
        }
    };

    private final int id;
    private final ContentSort contentSort;

    ContentStreamSortOrder(int id, ContentSort contentSort) {
        this.id = id;
        this.contentSort = contentSort;
    }

    @Override
    public int getId() {
        return id;
    }

    public ContentSort getContentSort() {
        return contentSort;
    }

    public boolean isTrending() {
        return this==TRENDING;
    }

    public boolean isFeatured() {
        return this==FEATURED;
    }

    public boolean isScrollable() {
        // bl: anything without a corresponding contentSort is not considered scrollable (e.g. featured)
        return contentSort!=null;
    }

    public abstract ContentStreamScrollParamsDTO getScrollParamsForLastContent(Content content);

    public abstract void applyScrollParamFilters(ContentStreamContentListFactory.ContentStreamContentListFactoryBuilder builder, ContentStreamScrollParamsDTO scrollParams);
}
