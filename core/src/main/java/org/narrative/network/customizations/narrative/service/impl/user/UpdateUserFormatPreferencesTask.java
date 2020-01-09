package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

public class UpdateUserFormatPreferencesTask extends GlobalTaskImpl<User> {

    private final OID userOid;
    private final FormatPreferences formatPreferences;

    public UpdateUserFormatPreferencesTask(OID userOid, FormatPreferences formatPreferences) {
        this.userOid = userOid;
        this.formatPreferences = formatPreferences;
    }

    @Override
    protected User doMonitoredTask() {
        User updatedUser = User.dao().get(userOid);
        updatedUser.setFormatPreferences(formatPreferences);
        return updatedUser;
    }
}
