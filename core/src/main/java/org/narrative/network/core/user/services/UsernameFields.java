package org.narrative.network.core.user.services;

import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.user.User;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 5/21/18
 * Time: 4:17 PM
 */
public class UsernameFields {
    private final User user;
    private String username;

    public static final String USERNAME_PARAM = "username";

    public UsernameFields() {
        this.user = null;
    }

    public UsernameFields(User user) {
        this.username = user.getUsername();
        this.user = user;
    }

    public void validate(ValidationHandler handler, String fieldsParam) {
        validate(handler, fieldsParam + "." + USERNAME_PARAM, user, getUsername());
    }

    public static void validate(ValidationHandler handler, String usernameParamName, User existingUser, String username) {
        if (!handler.validateString(username, UsernameUtils.MIN_LENGTH, UsernameUtils.MAX_LENGTH, usernameParamName, "tags.site.page.usernameFields.handle")) {
            return;
        }
        if (UsernameUtils.INVALID_USERNAME_CHARS_PATTERN.matcher(username).find()) {
            handler.addWordletizedFieldError(usernameParamName, "usernameFields.invalidCharactersPresent");
            return;
        }
        User userWithUsername = User.dao().getUserByUsername(networkContext().getAuthZone(), username);
        if (exists(userWithUsername) && !isEqual(existingUser, userWithUsername)) {
            handler.addWordletizedFieldError(usernameParamName, "usernameFields.usernameAlreadyInUse");
            return;
        }
    }

    public User getUser() {
        return user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsernameForUpdate() {
        assert !isEmpty(getUsername()) : "By the time this gets called, we should always have a username!";

        return username;
    }
}
