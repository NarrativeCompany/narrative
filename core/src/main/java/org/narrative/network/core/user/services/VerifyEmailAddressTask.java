package org.narrative.network.core.user.services;

import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.EmailAddressType;
import org.narrative.network.core.user.EmailAddressVerificationStep;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-10
 * Time: 11:22
 *
 * @author jonmark
 */
public class VerifyEmailAddressTask extends AreaTaskImpl<Object> {
    private final User user;
    private final EmailAddress emailAddress;
    private final EmailAddressVerificationStep verifiedType;

    public VerifyEmailAddressTask(User user, EmailAddress emailAddress, EmailAddressVerificationStep verifiedType) {
        assert exists(user) : "We should always be given a user!";
        assert exists(emailAddress) : "We should always have a emailAddress to verify!";
        assert isEqual(user, emailAddress.getUser()) : "The user/"+ emailAddress.getUser().getOid()+" for the email/"+emailAddress.getOid()+" should always be the same as the provided user/"+user.getOid();
        assert verifiedType != null : "We should alwayhs be given a verifiedType!";
        assert verifiedType.isNewUserStep() || emailAddress.getType().isPending() : "We should never provide a non-new user step verification step for a PRIMARY emailAddress/"+emailAddress.getOid();

        this.user = user;
        this.emailAddress = emailAddress;
        this.verifiedType = verifiedType;
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: first, let's add the type of the email that was verified to the verified list.
        emailAddress.getVerifiedSteps().add(verifiedType);

        // jw: first, let's handle the scenario where the user is verifying their active emailAddress
        if (emailAddress.getType().isPrimary()) {
            assert verifiedType.isNewUserStep() : "Should only ever use NEW_USER_STEP for primary email verification!";

            // jw: that is all that should be necessary for a user to verify their active email address. Short out.
            return null;
        }
        assert emailAddress.getType().isPending() : "Expected pending email address type, not/"+emailAddress.getType();

        // jw: We need to get a reference to the active email address for the following processing.
        EmailAddress activeEmailAddress = user.getUserFields().getEmailAddress();

        // jw: if there are still email addresses to verify, then we can just short out.
        if (!emailAddress.isVerified()) {
            return null;
        }

        // jw: first, we need to set the pendingEmailAddress as the active one on the account so that we can later delete
        //     the old active email address.
        user.getUserFields().setEmailAddress(emailAddress);

        // jw: to be safe, let's flush that
        PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

        // jw: now we should be safe to delete the old active email address
        EmailAddress.dao().delete(activeEmailAddress);

        // jw: similar to above, since there is a constraint on user/type in EmailAddress we need to flush that delete
        //     before we update the type on the pendingEmailAddress
        PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

        // jw: now we are free to update the type on the pendingEmailAddress
        emailAddress.setType(EmailAddressType.PRIMARY);

        // jw: since the users active email address changed we need to synchronize other data with their active email address
        if (user.isHasInternalCredentials()) {
            user.getInternalCredentials().setEmailAddress(emailAddress.getEmailAddress());
        }

        return null;
    }
}
