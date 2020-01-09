package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.IPStringUtil;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.SuspendEmailInput;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 10/4/18
 * Time: 8:48 AM
 *
 * @author brian
 */
public class SuspendUserEmailsTask extends GlobalTaskImpl<Object> {

    private static final String SUSPEND_EMAILS_PRIVATE_KEY = SuspendUserEmailsTask.class.getName() + "-private-key-ExeEoutwgkqrkBYGAtYzVNw6sC7XrMKu]2sNgTx?uRAFQEK8uJXBvoqHE.hgysRA";

    private final User user;
    private final SuspendEmailInput suspendEmailInput;

    public SuspendUserEmailsTask(User user, SuspendEmailInput suspendEmailInput) {
        this.user = user;
        this.suspendEmailInput = suspendEmailInput;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        validate(user, suspendEmailInput);
    }

    public static void validate(User user, SuspendEmailInput suspendEmailInput) {
        if (!isEqual(suspendEmailInput.getToken(), getAuthKeyForSuspendAllEmails(user))) {
            throw new ApplicationError(wordlet("SuspendUserEmailsTask.suspendLinkInvalid"));
        }
        // bl: if the user's email address is different, then this is no longer a valid request.
        // note that i'm intentionally doing this after we've validated the key so that people can't use this to
        // guess a user's email address. they must have a valid key before we even check this.
        if(!isEqual(user.getEmailAddress(), suspendEmailInput.getEmailAddress())) {
            throw new ApplicationError(wordlet("SuspendUserEmailsTask.suspendLinkNoLongerValid"));
        }
    }

    @Override
    protected Object doMonitoredTask() {
        user.getPreferences().setSuspendAllEmails(true);
        return null;
    }

    public static String getAuthKeyForSuspendAllEmails(User user) {
        List<Object> objectsForKey = user.getObjectsForSecurityToken();
        objectsForKey.add(SUSPEND_EMAILS_PRIVATE_KEY);
        return IPStringUtil.getMD5DigestFromObjects(objectsForKey);
    }
}
