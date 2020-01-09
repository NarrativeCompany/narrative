package org.narrative.network.core.user;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.*;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-17
 * Time: 15:29
 *
 * @author jonmark
 */
public enum TwoFactorAuthenticationBackupCode implements IntegerEnum {
    // jw: purposefully starting at 1 to align names with id.
    CODE_1(1)
    ,CODE_2(2)
    ,CODE_3(3)
    ,CODE_4(4)
    ,CODE_5(5)
    ,CODE_6(6)
    ,CODE_7(7)
    ,CODE_8(8)
    ,CODE_9(9)
    ,CODE_10(10)
    ;

    public static final String TYPE = "org.narrative.network.core.user.TwoFactorAuthenticationBackupCode";

    private final int id;

    TwoFactorAuthenticationBackupCode(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getBackupCode(User user) {
        assert exists(user) : "Should only ever call this method with a user!";
        assert user.isTwoFactorAuthenticationEnabled() : "Should only ever call this method with a user who has 2FA enabled! user/"+user.getOid();

        return getBackupCode(user.getTwoFactorAuthenticationSecretKey());
    }

    public int getBackupCode(String secretKey) {
        int backupCode = getRawBackupCode(secretKey);
        // jw: to ensure uniqueness of all codes we are joing to place the id of this enum into the code, but to mitigate
        //     the appearance of this to the user we will pick a position to place the code into based on the secret.
        int idPlacement = getIdPlacementIndex(secretKey);

        // jw: let's replace the character from the ID into the result through string conversion.
        char[] chars = String.format("%06d", backupCode).toCharArray();
        chars[idPlacement] = Integer.toString(id).charAt(0);
        return Integer.parseInt(new String(chars));
    }

    private int getRawBackupCode(String secretKey) {
        String seed = secretKey + "-"+ name();
        try {
            byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(seed.getBytes());

            // jw: create a big integer from the bytes
            BigInteger integer = new BigInteger(md5Bytes);

            // jw: now, let's truncate that down to 6 digits
            integer = integer.mod(BigInteger.valueOf(1000000));

            // jw: let's return this as a integer
            return integer.intValue();

        } catch (NoSuchAlgorithmException e) {
            throw UnexpectedError.getRuntimeException("unable to md5 bytes", e);
        }
    }

    public static int getIdPlacementIndex(String secretKey) {
        try {
            byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(secretKey.getBytes());

            // jw: create a big integer from the bytes
            BigInteger integer = new BigInteger(md5Bytes);

            // jw: now, let's mode this down to 0-5
            integer = integer.mod(BigInteger.valueOf(6));

            // jw: Since we are generating an index we can just return this value.
            return integer.intValue();

        } catch (NoSuchAlgorithmException e) {
            throw UnexpectedError.getRuntimeException("unable to md5 bytes", e);
        }
    }

    public static List<Integer> getAllBackupCodes(String secretKey) {
        List<Integer> backupCodes = new ArrayList<>(values().length);
        for (TwoFactorAuthenticationBackupCode backupCode : TwoFactorAuthenticationBackupCode.values()) {
            backupCodes.add(backupCode.getBackupCode(secretKey));
        }

        // jw: randomize the list
        Collections.shuffle(backupCodes);

        return backupCodes;
    }
}