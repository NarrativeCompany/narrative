package org.narrative.network.customizations.narrative.service.api;

import org.narrative.network.customizations.narrative.service.api.model.TokenDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.DisableTwoFactorAuthInput;
import org.narrative.network.customizations.narrative.service.api.model.input.EnableTwoFactorAuthInput;
import net.glxn.qrgen.core.image.ImageType;

import java.io.OutputStream;
import java.util.List;

/**
 * Service to manage two factor authentication operations
 *
 */
public interface TwoFactorAuthenticationService {
    /**
     * Generate a new secret for the current user
     *
     * @return New secret for the current user
     */
    String generateNewSecret();

    /**
     * Generate backup codes from secret
     *
     * @param secret The secret to generate backup codes from
     * @return Backup codes for the specified secret
     */
    List<Integer> generateBackupCodes(String secret);

    /**
     * Validate a two factor code against the provided secret
     *
     * @param secret The secret to use
     * @param verificationCode The code to test against the secret
     * @return true if the code is valid false otherwise
     */
    boolean isTwoFactorCodeValid(String secret, int verificationCode);

    /**
     * Enable two factor authentication for the current user
     *
     * @param jwtString        String representing the *current* JWT for the current user
     * @param enableTwoFactorAuthInput Contains secret, verificationCode and rememberMe for the request
     * @return {@link TokenDTO} DTO with the JWT token
     */
    TokenDTO enableTwoFactorAuthForCurrentUser(String jwtString, EnableTwoFactorAuthInput enableTwoFactorAuthInput);

    /**
     *  Renew two factor authentication from a user's current JWT by generating a new JWT {@link TokenDTO} if
     *  validation of the provided verification code is successful.
     *
     * @param currentJWTString The user's current JWT
     * @param verificationCode 2FA verification code to use for validation
     * @param rememberMe true if the new JWT should use 2FA remember me
     * @return String representing the new JWT
     */
     TokenDTO renewTwoFactorAuthForCurrentJWT(String currentJWTString, int verificationCode, boolean rememberMe);

    /**
     * Disable two factor authentication for the current user
     * @param disableTwoFactorAuthInput Contains password for the current user and verificationCode for the current user
     */
    void disableTwoFactorAuthForCurrentUser(DisableTwoFactorAuthInput disableTwoFactorAuthInput);

    /**
     * Is two factor authentication enabled for the current user
     */
    boolean isTwoFactorAuthEnabledForCurrentUser();

    /**
     * Build a QR image representing the passed secret and current user context to be used for code validation.
     *
     * @param secret Secret to be used for generating the QR code
     * @param imageType The image type to generate
     * @return {@link OutputStream} from which the resulting image can be accessed
     */
    byte[] buildQRImageForCurrentUser(String secret, ImageType imageType);
}
