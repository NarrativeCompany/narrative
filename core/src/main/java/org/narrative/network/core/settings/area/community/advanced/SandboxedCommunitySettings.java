package org.narrative.network.core.settings.area.community.advanced;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeBase;
import org.narrative.network.core.propertyset.base.services.annotations.IsDefaultRequired;
import org.narrative.network.core.propertyset.base.services.annotations.PropertySetTypeDef;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.settings.global.services.translations.DefaultLocale;
import org.narrative.network.core.system.Encryption;
import org.narrative.network.customizations.narrative.niches.NarrativeCircleType;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Oct 4, 2010
 * Time: 4:15:00 PM
 *
 * @author brian
 */
@PropertySetTypeDef(name = SandboxedCommunitySettings.SANDBOXED_COMMUNITY_SETTINGS_NAME, defaultProvider = DefaultSandboxedCommunitySettings.class)
public abstract class SandboxedCommunitySettings implements PropertySetTypeBase {

    public static final String SANDBOXED_COMMUNITY_SETTINGS_NAME = "SandboxedCommunitySettings";

    @IsDefaultRequired(true)
    public abstract TimeZone getDefaultGuestTimeZone();

    public abstract void setDefaultGuestTimeZone(TimeZone timeZone);

    @IsDefaultRequired(true)
    public abstract DefaultLocale getDefaultLocale();

    public abstract void setDefaultLocale(DefaultLocale defaultLocale);

    public boolean isEncryptionEnabled() {
        // bl: encryption is enabled on this site if it has an initialization vector.
        return !isEmpty(getEncryptionPassword()) && getEncryptionInitializationVector() != null;
    }

    @IsDefaultRequired(false)
    public abstract String getEncryptionPassword();

    public abstract void setEncryptionPassword(String password);

    @IsDefaultRequired(false)
    public abstract byte[] getEncryptionInitializationVector();

    public abstract void setEncryptionInitializationVector(byte[] vector);

    public void initEncryption() {
        assert Encryption.INSTANCE.isEnabled() : "Should only ever manage the encryption stream settings when the cluster is setup to support it (a password must be set)";

        // jw: if this is the first time that encryption has been turned on for this site, we need to generate a initialization vector for this site.
        if (!isEncryptionEnabled()) {
            // bl: use a random, alphanumeric password between 45 and 55 characters.
            // this method will include both uppercase and lowercase alphabetic characters.
            setEncryptionPassword(RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(45, 55)));

            // jw: first, lets just make up a salt, this does not matter, we just need something to create the Cipher
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec keySpec = new PBEKeySpec(getEncryptionPassword().toCharArray(), Encryption.INSTANCE.generateSalt(), 65536, 256);
                SecretKey secretKey = factory.generateSecret(keySpec);
                SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secret);
                AlgorithmParameters params = cipher.getParameters();

                setEncryptionInitializationVector(params.getParameterSpec(IvParameterSpec.class).getIV());

            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidParameterSpecException | InvalidKeySpecException e) {
                throw UnexpectedError.getRuntimeException("Failed generating initialization vector!", e);
            }
        }
    }

    @IsDefaultRequired(false)
    public abstract Map<NarrativeCircleType, OID> getNarrativeCircleAssociations();

    public abstract void setNarrativeCircleAssociations(Map<NarrativeCircleType, OID> associations);

    public Map<NarrativeCircleType, AreaCircle> getCirclesByNarrativeCircleType() {
        Map<NarrativeCircleType, OID> rawLookup = getNarrativeCircleAssociations();
        if (isEmptyOrNull(rawLookup)) {
            return Collections.emptyMap();
        }

        Map<NarrativeCircleType, AreaCircle> results = new HashMap<>();
        Map<OID, AreaCircle> oidToCircle = AreaCircle.dao().getIDToObjectsFromIDs(rawLookup.values());
        for (NarrativeCircleType circleType : NarrativeCircleType.values()) {
            OID circleOid = rawLookup.get(circleType);
            AreaCircle circle = oidToCircle.get(circleOid);
            if (!exists(circle)) {
                continue;
            }
            results.put(circleType, circle);
        }
        return Collections.unmodifiableMap(results);
    }

    public abstract String getNrveScriptHash();

    public abstract void setNrveScriptHash(String nrveScriptHash);

    @IsDefaultRequired(false)
    public abstract String getExtraNarrativeCompanyNeoWalletAddress();

    public abstract void setExtraNarrativeCompanyNeoWalletAddress(String neoWalletAddress);

    @IsDefaultRequired(false)
    public abstract boolean isHasSetupNarrativeCustomizationBefore();

    public abstract void setHasSetupNarrativeCustomizationBefore(boolean has);

    public abstract boolean isFiatPaymentsEnabled();

    public abstract void setFiatPaymentsEnabled(boolean enabled);

    @IsDefaultRequired(false)
    public abstract String getShutdownNoticeUrl();
    public abstract void setShutdownNoticeUrl(String shutdownNoticeUrl);
}
