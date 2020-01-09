package org.narrative.network.core.user.services.activityrate;

import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserActivityRateLimit;

import java.time.Instant;

/**
 * Date: 2019-06-14
 * Time: 12:34
 *
 * @author jonmark
 */
public class GetCommentUserActivityCountTask extends GetUserCompositionActivityRateLimitCountTaskBase {
    public GetCommentUserActivityCountTask(User user) {
        super(user);
    }

    @Override
    public UserActivityRateLimit getRateLimit() {
        return UserActivityRateLimit.COMMENT;
    }

    @Override
    protected long getUserCompositionActivityCount(User user, Instant after) {
        return Reply.dao().getCountCreatedByUserAfter(user, after);
    }
}
