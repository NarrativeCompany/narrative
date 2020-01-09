package org.narrative.network.core.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.settings.area.community.advanced.SandboxedCommunitySettings;
import org.narrative.network.core.settings.global.services.translations.DefaultLocale;
import org.narrative.network.core.system.Encryption;
import org.narrative.network.core.user.services.AuthZoneJacksonDeserializer;
import org.narrative.network.core.user.services.AuthZoneJacksonSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.TimeZone;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Sep 20, 2010
 * Time: 1:58:47 PM
 *
 * @author brian
 */
@JsonSerialize(using = AuthZoneJacksonSerializer.class)
@JsonDeserialize(using = AuthZoneJacksonDeserializer.class)
public abstract class AuthZone implements Serializable, AuthRealm {

    protected final OID oid;

    @JsonCreator
    protected AuthZone(@JsonProperty("oid") OID oid) {
        assert oid != null : "Should never create an AuthZone with a non-null OID!";
        this.oid = oid;
    }

    public OID getOid() {
        return oid;
    }

    @Override
    public AuthZone getAuthZone() {
        return this;
    }

    public abstract Area getArea();

    public abstract boolean isSslEnabled();

    public abstract String getBaseUrl();

    public abstract String getSecureBaseUrl();

    public String getBaseUrlForCurrentScheme() {
        return networkContext().isUseSecureUrls() ? getSecureBaseUrl() : getBaseUrl();
    }

    public abstract String getName();

    public Credentials getAndSaveNewCredentials(String emailAddress, String password) {
        Credentials credentials = createNewCredentials();
        // set the e-mail address.
        credentials.setEmailAddress(emailAddress.toLowerCase());
        credentials.getPasswordFields().setPassword(password);
        saveCredentials(credentials);

        return credentials;
    }

    protected abstract Credentials createNewCredentials();

    protected abstract void saveCredentials(Credentials credentials);

    public abstract AuthProvider getInternalAuthProvider();

    public Credentials getInternalCredentials(UserAuth userAuth) {
        if (!exists(userAuth)) {
            return null;
        }
        return getInternalCredentialsFromUserAuth(userAuth);
    }

    protected abstract Credentials getInternalCredentialsFromUserAuth(UserAuth userAuth);

    public abstract String getStaticBaseUrl();

    public abstract String getSecureStaticBaseUrl();

    public String getStaticBaseUrlForCurrentScheme() {
        return networkContext().isUseSecureUrls() ? getSecureStaticBaseUrl() : getStaticBaseUrl();
    }

    public abstract SandboxedCommunitySettings getSandboxedCommunitySettings();

    public abstract AuthZoneMaster getAuthZoneMaster();

    public abstract TimeZone getDefaultGuestTimeZone();

    public abstract DefaultLocale getDefaultLocale();

    public static AuthZone getAuthZone(Area area) {
        assert exists(area) : "Should always have an Area now!";
        return new AreaAuthZone(area);
    }

    public static AuthZone getAuthZone(long val) {
        return getAreaAuthZone(OID.valueOf(val));
    }

    private static AuthZone getAreaAuthZone(OID areaOid) {
        return new AreaAuthZone(areaOid);
    }

    public static AuthZone getAuthZoneFromAreaOid(OID areaOid) {
        assert areaOid != null : "Should never attempt to get AuthZone without an Area OID now!";

        return getAreaAuthZone(areaOid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuthZone authZone = (AuthZone) o;

        if (oid != null ? !oid.equals(authZone.oid) : authZone.oid != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return oid != null ? oid.hashCode() : 0;
    }

    @Override
    public String toString() {
        return oid.toString();
    }

    public abstract String getReplyToEmailAlias();
    public abstract String getReplyToEmailAddress();

    public abstract boolean isEncryptionEnabled();

    public abstract String getEncryptionPassword();

    public abstract byte[] getEncryptionInitializationVector();

    public byte[] generateEncryptionSalt() {
        if (!isEncryptionEnabled()) {
            return null;
        }

        return Encryption.INSTANCE.generateSalt();
    }

    public OutputStream encryptingOutputStream(OutputStream stream, byte[] salt) {
        return Encryption.INSTANCE.encryptingOutputStream(stream, getEncryptionPassword(), getEncryptionInitializationVector(), salt);
    }

    public InputStream decryptingInputStream(InputStream stream, byte[] salt) {
        return Encryption.INSTANCE.decryptingInputStream(stream, getEncryptionPassword(), getEncryptionInitializationVector(), salt);
    }

    public boolean isFiatPaymentsEnabled() {
        return getSandboxedCommunitySettings().isFiatPaymentsEnabled();
    }

}
