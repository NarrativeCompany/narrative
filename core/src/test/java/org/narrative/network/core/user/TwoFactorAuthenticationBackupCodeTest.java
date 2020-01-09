package org.narrative.network.core.user;

import org.jooq.lambda.function.Function1;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 2019-06-20
 * Time: 08:57
 *
 * @author jonmark
 */
class TwoFactorAuthenticationBackupCodeTest {

    @Test
    void getBackupCode_Success_consistentResults() {
        ensureConsistentResults(TwoFactorAuthenticationBackupCode.CODE_1::getBackupCode, "Test");
    }

    @Test
    void getIdPlacementIndex_Success_consistentResults() {
        ensureConsistentResults(TwoFactorAuthenticationBackupCode::getIdPlacementIndex, "Test");
    }

    @Test
    void getIdPlacementIndex_Success_expectedValueRange() {
        // jw: we can never be sure that we didn't just happen to miss the absolute outer edges, but trying 1000 times
        //     to get outside of it should help confidence.
        for (int i = 0; i < 1000; i++) {
            int index = TwoFactorAuthenticationBackupCode.getIdPlacementIndex("Test-"+(100000000000000000L * Math.random()));
            assertTrue(index >= 0, "Resulting index should always be zero or greater!");
            assertTrue(index <= 5, "Resulting index should always be five or less!");
        }
    }

    @Test
    void getBackupCode_Success_expectedStructure() {
        String secret = "Test-"+(100000000000000000L * Math.random());
        int idIndex = TwoFactorAuthenticationBackupCode.getIdPlacementIndex(secret);

        for (TwoFactorAuthenticationBackupCode backupCode : TwoFactorAuthenticationBackupCode.values()) {
            int code = backupCode.getBackupCode(secret);

            assertTrue(code >= 0, "The code should always be a positive (or extremely unlikely 0) number!");

            String codeString = String.format("%06d", code);

            // jw: ensure that he character at the idIndex matches the first character
            assertEquals(Integer.toString(backupCode.getId()).charAt(0), codeString.charAt(idIndex));
        }
    }


    @Test
    void getAllBackupCodes_Success_ensureUnique() {
        List<Integer> codes = TwoFactorAuthenticationBackupCode.getAllBackupCodes("Test");

        assertTrue(codes.size() == 10, "We should always generate 10 codes!");

        Set<Integer> uniqueCodes = new HashSet<>(codes);

        assertTrue(uniqueCodes.size() == codes.size(), "We should never lose codes by storing them in a set.");
    }

    private static void ensureConsistentResults(Function1<String, Integer> function, String secretKey) {
        // jw: just generate a value up front.
        Integer expectedResult = function.apply(secretKey);

        // jw: let's test the function 100 times to ensure it returns the same result
        for (int i = 0; i < 100; i++) {
            assertEquals(expectedResult, function.apply(secretKey));
        }
    }
}