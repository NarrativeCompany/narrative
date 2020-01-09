package org.narrative.network.customizations.narrative.service.impl.security;

import com.google.common.annotations.VisibleForTesting;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.security.NarrativeAuthenticationToken;
import org.narrative.network.core.security.jwt.JwtTokenInvalidException;
import org.narrative.network.core.security.jwt.JwtUtil;
import org.narrative.network.core.user.TwoFactorAuthenticationBackupCode;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.CheckTwoFactorAuthenticationBackupCodeTask;
import org.narrative.network.core.user.services.TwoFactorAuthUtils;
import org.narrative.network.customizations.narrative.controller.postbody.user.TwoFactoryVerifyInputDTO;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.TwoFactorAuthenticationService;
import org.narrative.network.customizations.narrative.service.api.UserService;
import org.narrative.network.customizations.narrative.service.api.model.TokenDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.DisableTwoFactorAuthInput;
import org.narrative.network.customizations.narrative.service.api.model.input.EnableTwoFactorAuthInput;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.impl.common.ValidationExceptionFactory;
import org.narrative.network.shared.security.AuthZoneLoginRequired;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.narrative.network.core.security.jwt.JwtUtil.*;

/**
 * Service to manage Google authenticator two factor authentication operations
 */
@Service
public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService {
    private static final int QR_CODE_WIDTH = 200;
    private static final int QR_CODE_HEIGHT = 200;

    private final UserService userService;
    private final AreaTaskExecutor taskExecutor;
    private final StaticMethodWrapper staticMethodWrapper;
    private final ValidationExceptionFactory validationExceptionFactory;
    private final JwtUtil jwtUtil;
    private final MessageSourceAccessor messageSource;
    private final NarrativeProperties narrativeProperties;

    public TwoFactorAuthenticationServiceImpl(UserService userService, AreaTaskExecutor taskExecutor,
                                              StaticMethodWrapper staticMethodWrapper,
                                              ValidationExceptionFactory validationExceptionFactory,
                                              JwtUtil jwtUtil, MessageSourceAccessor messageSource,
                                              NarrativeProperties narrativeProperties) {
        this.userService = userService;
        this.taskExecutor = taskExecutor;
        this.staticMethodWrapper = staticMethodWrapper;
        this.validationExceptionFactory = validationExceptionFactory;
        this.jwtUtil = jwtUtil;
        this.messageSource = messageSource;
        this.narrativeProperties = narrativeProperties;
    }

    /**
     * Generate a new secret for the current user
     *
     * @return New secret for the current user
     */
    @Override
    public String generateNewSecret() {
        return taskExecutor.executeAreaTask(new AreaTaskImpl<String>(false) {
            @Override
            protected String doMonitoredTask() {
                //Must be a registered user
                staticMethodWrapper.networkContext().getPrimaryRole().checkRegisteredUser();

                return TwoFactorAuthUtils.getSecretKey();
            }
        });
    }

    @Override
    public List<Integer> generateBackupCodes(String secret) {
        return TwoFactorAuthenticationBackupCode.getAllBackupCodes(secret);
    }

    /**
     * Validate a two factor code against the provided secret
     *
     * @param secret The secret to use
     * @param verificationCode The code to test against the secret
     * @return true if the code is valid false otherwise
     */
    @Override
    public boolean isTwoFactorCodeValid(String secret, int verificationCode) {
        return taskExecutor.executeAreaTask(new AreaTaskImpl<Boolean>(false) {
            @Override
            protected Boolean doMonitoredTask() {
                //Must be a registered user
                staticMethodWrapper.networkContext().getPrimaryRole().checkRegisteredUser();

                return TwoFactorAuthUtils.isValidCode(secret, verificationCode);
            }
        });
    }

    /**
     * Enable two factor authentication for the current user
     *
     * @param currentJWTString String representing the *current* JWT for the current user
     * @param enableTwoFactorAuthInput Contains secret, verificationCode and rememberMe for the request
     * @return {@link TokenDTO} DTO with the JWT token
     */
    @Override
    public TokenDTO enableTwoFactorAuthForCurrentUser(String currentJWTString, EnableTwoFactorAuthInput enableTwoFactorAuthInput) {
        //Validate the code
        if (!isTwoFactorCodeValid(enableTwoFactorAuthInput.getSecret(), enableTwoFactorAuthInput.getTwoFactorAuthCode())) {
            throw validationExceptionFactory.forInvalidFieldError("Invalid verification code", EnableTwoFactorAuthInput.Fields.twoFactorAuthCode);
        }

        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        User user = primaryRole.getUser();

        if (!user.getInternalCredentials().getPasswordFields().isCorrectPassword(enableTwoFactorAuthInput.getCurrentPassword())) {
            throw validationExceptionFactory.forFieldError("Bad password", EnableTwoFactorAuthInput.Fields.currentPassword, "updateProfilePasswordConfirmationBaseTask.incorrectPassword");
        }

        //If we got here all is good - go ahead and set the secret for the user
        userService.enableTwoFactorSecretForCurrentUser(enableTwoFactorAuthInput.getSecret());

        //Generate and return the token
        return renewTwoFactorAuthForCurrentJWT(currentJWTString, enableTwoFactorAuthInput.getTwoFactorAuthCode(), enableTwoFactorAuthInput.isRememberMe());
    }

    /**
     *  Renew two factor authentication from a user's current JWT by generating a new JWT {@link TokenDTO} if
     *  validation of the provided verification code is successful.
     *
     * @param currentJWTString The user's current JWT
     * @param verificationCode 2FA verification code to use for validation
     * @param rememberMe true if the new JWT should use 2FA remember me
     * @return String representing the new JWT
     */
    public TokenDTO renewTwoFactorAuthForCurrentJWT(String currentJWTString, int verificationCode, boolean rememberMe) {
        if (StringUtils.isEmpty(currentJWTString)) {
            throw new AuthZoneLoginRequired();
        }

        Jws<Claims> jws;
        Pair<User, NarrativeAuthenticationToken> userTokenPair;

        try {
            //Parse the current JWT
            jws = jwtUtil.parseAndValidateJWTFromString(currentJWTString);

            //Extract the auth token for the user specified by the JWT - don't validate the 2FA expiry since we will
            //be setting a new one if successful
            userTokenPair = jwtUtil.resolveAuthTokenAndUserFromJws(null, jws, false);
        } catch (Exception e) {
            throw new JwtTokenInvalidException(e);
        }

        //Set the current primary role - may be needed for downstream operations
        staticMethodWrapper.getCurrentNetworkContextImplBase().setPrimaryRole(userTokenPair.getLeft());

        //Validate the new 2FA code against the principal found in the JWT
        validateTwoFactorCodeForUser(userTokenPair.getLeft(), verificationCode);

        //Generate the new JWT string
        String newJWTString = buildRenewedTFAExpiryJWTStringFromCurrentJWT(jws, userTokenPair.getRight(), rememberMe, userTokenPair.getLeft().getTwoFactorAuthenticationSecretKey());
        return jwtUtil.buildTokenDTO(newJWTString, false);
    }

    /**
     * Build a new JWT String based on an auth token and the current user's claims that has a renewed 2FA expiry.
     *
     * @param jws             Claims for this JWT
     * @param authToken       The auth token to operate on
     * @param rememberMe      true if new JWT should honor 2FA remember me
     * @param twoFactorSecret The two factor secret to use for this JWT
     * @return String representing the new JWT
     */
    @VisibleForTesting
    String buildRenewedTFAExpiryJWTStringFromCurrentJWT(Jws<Claims> jws, NarrativeAuthenticationToken authToken, boolean rememberMe, String twoFactorSecret) {
        Map<String, Object> claimMap = new HashMap<>();

        //Add the existing authorites to the claim map
        claimMap.put(AUTHORITIES_CLAIM, authToken.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

        //Add the two factor expiry to the claims with a new expiry value
        Duration expirationDuration = rememberMe ? narrativeProperties.getSecurity().getTwoFactorRememberMeExpiration() : narrativeProperties.getSecurity().getTwoFactorExpiration();
        Instant expiryInstant = Instant.now().plus(expirationDuration);
        claimMap.put(JwtUtil.TWO_FACTOR_AUTH_EXPIRY_CLAIM, Date.from(expiryInstant));

        //Hash the 2FA secret and add as a claim so we can verify that the secret in the token matches the user's current secret
        String hashedPassword = BCrypt.hashpw(twoFactorSecret, BCrypt.gensalt());
        claimMap.put(JwtUtil.TWO_FACTOR_AUTH_SECRET_HASH_CLAIM, hashedPassword);

        //Build a clone of the token passed in the header but update the two factor expiry
        return jwtUtil.buildJWTToken(
                //Id
                jws.getBody().getId(),
                //Subject
                jws.getBody().getSubject(),
                //Issued at date
                new Date(System.currentTimeMillis()),
                //Expiration date
                jws.getBody().getExpiration(),
                //Additional claims
                claimMap
        );
    }

    /**
     * Test the two factor authentication code for the current user
     *
     * @param user The user to use when evaluating the 2FA code
     * @param code The 2FA code
     */
    @VisibleForTesting
    void validateTwoFactorCodeForUser(User user, int code) {
        RuntimeException exception = taskExecutor.executeAreaTask(new AreaTaskImpl<RuntimeException>() {
            @Override
            protected RuntimeException doMonitoredTask() {
                if (!user.isTwoFactorAuthenticationEnabled()) {
                    return new BadCredentialsException(messageSource.getMessage("AuthenticationFilter.error.twoFactor.notEnabledForUser"));
                }

                // jw: first, let's see if this code matches the users currently generated code
                if (isTwoFactorCodeValid(user.getTwoFactorAuthenticationSecretKey(), code)) {
                    return null;
                }

                // jw: if the code didn't match what is currently being generated, then check their backup codes.
                if (taskExecutor.executeAreaTask(new CheckTwoFactorAuthenticationBackupCodeTask(user, code))) {
                    return null;
                }

                throw validationExceptionFactory.forInvalidFieldError("2FA code validation error", TwoFactoryVerifyInputDTO.Fields.verificationCode);
            }
        });

        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Disable two factor authentication for the current user
     * @param disableTwoFactorAuthInput Contains password for the current user and verificationCode for the current user
     */
    @Override
    public void disableTwoFactorAuthForCurrentUser(DisableTwoFactorAuthInput disableTwoFactorAuthInput) {
        userService.disableTwoFactorSecretForCurrentUser(disableTwoFactorAuthInput);
    }

    /**
     * Is two factor authentication enabled for the current user?
     *
     * This method is idempotent since it essentially removes the 2FA secret for the current user.
     */
    @Override
    public boolean isTwoFactorAuthEnabledForCurrentUser() {
        return userService.isTwoFactorAuthEnabledForCurrentUser();
    }


    /**
     * Build an image representing the passed secret and current user context to be used for code validation.
     *
     * For this implementation, the result will be a Google Authenticator QR code.
     *
     * @param secret    Secret to be used for generating the QR code
     * @param imageType The image type to generate
     * @return {@link byte[]} containing the QR code image
     */
    @Override
    public byte[] buildQRImageForCurrentUser(String secret, ImageType imageType) {
        return taskExecutor.executeAreaTask(new AreaTaskImpl<byte[]>(false) {
            @Override
            protected byte[] doMonitoredTask() {
                //Must be a registered user
                staticMethodWrapper.networkContext().getPrimaryRole().checkRegisteredUser();

                ByteArrayOutputStream bos = QRCode.from(TwoFactorAuthUtils.getOtpAuthTotpURL(getNetworkContext().getUser(), secret))
                        .to(imageType)
                        .withSize(QR_CODE_WIDTH, QR_CODE_HEIGHT)
                        .stream();
                return bos.toByteArray();
            }
        });
    }
}
