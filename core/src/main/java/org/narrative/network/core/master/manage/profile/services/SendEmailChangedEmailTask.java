package org.narrative.network.core.master.manage.profile.services;

import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.EmailAddressVerificationStep;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.email.NetworkMailUtil;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 21, 2006
 * Time: 12:08:22 PM
 */
public class SendEmailChangedEmailTask extends GlobalTaskImpl<Object> {

    private final User user;
    private final EmailAddress pendingEmailAddress;

    private EmailAddressVerificationStep forVerificationStep;

    public SendEmailChangedEmailTask(User user) {
        super(false);
        this.user = user;
        pendingEmailAddress = user.getUserFields().getPendingEmailAddress();

        assert exists(pendingEmailAddress) : "The user should always have a pending email address set when this is called!";
    }

    protected Object doMonitoredTask() {
        // jw: we need to send an email to both email addresses for verification
        forVerificationStep = EmailAddressVerificationStep.VERIFY_PRIMARY;
        NetworkMailUtil.sendJspCreatedEmail(this, user);

        forVerificationStep = EmailAddressVerificationStep.VERIFY_PENDING;
        NetworkMailUtil.sendJspCreatedEmail(this, user, null, getPendingEmailAddress().getEmailAddress());

        return null;
    }

    public User getUser() {
        return user;
    }

    public EmailAddress getPendingEmailAddress() {
        return pendingEmailAddress;
    }

    public EmailAddressVerificationStep getForVerificationStep() {
        return forVerificationStep;
    }

    public String getConfirmationUrl() {
        return getPendingEmailAddress().getConfirmationUrl(getForVerificationStep());
    }

    public String getCancelUrl() {
        return getPendingEmailAddress().getCancelUrl(getForVerificationStep());
    }
}
