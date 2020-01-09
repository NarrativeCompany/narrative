package org.narrative.network.core.user.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.base.services.JoinGroupTask;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletType;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.Credentials;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.EmailAddressVerificationStep;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.customizations.narrative.personaljournal.PersonalJournal;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 5, 2005
 * Time: 4:39:38 PM
 */
public class CreateNewUser extends GlobalTaskImpl<User> {
    private String password;
    private String displayName;
    private String username;
    private String emailAddress;
    private boolean isEmailVerified;
    private FormatPreferences formatPreferences;
    private Timestamp registrationDatetime;

    private ImageOnDisk avatar;

    private AuthZone authZone;

    private boolean bypassModeration = false;

    public CreateNewUser(String password, @NotNull String displayName, @NotNull String username, @NotNull String emailAddress, boolean isEmailVerified) {
        this.password = password;
        this.displayName = displayName;
        this.username = username;
        this.emailAddress = emailAddress;
        this.isEmailVerified = isEmailVerified;
    }

    protected User doMonitoredTask() {
        if (authZone == null) {
            authZone = getNetworkContext().getAuthZone();
        }

        // skip these checks for imports to speed things up
        if (!NetworkRegistry.getInstance().isImporting()) {
            // make sure the email is available.
            // even if this is a legacy account "upgrade".  we're making the rule that the legacy GN system
            // is the "master" so we can't go allowing other email addresses in our system.
            if (exists(EmailAddress.dao().getByEmailAddress(emailAddress))) {
                throw UnexpectedError.getRuntimeException("Duplicate email address:" + emailAddress + ". Calling code should check this first!");
            }
        }

        Credentials credentials = null;
        // bl: don't create the Credentials if no password was supplied.  in this case, it's probably a Facebook/3rd
        // party login system registration.
        if (!isEmpty(password)) {
            credentials = authZone.getAndSaveNewCredentials(emailAddress, password);
        }

        // jw: as part of new user creation we need to create a wallet for them to use.
        Wallet userWallet = new Wallet(WalletType.USER);

        // jw: now that we have the wallet we need to save it before creating the user.
        Wallet.dao().save(userWallet);

        // jw: due to the bi-directional nature of the emailAddress, we need to create and save the email before we
        //     can associate it to the user.
        EmailAddress email;
        if (exists(credentials)) {
            email = new EmailAddress(null, credentials.getEmailAddress());
        } else {
            email = new EmailAddress(null, emailAddress);
        }
        EmailAddress.dao().save(email);

        //create the user
        User newUser = new User(authZone, email, userWallet, formatPreferences);

        if (isEmailVerified) {
            newUser.getUserFields().getEmailAddress().getVerifiedSteps().add(EmailAddressVerificationStep.NEW_USER_STEP);
        }

        newUser.setDisplayName(displayName);
        newUser.setUsername(username);

        if (registrationDatetime != null) {
            newUser.getUserFields().setRegistrationDate(registrationDatetime);
        }

        //save the objects
        User.dao().save(newUser);

        // Add UserKyc
        UserKyc userKyc = new UserKyc(newUser);
        UserKyc.dao().save(userKyc);

        // jw: now that the user is saved, let's store the personal journal. We need to do this after since we need the
        //     oid to be generated.
        PersonalJournal journal = new PersonalJournal(newUser);
        PersonalJournal.dao().save(journal);
        newUser.setPersonalJournal(journal);

        if (exists(credentials)) {
            newUser.addUserAuth(authZone.getInternalAuthProvider(), credentials.getOid().toString());
        }

        if (exists(avatar)) {
            avatar.updateUser(newUser);
            newUser.updateAvatar(avatar);

        }

        // update invites to this user's email address to be linked to the newly registered User
        if (!NetworkRegistry.getInstance().isImporting()) {
            JoinGroupTask joinTask = new JoinGroupTask(newUser);
            if (bypassModeration) {
                joinTask.setBypassModeration(true);
            }

            getNetworkContext().doAreaTask(authZone.getArea(), joinTask);

            // jw: since a invite by a administrator can cause the member to bypass moderation lets go ahead and fetch
            //     that here.  It will never revert to false in that task so this is safe.
            bypassModeration = joinTask.isBypassModeration();
        }

        PartitionType.flushAllOpenSessionsForCurrentPartitionGroup();

        return newUser;
    }

    public void setAuthZone(AuthZone authZone) {
        this.authZone = authZone;
    }

    public void setAvatar(ImageOnDisk avatar) {
        this.avatar = avatar;
    }

    public void setFormatPreferences(FormatPreferences formatPreferences) {
        this.formatPreferences = formatPreferences;
    }

    public void setRegistrationDatetime(Timestamp registrationDatetime) {
        this.registrationDatetime = registrationDatetime;
    }
}
