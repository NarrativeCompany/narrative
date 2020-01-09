package org.narrative.network.customizations.narrative.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.narrative.common.persistence.OID;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.controller.postbody.redemption.RequestRedemptionInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.DeleteUserInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.DeleteUserNeoWalletInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.DisableTwoFactorAuthInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.EnableTwoFactorAuthInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.RecoverPasswordInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.RegisterUserInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.RegisterUserStepOneInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.ResetPasswordInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.SuspendEmailInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.UpdateEmailAddressInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.UpdatePasswordInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.UpdateUserNeoWalletInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.UpdateUserProfileInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.UserNotificationSettingsInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.UserPersonalSettingsInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.UserRenewTosAgreementInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.VerifyEmailAddressInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.VerifyPendingEmailAddressInputDTO;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.interceptors.BypassUserDisabledCheck;
import org.narrative.network.customizations.narrative.interceptors.BypassUserEmailVerificationCheck;
import org.narrative.network.customizations.narrative.interceptors.UserStatusInterceptor;
import org.narrative.network.customizations.narrative.service.api.KycService;
import org.narrative.network.customizations.narrative.service.api.RedemptionService;
import org.narrative.network.customizations.narrative.service.api.TwoFactorAuthenticationService;
import org.narrative.network.customizations.narrative.service.api.UserService;
import org.narrative.network.customizations.narrative.service.api.model.CurrentUserDTO;
import org.narrative.network.customizations.narrative.service.api.model.CurrentUserFollowedItemDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowScrollParamsDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedNichesDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedPublicationsDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedUsersDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheUserAssociationDTO;
import org.narrative.network.customizations.narrative.service.api.model.PWResetURLValidationResultDTO;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.PermittedAgeRatingDTO;
import org.narrative.network.customizations.narrative.service.api.model.PostDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationUserAssociationDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardPeriodDTO;
import org.narrative.network.customizations.narrative.service.api.model.SuspendEmailValidationDTO;
import org.narrative.network.customizations.narrative.service.api.model.TokenDTO;
import org.narrative.network.customizations.narrative.service.api.model.TwoFactorSecretDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserEmailAddressDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserFollowersDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserKycDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserNeoWalletDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserNotificationSettingsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserOwnedChannelsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserPersonalSettingsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserReferralDetailsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserRewardPeriodStatsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserRewardTransactionDTO;
import org.narrative.network.customizations.narrative.service.api.model.VerifyEmailAddressResultDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.KycApplicantInput;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.impl.common.ValidationExceptionFactory;
import org.narrative.network.customizations.narrative.service.mapper.CurrentUserMapper;
import org.narrative.network.customizations.narrative.service.mapper.UserMapper;
import org.narrative.network.customizations.narrative.services.RestUploadUtils;
import org.narrative.network.customizations.narrative.util.FollowedItemScrollable;
import lombok.SneakyThrows;
import net.glxn.qrgen.core.image.ImageType;
import org.apache.commons.codec.binary.Base64;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.narrative.network.core.security.jwt.JwtUtil.*;
import static org.narrative.network.customizations.narrative.service.api.KycService.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 8/27/18
 * Time: 9:06 AM
 *
 * @author jonmark
 */
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    public static final String USER_ID_PARAM = "userId";
    public static final String USER_OID_PARAM = "userOid";
    public static final String REDEMPTION_OID_PARAM = "redemptionOid";
    public static final String USERNAME_PARAM = "username";
    public static final String EMAIL_ADDRESS_FIELD_NAME = "emailAddress";
    protected static final ImageType QR_IMAGE_TYPE = ImageType.JPG;
    private static final String IMAGE_FORMAT_STRING = "data:image/%s;base64,%s";
    private static final String CURRENT_USER_PATH = "/current";
    private static final String KYC_PATH = CURRENT_USER_PATH + "/kyc";

    private final UserService userService;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;
    private final RedemptionService redemptionService;
    private final UserMapper userMapper;
    private final CurrentUserMapper currentUserMapper;
    private final AcceptHeaderLocaleResolver acceptHeaderLocaleResolver;
    private final NarrativeProperties narrativeProperties;
    private final KycService kycService;
    private final StaticMethodWrapper staticMethodWrapper;
    private final ValidationExceptionFactory validationExceptionFactory;

    public UserController(UserService userService, TwoFactorAuthenticationService twoFactorAuthenticationService, RedemptionService redemptionService, UserMapper userMapper, CurrentUserMapper currentUserMapper, AcceptHeaderLocaleResolver acceptHeaderLocaleResolver, NarrativeProperties narrativeProperties, KycService kycService, StaticMethodWrapper staticMethodWrapper, ValidationExceptionFactory validationExceptionFactory) {
        this.userService = userService;
        this.twoFactorAuthenticationService = twoFactorAuthenticationService;
        this.redemptionService = redemptionService;
        this.userMapper = userMapper;
        this.currentUserMapper = currentUserMapper;
        this.acceptHeaderLocaleResolver = acceptHeaderLocaleResolver;
        this.narrativeProperties = narrativeProperties;
        this.kycService = kycService;
        this.staticMethodWrapper = staticMethodWrapper;
        this.validationExceptionFactory = validationExceptionFactory;
    }

    @PostMapping
    public UserDTO registerUser(@Valid @RequestBody RegisterUserInputDTO registerInput) {
        return userService.registerUser(registerInput);
    }

    @PostMapping ("validate-registering-user")
    public ScalarResultDTO<String> validateRegisteringUser(@Valid @RequestBody RegisterUserStepOneInputDTO stepOneInput) {
        // Primarily just validating the input DTO so we don't need to return anything
        // @Valid will return a ValidationError if there are ValidationErrors

        // bl: also need to validate the email address, username, and reCAPTCHA
        return userService.validateRegisterUserStepOne(stepOneInput);
    }

    @GetMapping(path = "/{" + USER_OID_PARAM + "}/niche-associations")
    public List<NicheUserAssociationDTO> getNicheAssociations(@PathVariable(USER_OID_PARAM) OID userOid) {
        User user = User.dao().getForApiParam(userOid, USER_OID_PARAM);

        List<NicheUserAssociationDTO> associations = userService.getNicheAssociationsForUser(user);
        if (associations == null) {
            return Collections.emptyList();
        }

        return associations;
    }

    @GetMapping(path = "/{" + USER_OID_PARAM + "}/publication-associations")
    public List<PublicationUserAssociationDTO> getPublicationAssociations(@PathVariable(USER_OID_PARAM) OID userOid) {
        User user = User.dao().getForApiParam(userOid, USER_OID_PARAM);

        return userService.getPublicationAssociationsForUser(user);
    }

    @GetMapping(path = "/{" + USER_OID_PARAM + "}/referral-details")
    public UserReferralDetailsDTO getReferralDetails(@PathVariable(USER_OID_PARAM) OID userOid) throws UserPrincipalNotFoundException {
        return userService.getUserReferralDetails(userOid);
    }

    @GetMapping(path = "/{" + USER_ID_PARAM + "}/detail")
    public UserDetailDTO getUserProfile(@PathVariable(USER_ID_PARAM) String userId) throws UserPrincipalNotFoundException {
        return userService.getUserProfile(userId);
    }

    /**
     * save a new avatar for the user
     *
     * @param file the new avatar to use
     * @return the {@link UserDTO} with the user's updated profile, including new avatar URL
     */
    @PostMapping(path = CURRENT_USER_PATH + "/avatar")
    public UserDTO uploadUserAvatar(@RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(file);
    }

    @DeleteMapping(path = CURRENT_USER_PATH + "/avatar")
    public UserDTO deleteUserAvatar() {
        return userService.deleteAvatar();
    }

    /**
     * Find user notification settings for the current user.
     *
     * @return {@link Optional} of {@link UserNotificationSettingsDTO}
     */
    @GetMapping(path = CURRENT_USER_PATH + "/notification-settings")
    public UserNotificationSettingsDTO findUserNotificationSettings() {
        return userService.findUserNotificationSettings();
    }

    /**
     * Update user notification preferences for the current user.
     *
     * @param settingsReq The settings to apply for the user of interest
     * @return {@link UserNotificationSettingsDTO} reflecting the applied settings
     */
    @PutMapping(path = CURRENT_USER_PATH + "/notification-settings")
    public UserNotificationSettingsDTO updateUserNotificationSettings(@Valid @RequestBody UserNotificationSettingsInputDTO settingsReq) {
        return userService.updateUserNotificationSettings(userMapper.mapUserNotificationsSettingsReqToDTO(settingsReq));
    }

    /**
     * Get personal settings for the current user.
     *
     * @return {@link Optional} of {@link UserPersonalSettingsDTO}
     */
    @GetMapping(path = CURRENT_USER_PATH + "/personal-settings")
    public UserPersonalSettingsDTO getUserPersonalSettings() {
        return userService.getPersonalSettingsForCurrentUser();
    }

    /**
     * Update personal settings for the current user.
     *
     * @param settings The settings to apply for the currnet user
     * @return {@link UserPersonalSettingsDTO} reflecting the applied settings
     */
    @PutMapping(path = CURRENT_USER_PATH + "/personal-settings")
    public UserPersonalSettingsDTO updateUserPersonalSettings(@Valid @RequestBody UserPersonalSettingsInputDTO settings) {
        return userService.updatePersonalSettingsForCurrentUser(userMapper.mapUserPersonalSettingsInputToDTO(settings));
    }

    /**
     * Gets current user.
     *
     * @param request the request
     * @return the current user
     */
    @GetMapping(path = CURRENT_USER_PATH)
    public CurrentUserDTO getCurrentUser(HttpServletRequest request) {
        // Bypass our FixedLanguageHeaderLocaleResolver locale resolution and get the locale from the Accept-Language header
        Locale locale = acceptHeaderLocaleResolver.resolveLocale(request);

        return currentUserMapper.mapUserEntityToCurrentUser(userService.getCurrentUser(locale));
    }

    @PutMapping(path = CURRENT_USER_PATH)
    public UserDTO updateProfileForCurrentUser(@Valid @RequestBody UpdateUserProfileInputDTO updateProfileInput) {
        return userService.updateUserProfileForCurrentUser(updateProfileInput);
    }

    /**
     * renew TOS agreement for the current user.
     * marked with {@link BypassUserDisabledCheck} so that {@link UserStatusInterceptor} relaxes the requirement
     * that all signed in users are active. this is necessary in order to re-activate the account.
     *
     * @param renewTosInput the request to renew TOS
     */
    @BypassUserDisabledCheck
    @PutMapping(path = CURRENT_USER_PATH + "/tos-agreement")
    public void renewTosAgreementForCurrentUser(@Valid @RequestBody UserRenewTosAgreementInputDTO renewTosInput) {
        userService.renewTosAgreementForCurrentUser(renewTosInput);
    }

    @PostMapping(path = CURRENT_USER_PATH + "/delete")
    public void deleteCurrentUser(@Valid @RequestBody DeleteUserInputDTO deleteUserInput) {
        userService.deleteCurrentUser(deleteUserInput);
    }

    @GetMapping(path = CURRENT_USER_PATH + "/detail")
    public UserDetailDTO getCurrentUserDetail() {
        return userMapper.mapUserEntityToUserDetail(networkContext().getUser());
    }

    @GetMapping(path = CURRENT_USER_PATH + "/profile-zip")
    public ResponseEntity<Resource> getCurrentUserProfileZip() {
        return userService.getCurrentUserProfileZip();
    }

    /**
     * Get counts of the current user's owned channels.
     *
     * @return {@link UserOwnedChannelsDTO} reflecting the current user's owned channels.
     */
    @GetMapping(path = CURRENT_USER_PATH + "/owned-channels")
    public UserOwnedChannelsDTO getUserOwnedChannels() {
        return userService.getUserOwnedChannels();
    }

    @GetMapping(path = CURRENT_USER_PATH + "/niche-associations")
    public List<NicheUserAssociationDTO> getNicheAssociations() {
        List<NicheUserAssociationDTO> associations = userService.getCurrentUserNicheAssociations();
        if (associations == null) {
            return Collections.emptyList();
        }

        return associations;
    }

    /**
     * Get the current user's email address
     */
    @GetMapping(path = CURRENT_USER_PATH + "/email-address")
    public UserEmailAddressDetailDTO getEmailAddressForCurrentUser() {
        return userService.getEmailAddressDetailForCurrentUser();
    }

    @PutMapping(path = CURRENT_USER_PATH + "/email-address")
    public UserEmailAddressDetailDTO updateEmailAddressForCurrentUser(@Valid @RequestBody UpdateEmailAddressInputDTO updateEmailAddress) {
        return userService.updateEmailAddressForCurrentUser(updateEmailAddress);
    }

    /**
     * verify email address for the specified user.
     * marked with {@link BypassUserEmailVerificationCheck} so that {@link UserStatusInterceptor} relaxes the requirement
     * that all signed in users have a verified email address. this is necessary in order to verify the user's email address.
     *
     * @param userOid            the user OID for this request
     * @param verifyEmailAddress the request to verify the email address
     */
    @BypassUserEmailVerificationCheck
    @PutMapping(path = "/{" + USER_OID_PARAM + "}/email-verification")
    public void verifyEmailAddressForUser(@PathVariable(USER_OID_PARAM) OID userOid, @Valid @RequestBody VerifyEmailAddressInputDTO verifyEmailAddress) {
        User user = User.dao().getForApiParam(userOid, USER_OID_PARAM);
        userService.verifyEmailAddressForUser(user, verifyEmailAddress);
    }

    /**
     * verify pending email address for the specified user.
     * marked with {@link BypassUserEmailVerificationCheck} so that {@link UserStatusInterceptor} relaxes the requirement
     * that all signed in users have a verified email address. this is necessary in order to verify the user's email address.
     *
     * @param userOid            the user OID for this request
     * @param verifyEmailAddress the request to verify the email address
     */
    @PutMapping(path = "/{" + USER_OID_PARAM + "}/pending-email-verification")
    public VerifyEmailAddressResultDTO verifyPendingEmailAddressForUser(@PathVariable(USER_OID_PARAM) OID userOid, @Valid @RequestBody VerifyPendingEmailAddressInputDTO verifyEmailAddress) {
        User user = User.dao().getForApiParam(userOid, USER_OID_PARAM);
        return userService.verifyPendingEmailAddressForUser(user, verifyEmailAddress);
    }

    // jw: while ideally this method would be a delete request, we cannot use the request body for properties on a delete,
    //     so we are forced to use a post request. Modelling this after deleteCurrentUser above, where we post to /users/current/delete
    @BypassUserEmailVerificationCheck
    @PostMapping(path = "/{" + USER_OID_PARAM + "}/pending-email-verification/cancel")
    public ScalarResultDTO<String> cancelPendingEmailAddressChangeForUser(@PathVariable(USER_OID_PARAM) OID userOid, @Valid @RequestBody VerifyPendingEmailAddressInputDTO verifyEmailAddress) {
        User user = User.dao().getForApiParam(userOid, USER_OID_PARAM);
        return userService.cancelPendingEmailAddressChangeForUser(user, verifyEmailAddress);
    }

    /**
     * re-send the verification email for the current user.
     * marked with {@link BypassUserEmailVerificationCheck} so that {@link UserStatusInterceptor} relaxes the requirement
     * that all signed in users have a verified email address. this is necessary in order to re-send the verification email.
     */
    @BypassUserEmailVerificationCheck
    @PostMapping(path = CURRENT_USER_PATH + "/email-verification-email")
    public void resendVerificationEmailForCurrentUser() {
        userService.resendVerificationEmailForCurrentUser();
    }

    @PutMapping(path = CURRENT_USER_PATH + "/password")
    public TokenDTO updatePasswordForCurrentUser(@Valid @RequestBody UpdatePasswordInputDTO updatePassword) {
        return userService.updatePasswordForCurrentUser(updatePassword);
    }

    /**
     * Enable two factor authentication for the current user.  If the passed verification code is successfully
     * validated against the passed secret, then the user will have 2FA enabled with the passed secret.
     *
     * @param jwtString                   The Authorization cookie header value (JWT token) for the incoming request
     * @param enableTwoFactorAuthInputDTO Params required for this operation
     * @return {@link TokenDTO} DTO with the JWT token
     */
    @PostMapping(path = CURRENT_USER_PATH + "/2fa-secret")
    public TokenDTO enableTwoFactorAuthenticationForCurrentUser(@CookieValue(AUTH_COOKIE_KEY) @NotEmpty String jwtString, @RequestBody @Valid EnableTwoFactorAuthInputDTO enableTwoFactorAuthInputDTO) {
        return twoFactorAuthenticationService.enableTwoFactorAuthForCurrentUser(jwtString, enableTwoFactorAuthInputDTO);
    }

    /**
     * Disable two factor authentication for the current user.
     *
     * This URI was changed from DELETE to PUT as Apollo Link Rest does not support a body for DELETE even though it
     * is not forbidden by the HTTP spec.
     */
    @PutMapping(path = CURRENT_USER_PATH + "/2fa-secret")
    public void disableTwoFactorAuthenticationForCurrentUser(@Valid @RequestBody DisableTwoFactorAuthInputDTO disableTwoFactorAuthInputDTO) {
        twoFactorAuthenticationService.disableTwoFactorAuthForCurrentUser(disableTwoFactorAuthInputDTO);
    }

    /**
     * Get the current users NEO Wallet information, which includes address, status, current balance and minimum withdrawal
     * amount if relevant.
     */
    @GetMapping(path = CURRENT_USER_PATH + "/neo-wallet")
    public UserNeoWalletDTO getNeoWalletForCurrentUser() {
        return redemptionService.getWalletNeoAddressForCurrentUser();
    }

    /**
     *
     */
    @PostMapping(path = CURRENT_USER_PATH + "/neo-wallet/redemptions")
    public void postNeoWalletRedemptionForCurrentUser(@Valid @RequestBody RequestRedemptionInputDTO inputDto) {
        redemptionService.requestNeoWalletRedemptionForCurrentUser(inputDto);
    }

    /**
     *
     */
    @DeleteMapping(path = CURRENT_USER_PATH + "/neo-wallet/redemptions/{" + REDEMPTION_OID_PARAM + "}")
    public void deleteNeoWalletRedemptionForCurrentUser(@NotNull @PathVariable(REDEMPTION_OID_PARAM) OID redemptionOid) {
        redemptionService.deleteNeoWalletRedemptionForCurrentUser(redemptionOid);
    }

    /**
     * Set or update the neo address for the current user
     *
     * @param input the data necessary to validate the users access to the account and update the neo address
     */
    @PutMapping(path = CURRENT_USER_PATH + "/neo-wallet")
    public UserNeoWalletDTO updateNeoWalletForCurrentUser(@RequestBody @Valid UpdateUserNeoWalletInputDTO input) {
        return redemptionService.updateWalletNeoAddressForCurrentUser(input);
    }

    /**
     * Delete the neo address for the current user.
     * NOTE: this can't be an HTTP DELETE since our Apollo framework doesn't support bodies for DELETE requests.
     *
     * @param input the account password/2FA validation
     */
    @PostMapping(path = CURRENT_USER_PATH + "/neo-wallet/delete")
    public UserNeoWalletDTO deleteNeoWalletForCurrentUser(@RequestBody @Valid DeleteUserNeoWalletInputDTO input) {
        return redemptionService.deleteWalletNeoAddressForCurrentUser(input);
    }

    /**
     * Generate a new two factor authentication secret and QR code for the current user
     *
     * @return {@link TwoFactorSecretDTO} that contains a secret and a QR code image
     */
    @GetMapping(path = CURRENT_USER_PATH + "/2fa-secret")
    public TwoFactorSecretDTO generateTwoFactorSecretForCurrentUser() {
        String secret = twoFactorAuthenticationService.generateNewSecret();
        byte[] qrByteArr = twoFactorAuthenticationService.buildQRImageForCurrentUser(secret, QR_IMAGE_TYPE);

        //Base 64 encode to a UTF-8 String to embed in the result
        String base64Image = String.format(IMAGE_FORMAT_STRING, QR_IMAGE_TYPE.name().toLowerCase(), Base64.encodeBase64String(qrByteArr));

        List<Integer> backupCodes = twoFactorAuthenticationService.generateBackupCodes(secret);

        return TwoFactorSecretDTO.builder()
                .secret(secret)
                .qrCodeImage(base64Image)
                .backupCodes(backupCodes)
                .build();
    }

    /**
     * Determine whether two factor authentication is enabled for the current user
     *
     * @return true if enabled, false otherwise
     */
    @GetMapping(path = CURRENT_USER_PATH + "/2fa-enabled")
    public ScalarResultDTO<Boolean> isTwoFactorEnabledForCurrentUser() {
        return ScalarResultDTO.<Boolean>builder()
                .value(twoFactorAuthenticationService.isTwoFactorAuthEnabledForCurrentUser())
                .build();
    }

    /**
     * Finds the X most posted to niches by the current user.
     *
     * @return Collection of NicheDTO
     */
    @GetMapping(path = CURRENT_USER_PATH + "/niches-most-posted-to")
    public Collection<NicheDTO> getNichesMostPostedToByCurrentUser(@RequestParam(defaultValue = "5") int count) {
        count = Math.min(count, narrativeProperties.getSpring().getMvc().getMaxPageSize());

        return userService.getNichesMostPostedToByCurrentUser(count);
    }

    /**
     * Fetches a page of posts published by the specified user.
     *
     * @return PageDataDTO of PostDTO
     */
    @GetMapping(path = CURRENT_USER_PATH + "/published-posts")
    public PageDataDTO<PostDTO> getPublishedPostsByCurrentUser(@PageableDefault(size = 50) Pageable pageRequest) {
        return userService.getPublishedPostsByCurrentUser(pageRequest);
    }

    /**
     * Fetches a page of draft posts pending by the current user.
     *
     * @return PageDataDTO of PostDTO
     */
    @GetMapping(path = CURRENT_USER_PATH + "/draft-posts")
    public PageDataDTO<PostDTO> getDraftPostsByCurrentUser(@PageableDefault(size = 50) Pageable pageRequest) {
        return userService.getDraftPostsByCurrentUser(pageRequest);
    }

    /**
     * Fetches a page of posts pending Publication approval by the current user.
     *
     * @return PageDataDTO of PostDTO
     */
    @GetMapping(path = CURRENT_USER_PATH + "/pending-posts")
    public PageDataDTO<PostDTO> getPendingPostsByCurrentUser(@PageableDefault(size = 50) Pageable pageRequest) {
        return userService.getPendingPostsByCurrentUser(pageRequest);
    }

    /**
     * Get the permitted age ratings for the current user.
     *
     * @return {@link PermittedAgeRatingDTO} containing a {@link Set} of {@link AgeRating} permitted for the current user
     */
    @GetMapping(path = CURRENT_USER_PATH + "/permitted-age-rating")
    public PermittedAgeRatingDTO getPermittedAgeRatingsForCurrentUser() {
        Set<AgeRating> ageRatingSet = userService.getPermittedAgeRatingsForCurrentUser();
        return PermittedAgeRatingDTO.builder().permittedAgeRatings(ageRatingSet).build();
    }

    /**
     * Get the current user's account balance.
     *
     * @return {@link NrveUsdValue} with the current user's account balance
     */
    @GetMapping(path = CURRENT_USER_PATH + "/rewards-balance")
    public ScalarResultDTO<NrveUsdValue> getCurrentUserRewardsBalance() {
        return userService.getCurrentUserRewardsBalance();
    }

    @GetMapping(path = "/{" + USER_OID_PARAM + "}/reward-periods")
    public List<RewardPeriodDTO> getUserRewardPeriods(@NotNull @PathVariable(USER_OID_PARAM) OID userOid) {
        return userService.getUserRewardPeriods(userOid);
    }

    @GetMapping("/{" + USER_OID_PARAM + "}/period-rewards")
    public UserRewardPeriodStatsDTO getUserRewardPeriodRewards(
            @NotNull @PathVariable(USER_OID_PARAM) OID userOid,
            @RequestParam(name=RewardsController.MONTH_PARAM) String yearMonthStr) {
        return userService.getUserRewardPeriodRewards(userOid, yearMonthStr);
    }

    @GetMapping("/{" + USER_OID_PARAM + "}/reward-transactions")
    public PageDataDTO<UserRewardTransactionDTO> getUserRewardTransactions(
            @NotNull @PathVariable(USER_OID_PARAM) OID userOid,
            @PageableDefault(size = 20) Pageable pageRequest) {
        return userService.getUserRewardTransactions(userOid, pageRequest);
    }

    /**
     * Rest API endpoint for sending lost password email.
     *
     * @param recoverPasswordInput The input for password recovery.
     */
    @PostMapping(path = "/lost-password-email")
    public void recoverPassword(@Valid @RequestBody RecoverPasswordInputDTO recoverPasswordInput) {
        userService.recoverPassword(recoverPasswordInput);
    }

    @GetMapping(path = "/{" + USER_OID_PARAM + "}/reset-password")
    public PWResetURLValidationResultDTO isValidResetURLParams(@PathVariable(USER_OID_PARAM) OID userOid,
                                                               @RequestParam(ResetPasswordInputDTO.Fields.timestamp) long timestamp,
                                                               @RequestParam(ResetPasswordInputDTO.Fields.resetPasswordKey) String key) {
        return userService.validateResetURLParams(userOid, timestamp, key);
    }

    @PostMapping(path = "/{" + USER_OID_PARAM + "}/reset-password")
    public void resetPassword(@PathVariable(USER_OID_PARAM) OID userOid, @Valid @RequestBody ResetPasswordInputDTO resetPasswordInput) {
        userService.resetPassword(userOid, resetPasswordInput);
    }

    @GetMapping(path = "/{" + USER_OID_PARAM + "}/suspend-email-preference")
    public SuspendEmailValidationDTO validateSuspendEmailPreference(@PathVariable(USER_OID_PARAM) OID userOid, @Valid SuspendEmailInputDTO suspendEmailInput) {
        User user = User.dao().getForApiParam(userOid, USER_OID_PARAM);
        return userService.validateSuspendEmailPreference(user, suspendEmailInput);
    }

    @PutMapping(path = "/{" + USER_OID_PARAM + "}/suspend-email-preference")
    public void setSuspendEmailPreference(@PathVariable(USER_OID_PARAM) OID userOid, @Valid @RequestBody SuspendEmailInputDTO suspendEmailInput) {
        User user = User.dao().getForApiParam(userOid, USER_OID_PARAM);
        userService.setSuspendEmailPreference(user, suspendEmailInput);
    }

    /**
     * Get a user's current KYC state
     */
    @GetMapping(path = KYC_PATH + "/status")
    public UserKycDTO getKycStateForCurrentUser() {
        staticMethodWrapper.networkContext().getPrimaryRole().checkRegisteredUser();
         return kycService.getKycStateForUser(staticMethodWrapper.networkContext().getUser().getOid());
    }

    /**
     * Submit a KYC verification request
     *
     * @param kycApplicantInput the applicant input object
     * @param livePhotoImage Live photo image
     * @param docFrontImage Document FRONT image
     * @param docBackImage Document BACK image (optional depending on document type)
     *
     */
    @PostMapping(path = KYC_PATH + "/verification")
    public UserKycDTO submitKYCApplicant(@RequestParam("kycApplicantInput") @NotNull KycApplicantInput kycApplicantInput, @RequestParam(LIVE_PHOTO_IMAGE) @NotNull MultipartFile livePhotoImage, @RequestParam(DOC_FRONT_IMAGE) @NotNull MultipartFile docFrontImage, @RequestParam(value = DOC_BACK_IMAGE, required = false) MultipartFile docBackImage) throws IOException {
        // Validate the expected number of multi part files has been uploaded
        if (kycApplicantInput.getKycIdentificationType().isRequiresBackImage() && docBackImage == null) {
            throw validationExceptionFactory.forInvalidFieldError("KYC request for document type " + kycApplicantInput.getKycIdentificationType().name() + " must contain a document back image", DOC_BACK_IMAGE);
        }

        // Write multipart files to temp files for the duration of the request and then clean up
        File livePhotoImageFile = getUploadedFile(livePhotoImage);
        File docFrontImageFile = getUploadedFile(docFrontImage);
        File docBackImageFile = docBackImage!=null ? getUploadedFile(docBackImage) : null;

        return kycService.submitKycApplicant(staticMethodWrapper.networkContext().getUser(), kycApplicantInput.getKycIdentificationType(), livePhotoImageFile, docFrontImageFile, docBackImageFile);
    }

    @VisibleForTesting
    File getUploadedFile(MultipartFile multipartFile) {
        return RestUploadUtils.getUploadedFile(multipartFile, true);
    }

    /**
     * Get users followed by a user.
     *
     * @param userOid The user who is following other users.
     * @return {@link FollowedUsersDTO} containing users who are followed by this user.
     */
    @GetMapping("/{" + USER_OID_PARAM + "}/follows/users")
    public FollowedUsersDTO getUsersFollowedByUser(
            @NotNull @PathVariable(USER_OID_PARAM) OID userOid,
            @Valid FollowedItemScrollable scrollable,
            FollowScrollParamsDTO scrollParams
    ) {
        if(scrollParams!=null) {
            scrollable.setScrollParams(scrollParams);
        }

        return userService.getUsersFollowedByUser(userOid, scrollable);
    }

    /**
     * Get Niches followed by a user.
     *
     * @param userOid The user who is following Niches.
     * @return {@link FollowedNichesDTO} containing Niches who are followed by this user.
     */
    @GetMapping("/{" + USER_OID_PARAM + "}/follows/niches")
    public FollowedNichesDTO getNichesFollowedByUser(
            @NotNull @PathVariable(USER_OID_PARAM) OID userOid,
            @Valid FollowedItemScrollable scrollable,
            FollowScrollParamsDTO scrollParams
    ) {
        if(scrollParams!=null) {
            scrollable.setScrollParams(scrollParams);
        }

        return userService.getNichesFollowedByUser(userOid, scrollable);
    }

    /**
     * Get Publications followed by a user.
     *
     * @param userOid The user who is following Publications.
     * @return {@link FollowedPublicationsDTO} containing Publications that are followed by this user.
     */
    @GetMapping("/{" + USER_OID_PARAM + "}/follows/publications")
    public FollowedPublicationsDTO getPublicationsFollowedByUser(
            @NotNull @PathVariable(USER_OID_PARAM) OID userOid,
            @Valid FollowedItemScrollable scrollable,
            FollowScrollParamsDTO scrollParams
    ) {
        if(scrollParams!=null) {
            scrollable.setScrollParams(scrollParams);
        }

        return userService.getPublicationsFollowedByUser(userOid, scrollable);
    }

    /**
     * Get users following a user.
     *
     * @param userOid The user who is being followed.
     * @return {@link UserFollowersDTO} containing users who are following this user.
     */
    @GetMapping("/{" + USER_OID_PARAM + "}/followers")
    public UserFollowersDTO getUsersFollowingUser(
            @NotNull @PathVariable(USER_OID_PARAM) OID userOid,
            @Valid FollowedItemScrollable scrollable,
            FollowScrollParamsDTO scrollParams
    ) {
        if(scrollParams!=null) {
            scrollable.setScrollParams(scrollParams);
        }

        return userService.getUsersFollowingUser(userOid, scrollable);
    }

    /**
     * Get current user following status.
     *
     * @param userOid The user to get follow status for the current user.
     * @return {@link CurrentUserFollowedItemDTO} representing the current user's follow of the specified user.
     */
    @GetMapping("/{" + USER_OID_PARAM + "}/followers/current")
    public CurrentUserFollowedItemDTO getCurrentUserFollowingUser(@NotNull @PathVariable(USER_OID_PARAM) OID userOid) {
        return userService.getUserFollowStatus(userOid);
    }

    /**
     * Start following a user.
     *
     * @param userOid The user to start following.
     * @return {@link CurrentUserFollowedItemDTO} representing the updated user follow.
     */
    @PostMapping("/{" + USER_OID_PARAM + "}/followers")
    public CurrentUserFollowedItemDTO startFollowingUser(@NotNull @PathVariable(USER_OID_PARAM) OID userOid) {
        return userService.followUser(userOid);
    }

    /**
     * Stop following a user.
     *
     * @param userOid The user to stop following.
     * @return {@link CurrentUserFollowedItemDTO} representing the updated user follow.
     */
    @DeleteMapping("/{" + USER_OID_PARAM + "}/followers")
    public CurrentUserFollowedItemDTO stopFollowingUser(@NotNull @PathVariable(USER_OID_PARAM) OID userOid) {
        return userService.stopFollowingUser(userOid);
    }

    /**
     * Converter to deserialize a JSON String into a KycApplicantInput - multipart uploads with Spring controllers do not support automagic
     * mapping of a JSON part for some reason.
     */
    @Component
    public static class StringToKYCApplicantInputConverter implements Converter<String, KycApplicantInput> {
        private final ObjectMapper objectMapper;

        public StringToKYCApplicantInputConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        @SneakyThrows
        public KycApplicantInput convert(String source) {
            return objectMapper.readValue(source, KycApplicantInput.class);
        }
    }
}
