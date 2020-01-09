package org.narrative.network.core.content.base.services;

import org.narrative.common.persistence.hibernate.criteria.CriteriaSort;
import org.narrative.common.util.enums.StringEnum;

/**
 * Created by Eclipse.
 * User: benjamin
 * Date: Feb 11, 2010
 * Time: 3:56:06 PM
 */
public enum ContentSort implements CriteriaSort, StringEnum {
    LAST_UPDATE_DATETIME("last-update"),
    SAVE_DATETIME("save-datetime"),
    LIVE_DATETIME("live-datetime"),
    COMMENTS("comments"),
    ALPHABETICAL("alphabetical"),
    POPULARITY("popularity"),
    TRENDING_OVER_LAST_DAY("trending-last-day", 1L),
    TRENDING_OVER_LAST_THREE_DAYS("trending-last-three-days", 3L),
    TRENDING_OVER_LAST_WEEK("trending-last-week", 7L),
    TRENDING_OVER_LAST_MONTH("trending-last-month", 30L),
    QUALITY_SCORE("quality-score"),
    TRENDING_SCORE("trending-score"),
    CHANNEL_MODERATION_DATETIME("channel-moderation-datetime"),
    ;

    private final String idStr;
    private final Long trendingOverXDays;

    ContentSort(String idStr) {
        this(idStr, null);
    }

    ContentSort(String idStr, Long trendingOverXDays) {
        this.idStr = idStr;
        this.trendingOverXDays = trendingOverXDays;
    }

    @Override
    public String getIdStr() {
        return idStr;
    }

    public boolean isLastUpdateDatetime() {
        return this == LAST_UPDATE_DATETIME;
    }

    public boolean isLiveDatetime() {
        return this == LIVE_DATETIME;
    }

    public boolean isSaveDatetime() {
        return this == SAVE_DATETIME;
    }

    public boolean isAlphabetical() {
        return this == ALPHABETICAL;
    }

    public boolean isComments() {
        return this == COMMENTS;
    }

    public boolean isPopularity() {
        return this == POPULARITY;
    }

    public boolean isTrending() {
        return trendingOverXDays != null;
    }

    public boolean isQualityScore() {
        return this == QUALITY_SCORE;
    }

    public boolean isTrendingScore() {
        return this == TRENDING_SCORE;
    }

    public boolean isChannelModerationDatetime() {
        return this == CHANNEL_MODERATION_DATETIME;
    }

    public Long getTrendingOverXDays() {
        return trendingOverXDays;
    }

}
