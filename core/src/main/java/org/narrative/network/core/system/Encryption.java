package org.narrative.network.core.system;

import org.narrative.common.util.UnexpectedError;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 11/30/16
 * Time: 10:41 AM
 */
public enum Encryption {
    INSTANCE;

    private boolean enableEncryption;

    public boolean isEnabled() {
        return enableEncryption;
    }

    public boolean isAttachmentEncryptionEnabled() {
        // bl: never going to use encryption for attachments. they're always public.
        return false;
    }

    public boolean isUseMySqlEncryption() {
        // bl: we are never going to use MySQL encryption now
        return false;
    }

    public void init(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;

        if (isEnabled()) {
            // attempt to enable unlimited-strength crypto.
            // bl: stole this from org.jruby.Ruby. by setting isRestricted=false, we don't need to install custom security policy files.
            // this will allow us to use 256-bit AES and avoid errors like:
            // java.security.InvalidKeyException: Illegal key size or default parameters
            // refer: http://stackoverflow.com/a/20286961
            // note that apparently OpenJDK made isRestricted final. thankfully, it's not final in Oracle's JDK, so we can just set its value directly here.
            try {
                Class jceSecurity = Class.forName("javax.crypto.JceSecurity");
                Field isRestricted = jceSecurity.getDeclaredField("isRestricted");
                isRestricted.setAccessible(true);
                // bl: starting in Oracle JDK 102, the field is now final. but! we can actually make the field no longer final so we can modify the value!
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                // this is what removes the final flag from the isRestricted field.
                modifiersField.setInt(isRestricted, isRestricted.getModifiers() & ~Modifier.FINAL);
                // now we should be able to set isRestricted to false!
                isRestricted.set(null, false);
                // and let's set isRestricted back to final for good measure
                modifiersField.setInt(isRestricted, isRestricted.getModifiers() & Modifier.FINAL);
                // now clean up after ourselves so everything is back to normal by making these fields private once again
                modifiersField.setAccessible(false);
                isRestricted.setAccessible(false);
            } catch (Exception e) {
                throw UnexpectedError.getRuntimeException("Unable to enable unlimited-strength crypto! Can't use encryption. Fix it!", e);
            }
        }
    }

    public OutputStream encryptingOutputStream(OutputStream stream, String password, byte[] iv, byte[] salt) {
        // jw: in the event where the underlying streams data is not actually encrypted we will just return the actual stream.
        if (!isEnabled() || isEmpty(password) || iv == null || salt == null) {
            return stream;
        }

        return new CipherOutputStream(stream, getCipher(true, password, iv, salt));
    }

    public InputStream decryptingInputStream(InputStream stream, String password, byte[] iv, byte[] salt) {
        // jw: in the event where the underlying streams data is not actually encrypted we will just return the actual stream.
        if (!isEnabled() || isEmpty(password) || iv == null || salt == null) {
            return stream;
        }

        return new CipherInputStream(stream, getCipher(false, password, iv, salt));
    }

    public OutputStream decryptingOutputStream(OutputStream stream, String password, byte[] iv, byte[] salt) {
        // jw: in the event where the underlying streams data is not actually encrypted we will just return the actual stream.
        if (!isEnabled() || isEmpty(password) || iv == null || salt == null) {
            return stream;
        }

        return new CipherOutputStream(stream, getCipher(false, password, iv, salt));
    }

    private Cipher getCipher(boolean forEncryption, String password, byte[] iv, byte[] salt) {
        assert isEnabled() : "Should only be creating a Cipher when the encryption stream password is set.";

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(keySpec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            // file decryption
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(forEncryption ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));

            return cipher;

        } catch (Exception e) {
            throw UnexpectedError.getRuntimeException("Failed creating Cipher!", e);
        }
    }

    public byte[] generateSalt() {
        byte[] salt = new byte[8];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);

        return salt;
    }
}
