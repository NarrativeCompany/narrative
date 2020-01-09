package org.narrative.network.customizations.narrative.publications;

import org.narrative.common.util.enums.*;

import java.time.Duration;

/**
 * Date: 2019-08-09
 * Time: 13:52
 *
 * @author jonmark
 */
public enum PublicationExpiringReminderType implements IntegerEnum {
    ONE_MONTH_WARNING(0, Duration.ofDays(30))
    ,ONE_WEEK_WARNING(1, Duration.ofDays(7))
    ,EXPIRED(2, null)
    ;

    private final int id;
    private final Duration sendBeforeEndDatetime;

    PublicationExpiringReminderType(int id, Duration sendBeforeEndDatetime) {
        this.id = id;
        this.sendBeforeEndDatetime = sendBeforeEndDatetime;
    }

    @Override
    public int getId() {
        return id;
    }

    public Duration getSendBeforeEndDatetime() {
        return sendBeforeEndDatetime;
    }

    public boolean isExpired() {
        return this == EXPIRED;
    }
}