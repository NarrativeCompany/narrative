package org.narrative.network.shared.security;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Transient;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 5, 2005
 * Time: 5:29:45 PM
 * This is the system role.  All processes that need to be able to pass all security checks should use this role.  For
 * instance, scheduled context will run under the context of this role.
 */
public class SystemRole extends PrimaryRole implements TransientRole {

    private static final OID SYSTEM_ROLE_OID = new OID(1);

    private FormatPreferences formatPreferences;

    public SystemRole() {
        super(true);

    }

    public OID getOid() {
        return SYSTEM_ROLE_OID;
    }

    @Override
    public AuthZone getAuthZone() {
        return null;
    }

    @Transient
    public boolean isSpider() {
        return false;
    }

    public boolean isRegisteredUser() {
        return false;
    }

    public String getDisplayNameResolved() {
        return "System Role";
    }

    public User getUser() {
        return null;
    }

    @NotNull
    public AreaRole getAreaRoleForArea(Area area) {
        assert false : "Can't get an AreaRole for a SystemRole!";
        return null;
    }

    public AreaUser getAreaUser() {
        return null;
    }

    public FormatPreferences getFormatPreferences() {
        if (formatPreferences == null) {
            formatPreferences = FormatPreferences.getDefaultFormatPreferences();
        }
        return formatPreferences;
    }

    public void setFormatPreferences(FormatPreferences formatPreferences) {
        this.formatPreferences = formatPreferences;
    }

    @Override
    public String getRoleStringForLogging() {
        return "SystemRole";
    }

}
