package org.narrative.network.core.user.services;

import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserStatus;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 11/9/17
 * Time: 2:41 PM
 */
public abstract class UserAccountStatusChangeEventTaskBase extends AreaTaskImpl<Object> {
    private final User user;
    private final boolean forDeletedAccount;
    private final boolean forDeactivatedAccount;
    private final String originalEmail;
    private final String originalName;
    private final boolean deletedAccountContents;

    public UserAccountStatusChangeEventTaskBase(User user, String originalName, String originalEmail, boolean deletedAccountContents) {
        super(false);
        this.user = user;
        this.forDeletedAccount = user.isDeleted();
        this.forDeactivatedAccount = user.isDeactivated();
        this.originalEmail = originalEmail;
        // bl: the originalName is only used in emails, so we need it in HTML format
        this.originalName = HtmlTextMassager.disableHtml(originalName);

        assert !deletedAccountContents || forDeletedAccount : "deletedAccountContents can only be used for deleted users";
        this.deletedAccountContents = deletedAccountContents;
    }

    // jw: let's expose these properties in case the email needs it for some reason!
    public boolean isForDeletedAccount() {
        return forDeletedAccount;
    }

    public boolean isForDeactivatedAccount() {
        return forDeactivatedAccount;
    }

    // jw: since this base class will be used for the front end user emails, and the admin emails, lets add a utility method to centralize the generation of this suffix
    public String getWordletSuffix() {
        return "." + getUserStatusForWordletSuffix() + (deletedAccountContents ? ".withContents" : "");
    }

    private UserStatus getUserStatusForWordletSuffix() {
        if (isForDeletedAccount()) {
            return UserStatus.DELETED;
        }
        if (isForDeactivatedAccount()) {
            return UserStatus.DEACTIVATED;
        }
        return UserStatus.ACTIVE;
    }

    public User getUser() {
        return user;
    }

    public String getOriginalEmail() {
        return originalEmail;
    }

    public String getOriginalName() {
        return originalName;
    }
}
