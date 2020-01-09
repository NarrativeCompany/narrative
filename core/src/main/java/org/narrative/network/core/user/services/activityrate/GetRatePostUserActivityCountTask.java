package org.narrative.network.core.user.services.activityrate;

import org.narrative.network.core.rating.model.UserAgeRatedComposition;
import org.narrative.network.core.rating.model.UserQualityRatedComposition;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserActivityRateLimit;

import java.time.Instant;

/**
 * Date: 2019-06-12
 * Time: 18:02
 *
 * @author jonmark
 */
public class GetRatePostUserActivityCountTask extends GetUserCompositionActivityRateLimitCountTaskBase {
    public GetRatePostUserActivityCountTask(User user) {
        super(user);
    }

    @Override
    public UserActivityRateLimit getRateLimit() {
        return UserActivityRateLimit.RATE_POST;
    }

    @Override
    protected long getUserCompositionActivityCount(User user, Instant after) {
        long ratings = UserQualityRatedComposition.dao().getCountForUserAfter(user, after);
        ratings += UserAgeRatedComposition.dao().getCountForUserAfter(user, after);

        return ratings;
    }
}
