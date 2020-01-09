package org.narrative.network.core.user.services.activityrate;

import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserActivityRateLimit;

import java.time.Instant;

/**
 * Date: 2019-06-14
 * Time: 10:40
 *
 * @author jonmark
 */
public class GetPostUserActivityCountTask extends GetUserActivityRateLimitCountTaskBase {
    public GetPostUserActivityCountTask(User user) {
        super(user);
    }

    @Override
    public UserActivityRateLimit getRateLimit() {
        return UserActivityRateLimit.POST;
    }

    @Override
    protected long getUserActivityRateCount(User user, Instant after) {
        return Content.dao().getCountCreatedByUserAfter(user, ContentType.NARRATIVE_POST, after);
    }
}
