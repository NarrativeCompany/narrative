package org.narrative.network.core.user;

import org.narrative.common.util.enums.*;
import org.narrative.network.core.user.services.activityrate.GetCommentUserActivityCountTask;
import org.narrative.network.core.user.services.activityrate.GetPostUserActivityCountTask;
import org.narrative.network.core.user.services.activityrate.GetUserActivityRateLimitCountTaskBase;
import org.narrative.network.core.user.services.activityrate.GetRatePostUserActivityCountTask;
import org.narrative.network.core.user.services.activityrate.GetRateReplyUserActivityCountTask;
import org.narrative.network.customizations.narrative.reputation.ReputationLevel;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-06-12
 * Time: 17:55
 *
 * @author jonmark
 */
public enum UserActivityRateLimit implements IntegerEnum {
    RATE_POST(0, 150, ChronoUnit.DAYS, null) {
        @Override
        protected GetUserActivityRateLimitCountTaskBase getActivityRateLimitCountTaskInternal(User user) {
            return new GetRatePostUserActivityCountTask(user);
        }
    }
    ,RATE_COMMENT(1, 300, ChronoUnit.DAYS, null) {
        @Override
        protected GetUserActivityRateLimitCountTaskBase getActivityRateLimitCountTaskInternal(User user) {
            return new GetRateReplyUserActivityCountTask(user);
        }
    }
    ,POST(2, 10, ChronoUnit.DAYS, ReputationLevel.MEDIUM) {
        @Override
        protected GetUserActivityRateLimitCountTaskBase getActivityRateLimitCountTaskInternal(User user) {
            return new GetPostUserActivityCountTask(user);
        }
    }
    ,COMMENT(3, 30, ChronoUnit.DAYS, ReputationLevel.MEDIUM) {
        @Override
        protected GetUserActivityRateLimitCountTaskBase getActivityRateLimitCountTaskInternal(User user) {
            return new GetCommentUserActivityCountTask(user);
        }
    }
    ;

    private final int id;
    private final int limit;
    private final TemporalUnit limitPeriod;
    private final ReputationLevel bypassAtReputationLevel;

    UserActivityRateLimit(int id, int limit, TemporalUnit limitPeriod, ReputationLevel bypassAtReputationLevel) {
        assert limitPeriod != null : "Must always specify a limitPeriod for all UserActivityRateLimits";
        assert bypassAtReputationLevel== null || (!bypassAtReputationLevel.isConductNegative() && !bypassAtReputationLevel.isLow()) : "Should never specify a bypass for conduct negative or low quality users.";

        this.id = id;
        this.limit = limit;
        this.limitPeriod = limitPeriod;
        this.bypassAtReputationLevel = bypassAtReputationLevel;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getLimit() {
        return limit;
    }

    public TemporalUnit getLimitPeriod() {
        return limitPeriod;
    }

    public ReputationLevel getBypassAtReputationLevel() {
        return bypassAtReputationLevel;
    }

    public boolean isDisableActivity() {
        // bl: disable all of the activities entirely once we have a shutdown notice URL set
        return !isEmpty(areaContext().getAreaRlm().getSandboxedCommunitySettings().getShutdownNoticeUrl());
    }

    public String getErrorMessage() {
        return wordlet("userActivityRateLimit.errorMessage." + (isDisableActivity() ? "disabled." : "") + this);
    }

    protected abstract GetUserActivityRateLimitCountTaskBase getActivityRateLimitCountTaskInternal(User user);

    public GetUserActivityRateLimitCountTaskBase getActivityRateLimitCountTask(User user) {
        GetUserActivityRateLimitCountTaskBase task = getActivityRateLimitCountTaskInternal(user);
        assert this.equals(task.getRateLimit()) : "Tasks rate limit type does not match the type generating it. task.type/"+task.getRateLimit()+" type/"+this;

        return task;
    }
}