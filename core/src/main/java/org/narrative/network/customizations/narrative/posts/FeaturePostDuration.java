package org.narrative.network.customizations.narrative.posts;

import org.narrative.common.util.enums.*;

import java.time.Duration;

/**
 * Date: 2019-07-29
 * Time: 13:47
 *
 * @author jonmark
 */
public enum FeaturePostDuration implements IntegerEnum {
    ONE_DAY(0, Duration.ofDays(1))
    ,THREE_DAYS(1, Duration.ofDays(3))
    ,ONE_WEEK(2, Duration.ofDays(7))
    ;

    private final int id;
    private final Duration duration;

    FeaturePostDuration(int id, Duration duration) {
        this.id = id;
        this.duration = duration;
    }

    @Override
    public int getId() {
        return id;
    }

    public Duration getDuration() {
        return duration;
    }
}