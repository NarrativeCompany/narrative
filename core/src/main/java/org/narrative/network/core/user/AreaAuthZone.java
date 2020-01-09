package org.narrative.network.core.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.user.AreaCredentials;
import org.narrative.network.core.settings.area.community.advanced.SandboxedCommunitySettings;
import org.narrative.network.core.settings.global.services.translations.DefaultLocale;
import org.narrative.network.core.system.Encryption;
import org.narrative.network.core.system.NetworkRegistry;

import java.util.TimeZone;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Oct 1, 2010
 * Time: 12:43:16 PM
 *
 * @author brian
 */
public class AreaAuthZone extends AuthZone {

    AreaAuthZone(Area area) {
        this(area.getOid());
    }

    @JsonCreator
    AreaAuthZone(@JsonProperty("oid") OID oid) {
        super(oid);
    }

    @Override
    public Area getArea() {
        return Area.dao().get(oid);
    }

    @Override
    public boolean isSslEnabled() {
        return getArea().isSslEnabled();
    }

    @Override
    public String getBaseUrl() {
        return getArea().getPrimaryAreaUrl();
    }

    @Override
    public String getSecureBaseUrl() {
        assert getArea().isSslEnabled() : "Should only get the secure base URL for an area AuthZone when SSL is actually enabled!  Otherwise, SSL is unsupported!";
        // since SSL is enabled, the secure base URL is just the base URL.
        return getBaseUrl();
    }

    @Override
    public String getName() {
        return getArea().getAreaNameResolved();
    }

    public AreaRlm getAreaRlm() {
        return Area.getAreaRlm(getArea());
    }

    @Override
    protected Credentials createNewCredentials() {
        return new AreaCredentials(getAreaRlm());
    }

    @Override
    protected void saveCredentials(Credentials credentials) {
        AreaCredentials.dao().save((AreaCredentials) credentials);
    }

    @Override
    public AuthProvider getInternalAuthProvider() {
        return AuthProvider.SANDBOXED_AREA;
    }

    @Override
    protected Credentials getInternalCredentialsFromUserAuth(UserAuth userAuth) {
        Area area = getArea();
        assert exists(area) : "Should always identify an Area for non-network AuthZones!";
        assert areaContext() != null && isEqual(this, areaContext().getArea().getAuthZone()) : "Any time you look up AreaCredentials for a user, the User's AuthZone area should already be in scope!";

        AreaCredentials areaCredentials = AreaCredentials.dao().get(OID.valueOf(userAuth.getIdentifier()));
        if (exists(areaCredentials)) {
            assert isEqual(area.getOid(), areaCredentials.getAreaRlm().getOid()) : "Should always get an Area OID match when looking up AreaCredentials! area/" + area.getOid() + " acAreaRlm/" + areaCredentials.getAreaRlm().getOid() + " ac/" + areaCredentials.getOid();
            return areaCredentials;
        }
        return null;
    }

    @Override
    public String getStaticBaseUrl() {
        return getBaseUrl() + NetworkRegistry.getInstance().getStaticPath();
    }

    @Override
    public String getSecureStaticBaseUrl() {
        assert getArea().isSslEnabled() : "Should only get the secure static base URL for an area AuthZone when SSL is actually enabled!  Otherwise, SSL is unsupported!";
        // since SSL is enabled, the secure static base URL is just the static base URL.
        return getStaticBaseUrl();
    }

    @Override
    public SandboxedCommunitySettings getSandboxedCommunitySettings() {
        return getAreaRlm().getSandboxedCommunitySettings();
    }

    @Override
    public AuthZoneMaster getAuthZoneMaster() {
        return NarrativeAuthZoneMaster.INSTANCE;
    }

    @Override
    public TimeZone getDefaultGuestTimeZone() {
        return getAreaRlm().getSandboxedCommunitySettings().getDefaultGuestTimeZone();
    }

    @Override
    public DefaultLocale getDefaultLocale() {
        return getAreaRlm().getSandboxedCommunitySettings().getDefaultLocale();
    }

    @Override
    public String getReplyToEmailAlias() {
        if(NetworkRegistry.getInstance().isProductionServer()) {
            return NarrativeAuthZoneMaster.NARRATIVE_NAME;
        }
        return NarrativeAuthZoneMaster.NARRATIVE_NAME + " " + NetworkRegistry.getInstance().getClusterId();
    }

    @Override
    public String getReplyToEmailAddress() {
        return getAuthZoneMaster().getReplyToEmailAddress();
    }

    @Override
    public boolean isEncryptionEnabled() {
        return Encryption.INSTANCE.isEnabled() && getSandboxedCommunitySettings().isEncryptionEnabled();
    }

    @Override
    public String getEncryptionPassword() {
        return getSandboxedCommunitySettings().getEncryptionPassword();
    }

    @Override
    public byte[] getEncryptionInitializationVector() {
        return getSandboxedCommunitySettings().getEncryptionInitializationVector();
    }

}
