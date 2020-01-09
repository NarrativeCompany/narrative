package org.narrative.network.shared.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * User: brian
 * Date: Mar 4, 2016
 * Time: 10:04:45 AM
 */
public class ClusterRole extends PrimaryRole implements TransientRole {

    private final String username;

    private final transient OID oid;
    private transient FormatPreferences formatPreferences;

    @JsonCreator
    public ClusterRole(@JsonProperty("username") String username) {
        super(true);
        this.oid = OID.valueOf(username.hashCode());
        this.username = username;

    }

    public OID getOid() {
        return oid;
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
        return username;
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

    @Override
    public String getRoleStringForLogging() {
        StringBuilder sb = new StringBuilder();
        sb.append("username/").append(getUniqueName()).append("/").append(getOid());
        return sb.toString();
    }

}
