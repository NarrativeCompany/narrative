package org.narrative.network.core.user.services.activityrate;

import org.narrative.network.core.rating.model.UserQualityRatedReply;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserActivityRateLimit;

import java.time.Instant;

/**
 * Date: 2019-06-12
 * Time: 19:09
 *
 * @author jonmark
 */
public class GetRateReplyUserActivityCountTask extends GetUserCompositionActivityRateLimitCountTaskBase {
    public GetRateReplyUserActivityCountTask(User user) {
        super(user);
    }

    @Override
    public UserActivityRateLimit getRateLimit() {
        return UserActivityRateLimit.RATE_COMMENT;
    }

    @Override
    protected long getUserCompositionActivityCount(User user, Instant after) {
        return UserQualityRatedReply.dao().getCountForUserAfter(user, after);
    }
}
