package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.ApplicationError;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.ResendEmailVerificationEmailTask;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 10/2/18
 * Time: 9:25 AM
 *
 * @author brian
 */
public class ResendVerificationEmailTask extends GlobalTaskImpl<Object> {
    private final User user;

    ResendVerificationEmailTask(User user) {
        this.user = user;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        if (user.getUserFields().isEmailVerified()) {
            throw new ApplicationError(wordlet("error.email_already_verified"));
        }
    }

    @Override
    protected Object doMonitoredTask() {
        getNetworkContext().doGlobalTask(new ResendEmailVerificationEmailTask(user));
        return null;
    }
}
