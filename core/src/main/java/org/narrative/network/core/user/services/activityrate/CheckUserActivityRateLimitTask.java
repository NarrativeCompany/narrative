package org.narrative.network.core.user.services.activityrate;

import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserActivityRateLimit;
import org.narrative.network.customizations.narrative.reputation.ReputationLevel;
import org.narrative.network.shared.security.ActivityRateLimitAccessViolation;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-12
 * Time: 19:17
 *
 * @author jonmark
 */
public class CheckUserActivityRateLimitTask extends AreaTaskImpl<Object> {
    private final User user;
    private final UserActivityRateLimit limit;

    public CheckUserActivityRateLimitTask(User user, UserActivityRateLimit limit) {
        assert exists(user) : "We should always be given a user to check the activity rate limit for.";
        assert limit != null : "We should always be given a limit to check the user against.";

        this.user = user;
        this.limit = limit;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: if the activity is disabled entirely, then just throw the exception immediately; no further checks required.
        if(limit.isDisableActivity()) {
            throw new ActivityRateLimitAccessViolation(limit);
        }

        // jw: if the limit has a bypass for specific reputation level let's honor that before we do anything else.
        ReputationLevel reputationLevel = limit.getBypassAtReputationLevel();
        // jw: The limit must specify a bypass, so if set let's see if the users score meets the minimum
        if (reputationLevel != null && user.getReputation().getTotalScore() >= reputationLevel.getMinimumScore()) {
            return null;
        }

        // jw: first things first, let's check their activity for this limit
        long activity = getAreaContext().doAreaTask(limit.getActivityRateLimitCountTask(user));

        // jw: if they've had more activity than the limit allows we need to throw an exception
        if (activity >= limit.getLimit()) {
            throw new ActivityRateLimitAccessViolation(limit);
        }

        return null;
    }
}
