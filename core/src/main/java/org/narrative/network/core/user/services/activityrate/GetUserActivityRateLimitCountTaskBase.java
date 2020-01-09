package org.narrative.network.core.user.services.activityrate;

import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserActivityRateLimit;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-12
 * Time: 17:57
 *
 * @author jonmark
 */
public abstract class GetUserActivityRateLimitCountTaskBase extends AreaTaskImpl<Long> {
    private final User user;

    protected GetUserActivityRateLimitCountTaskBase(User user) {
        assert exists(user) : "This task should always be provided with a user to check their activity rate for!";

        this.user = user;
    }

    public abstract UserActivityRateLimit getRateLimit();

    // jw: let's force implementation through a more explicit function. This allows us to force a int result, and we can
    //     provide the user more naturally than using protected access on the property.
    protected abstract long getUserActivityRateCount(User user, Instant after);

    @Override
    protected Long doMonitoredTask() {
        Instant after = ZonedDateTime.now(user.getFormatPreferences().getZoneId()).truncatedTo(getRateLimit().getLimitPeriod()).toInstant();

        return getUserActivityRateCount(user, after);
    }
}
