package org.narrative.network.shared.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.security.area.base.AreaGuest;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.jetbrains.annotations.NotNull;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Dec 22, 2005
 * Time: 7:55:52 AM
 *
 * @author Brian
 */
public class Guest extends PrimaryRole implements TransientRole {

    private final AuthZone authZone;
    private boolean isSpider = false;
    private String displayName;

    private FormatPreferences formatPreferences = FormatPreferences.getDefaultFormatPreferences();

    public Guest(AuthZone authZone) {
        this(authZone, OIDGenerator.getNextOID());
    }

    /**
     * Special constructor for deserializing session info - no need to look up default guest time zone since we already
     * have it serialzed.  It will have been populated during initial construction.
     */
    @JsonCreator
    private Guest(@JsonProperty("authZone") AuthZone authZone, @JsonProperty("oid") OID oid, @JsonProperty("isSpider") boolean isSpider) {
        super(true);
        assert oid!=null : "Shouldn't call Guest(OID) constructor without a valid OID!";
        setOid(oid);
        this.authZone = authZone;
    }

    public Guest(AuthZone authZone, OID oid) {
        super(true);
        assert oid != null : "Shouldn't call Guest(OID) constructor without a valid OID!";
        setOid(oid);
        this.authZone = authZone;
        // bl: be sure to default all guests to use the time zone default specified for the AuthZone
        if (authZone != null) {
            this.formatPreferences.setTimeZone(authZone.getDefaultGuestTimeZone());
        }
    }

    @Override
    public AuthZone getAuthZone() {
        return authZone;
    }

    public boolean isSpider() {
        return isSpider;
    }

    public void setSpider(boolean spider) {
        isSpider = spider;
    }

    public FormatPreferences getFormatPreferences() {
        return formatPreferences;
    }

    public void setFormatPreferences(FormatPreferences formatPreferences) {
        this.formatPreferences = formatPreferences;
    }

    public boolean isRegisteredUser() {
        return false;
    }

    public User getUser() {
        assert false : "getUser() not supported for the Guest role type";
        return null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayNameResolved() {
        if (!isEmpty(displayName)) {
            return wordlet("role.customGuestName", displayName);
        }
        return getDefaultDisplayName();
    }

    @NotNull
    public AreaRole getAreaRoleForArea(Area area) {
        assert isEqual(getAuthZone(), area.getAuthZone()) : "Should never attempt to get an AreaRole for a Guest that has a different AuthZone than the specified Area! oid/" + getOid() + " uAz/" + getAuthZone() + " a/" + area.getOid() + " aAz/" + area.getAuthZone();

        // give the area guest the same OID as the Guest
        return new AreaGuest(area, this);
    }

    @Override
    public String getRoleStringForLogging() {
        StringBuilder sb = new StringBuilder();
        sb.append("Guest/").append(getOid());
        sb.append("/Spider/").append(isSpider());
        if (!isEmpty(displayName)) {
            sb.append("/Name/").append(displayName);
        }
        return sb.toString();
    }

    public static String getDefaultDisplayName() {
        return wordlet("role.guest");
    }

}
