package org.narrative.network.customizations.narrative.service.api.model.filter;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.customizations.narrative.posts.QualityLevel;

/**
 * Enum representing quality filter options.
 */
public enum QualityFilter implements IntegerEnum {
    ONLY_HIGH_QUALITY(0, QualityLevel.HIGH),
    HIDE_LOW_QUALITY(1, QualityLevel.MEDIUM),
    ANY_QUALITY(2, QualityLevel.LOW)
    ;

    public static final QualityFilter DEFAULT_FILTER = HIDE_LOW_QUALITY;

    private final int id;
    private final QualityLevel minimumQualityLevel;

    QualityFilter(int id, QualityLevel minimumQualityLevel) {
        this.id = id;
        this.minimumQualityLevel = minimumQualityLevel;
    }

    @Override
    public int getId() {
        return id;
    }

    public QualityLevel getMinimumQualityLevel() {
        return minimumQualityLevel;
    }

    public boolean isAnyQuality() {
        return ANY_QUALITY==this;
    }
}
