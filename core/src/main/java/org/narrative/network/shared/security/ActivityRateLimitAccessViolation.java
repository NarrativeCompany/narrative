package org.narrative.network.shared.security;

import org.narrative.network.core.user.UserActivityRateLimit;
import org.narrative.network.customizations.narrative.service.api.model.ErrorType;

/**
 * Date: 2019-06-12
 * Time: 22:28
 *
 * @author jonmark
 */
public class ActivityRateLimitAccessViolation extends AccessViolation {

    public ActivityRateLimitAccessViolation(UserActivityRateLimit rateLimit) {
        super(rateLimit.getErrorMessage());
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.ACTIVITY_RATE_LIMIT_EXCEEDED;
    }
}
