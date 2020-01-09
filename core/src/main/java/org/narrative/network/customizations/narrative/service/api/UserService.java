package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.api.model.CurrentUserFollowedItemDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedNichesDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedPublicationsDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedUsersDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheUserAssociationDTO;
import org.narrative.network.customizations.narrative.service.api.model.PWResetURLValidationResultDTO;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.PostDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationUserAssociationDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardPeriodDTO;
import org.narrative.network.customizations.narrative.service.api.model.SuspendEmailValidationDTO;
import org.narrative.network.customizations.narrative.service.api.model.TokenDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserEmailAddressDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserFollowersDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserNotificationSettingsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserOwnedChannelsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserPersonalSettingsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserReferralDetailsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserRewardPeriodStatsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserRewardTransactionDTO;
import org.narrative.network.customizations.narrative.service.api.model.VerifyEmailAddressResultDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.DeleteUserInput;
import org.narrative.network.customizations.narrative.service.api.model.input.DisableTwoFactorAuthInput;
import org.narrative.network.customizations.narrative.service.api.model.input.RecoverPasswordInput;
import org.narrative.network.customizations.narrative.service.api.model.input.RegisterUserInput;
import org.narrative.network.customizations.narrative.service.api.model.input.RegisterUserStepOneInput;
import org.narrative.network.customizations.narrative.service.api.model.input.ResetPasswordInput;
import org.narrative.network.customizations.narrative.service.api.model.input.SuspendEmailInput;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateEmailAddressInput;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdatePasswordInput;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateUserProfileInput;
import org.narrative.network.customizations.narrative.service.api.model.input.UserRenewTosAgreementInput;
import org.narrative.network.customizations.narrative.service.api.model.input.VerifyEmailAddressInput;
import org.narrative.network.customizations.narrative.service.api.model.input.VerifyPendingEmailAddressInput;
import org.narrative.network.customizations.narrative.util.FollowedItemScrollable;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * The interface User service.
 */
public interface UserService {
    /**
     * Retrieve {@link User} by {@link OID}.
     *
     * @param oid Unique user oid.
     * @return {@link User} based on {@link OID}.
     * @throws UserPrincipalNotFoundException when OID does not exist.
     */
    User getUser(OID oid) throws UserPrincipalNotFoundException;

    /**
     * Gets current user.
     *
     * @param locale the locale to set for the current user
     * @return the current user
     */
    User getCurrentUser(Locale locale);

    /**
     * Retrieve {@link User} by username.
     *
     * @param username Unique username.
     * @return {@link User} based on username.
     * @throws UserPrincipalNotFoundException when username does not exist.
     */
    User getUserByUsername(String username) throws UserPrincipalNotFoundException;

    /**
     * Register a new user.
     *
     * @param registerInput the registration details
     * @return {@link UserDTO} the newly registered user.
     */
    UserDTO registerUser(RegisterUserInput registerInput);

    /**
     * Validate step one when registering a new user.
     *
     * @param stepOneInput the registration details from step one
     * @return {@link ScalarResultDTO} the recaptchaToken value to be included on step two of registration.
     */
    ScalarResultDTO<String> validateRegisterUserStepOne(RegisterUserStepOneInput stepOneInput);

    /**
     * Get all associations to {@link Niche} for the current {@link User}.
     *
     * @return {@link List} of {@link NicheUserAssociationDTO} for {@link User}
     */
    List<NicheUserAssociationDTO> getCurrentUserNicheAssociations();

    /**
     * Get all associations to {@link Niche} for the specified {@link User}.
     *
     * @param user The user to get associations for.
     * @return {@link List} of {@link NicheUserAssociationDTO} for {@link User}
     */
    List<NicheUserAssociationDTO> getNicheAssociationsForUser(User user);

    /**
     * Get all associations to {@link Publication} for the specified {@link User}.
     *
     * @param user The user to get associations for.
     * @return {@link List} of {@link PublicationUserAssociationDTO} for {@link User}
     */
    List<PublicationUserAssociationDTO> getPublicationAssociationsForUser(User user);

    /**
     * Retrieve {@link UserDetailDTO} by oid.
     *
     * @param userId Unique user oid. Prefixed with "id_" if using the Users username.
     * @return {@link UserDetailDTO} based on oid.
     * @throws UserPrincipalNotFoundException when user oid does not exist.
     */
    UserDetailDTO getUserProfile(String userId) throws UserPrincipalNotFoundException;

    /**
     * get the current user's profile data as a ZIP file
     *
     * @return the {@link ResponseEntity} for the ZIP file to send to the user
     */
    ResponseEntity<Resource> getCurrentUserProfileZip();

    /**
     * Retrieve {@link UserReferralDetailsDTO} by oid.
     *
     * @param userOid Unique user oid.
     * @return {@link UserReferralDetailsDTO} based on oid.
     * @throws UserPrincipalNotFoundException when user oid does not exist.
     */
    UserReferralDetailsDTO getUserReferralDetails(OID userOid) throws UserPrincipalNotFoundException;

    /**
     * Retrieve {@link UserDetailDTO} by username.
     *
     * @param username Unique username.
     * @return {@link UserDetailDTO} based on oid.
     * @throws UserPrincipalNotFoundException when user oid does not exist.
     */
    UserDetailDTO getUserProfileByUsername(String username) throws UserPrincipalNotFoundException;

    /**
     * Find notification settings for a user by OID.
     *
     * @return {@link Optional} of {@link UserNotificationSettingsDTO} if found
     */
    UserNotificationSettingsDTO findUserNotificationSettings();

    /**
     * Update notification settings for a user.
     *
     * @param pendingNotificationSettings The pending notification settings to apply
     * @return {@link UserNotificationSettingsDTO} as applied
     */
    UserNotificationSettingsDTO updateUserNotificationSettings(UserNotificationSettingsDTO pendingNotificationSettings);

    /**
     * Get personal settings for the current user
     *
     * @return {@link Optional} of {@link UserNotificationSettingsDTO} if found
     */
    UserPersonalSettingsDTO getPersonalSettingsForCurrentUser();

    /**
     * Update personal settings for the current user.
     *
     * @param input The personal settings to apply
     */
    UserPersonalSettingsDTO updatePersonalSettingsForCurrentUser(UserPersonalSettingsDTO input);

    /**
     * Upload a new avatar for a {@link User} and return a {@link UserDTO} with the updated user data.
     *
     * @param file the file
     * @return {@link UserDTO} with the user's updated data
     */
    UserDTO uploadAvatar(MultipartFile file);

    /**
     * Delete the avatar for a {@link User} and return a {@link UserDTO} with the updated user data.
     *
     * @return {@link UserDTO} with the user's updated data
     */
    UserDTO deleteAvatar();

    /**
     * Get counts of the current user's owned channels.
     *
     * @return {@link UserOwnedChannelsDTO} for the current user
     */
    UserOwnedChannelsDTO getUserOwnedChannels();

    /**
     * Enable two factor authentication for the current user by setting a secret.
     *
     * This operation will fail for a user that already has 2FA enabled - they must disable 2FA first.
     *
     * @param secret The new secret for this user
     */
    void enableTwoFactorSecretForCurrentUser(String secret);

    /**
     * Disable two factor authentication for the current user by removing the secret.
     *
     * @param disableTwoFactorAuthInput Contains password for the current user and verificationCode for the current user
     */
    void disableTwoFactorSecretForCurrentUser(DisableTwoFactorAuthInput disableTwoFactorAuthInput);

    /**
     * Is two factor authentication enabled for the current user?
     *
     * This method is idempotent since it essentially removes the 2FA secret for the current user.
     *
     * @return the boolean
     */
    boolean isTwoFactorAuthEnabledForCurrentUser();

    /**
     * Recover password
     *
     * @param recoverPasswordInput Password recovery input.
     */
    void recoverPassword(RecoverPasswordInput recoverPasswordInput);

    /**
     * Validate password reset request URL parameters.
     *
     * @param userOid   User oid provided from reset URL
     * @param timestamp Timestamp provided from reset URL
     * @param key       Key provided from reset URL
     * @return true if the params are valid, false otherwise
     */
    PWResetURLValidationResultDTO validateResetURLParams(OID userOid, Long timestamp, String key);

    /**
     * Reset password
     *
     * @param userOid User's oid.
     * @param resetPasswordInput Additional password request parameters
     */
    void resetPassword(OID userOid, ResetPasswordInput resetPasswordInput);

    /**
     * Update the current user's profile.
     *
     * @param updateProfileInput the updated profile information
     * @return the user dto
     */
    UserDTO updateUserProfileForCurrentUser(UpdateUserProfileInput updateProfileInput);

    /**
     * Get the current user's email address details
     *
     * @return The email address details for the current user
     */
    UserEmailAddressDetailDTO getEmailAddressDetailForCurrentUser();

    /**
     * Update the current user's email address
     *
     * @param updateEmailAddress the update email address request
     * @return The updated email address details for the current user
     */
    UserEmailAddressDetailDTO updateEmailAddressForCurrentUser(UpdateEmailAddressInput updateEmailAddress);

    /**
     * Verify the current user's email address
     *  @param user User of interest
     * @param verifyEmailAddress the verify email address request
     */
    void verifyEmailAddressForUser(User user, VerifyEmailAddressInput verifyEmailAddress);

    /**
     * Verify the specified email address for the specified user.
     *  @param user User of interest
     * @param input the verify email address details
     */
    VerifyEmailAddressResultDTO verifyPendingEmailAddressForUser(User user, VerifyPendingEmailAddressInput input);

    /**
     * Cancels pending email address change request by user.
     * @param user The user who is in the process of changing their email address
     * @param input The verification details to validate that this is a valid request.
     * @return The email address that the user was going to change to before cancelation
     */
    ScalarResultDTO<String> cancelPendingEmailAddressChangeForUser(User user, VerifyPendingEmailAddressInput input);

    /**
     * Resend the verification email for the current user
     */
    void resendVerificationEmailForCurrentUser();

    /**
     * Update the current user's password
     *
     * @param updatePassword the update password request
     * @return an updated {@link TokenDTO} for the user
     */
    TokenDTO updatePasswordForCurrentUser(UpdatePasswordInput updatePassword);

    /**
     * Delete the current user's account
     *
     * @param deleteUserInput the delete user request
     */
    void deleteCurrentUser(DeleteUserInput deleteUserInput);

    /**
     * Renew the current user's agreement to the TOS
     *
     * @param renewTosInput the renew TOS agreement request
     */
    void renewTosAgreementForCurrentUser(UserRenewTosAgreementInput renewTosInput);

    /**
     * Update the format preferences for the current user
     *
     * @param user the user
     * @param formatPreferences the new format preferences
     */
    void updateFormatPreferencesForUser(User user, FormatPreferences formatPreferences);

    /**
     * Validate the suspend emails request parameters. Returns successfully if the params are valid.
     * Throws an exception if the params are invalid.
     *
     * @param user the user the suspend request is for
     * @param suspendEmailInput the input params used to validate the request
     */
    SuspendEmailValidationDTO validateSuspendEmailPreference(User user, SuspendEmailInput suspendEmailInput);

    /**
     * Enable a user's suspend email preference.
     *
     * @param user the user to suspend emails for
     * @param suspendEmailInput the input params used to validate the request
     */
    void setSuspendEmailPreference(User user, SuspendEmailInput suspendEmailInput);

    /**
     * Returns a list of Niches most posted to by the current user.
     *
     * @return List of niches most posted to by this user.
     * @param count The maximum number of niches to return.
     */
    Collection<NicheDTO> getNichesMostPostedToByCurrentUser(int count);

    /**
     * Returns a page of PostDTO published by the current user.
     *
     * @param pageRequest Paging information for this request
     * @return {@link PageDataDTO} of {@link PostDTO} found
     */
    PageDataDTO<PostDTO> getPublishedPostsByCurrentUser(Pageable pageRequest);

    /**
     * Returns a page of draft PostDTO for the current user.
     * @param pageRequest Paging information for this request
     * @return {@link PageDataDTO} of {@link PostDTO} found
     */
    PageDataDTO<PostDTO> getDraftPostsByCurrentUser(Pageable pageRequest);

    /**
     * Returns a page of pending PostDTO for the current user.
     * @param pageRequest Paging information for this request
     * @return {@link PageDataDTO} of {@link PostDTO} found
     */
    PageDataDTO<PostDTO> getPendingPostsByCurrentUser(Pageable pageRequest);

    /**
     * Returns a {@link Set} of {@link AgeRating} that are permitted for the current user.
     *
     * @return {@link Set} of {@link AgeRating} for the current user
     */
    Set<AgeRating> getPermittedAgeRatingsForCurrentUser();

    /**
     * Get the current user's account balance.
     *
     * @return {@link NrveUsdValue} with the current user's account balance
     */
    ScalarResultDTO<NrveUsdValue> getCurrentUserRewardsBalance();

    /**
     * Get the {@link RewardPeriodDTO} objects that the specified user has received rewards in.
     * @param userOid OID of the user to get reward periods for
     * @return the {@link List} of {@link RewardPeriodDTO} objects for the specified user
     */
    List<RewardPeriodDTO> getUserRewardPeriods(OID userOid);

    /**
     * Get rewards for the given month
     * @param userOid the user to get month rewards for
     * @param yearMonthStr the month to get rewards for. required.
     * @return {@link UserRewardPeriodStatsDTO} containing stats for the User's rewards in the given month
     */
    UserRewardPeriodStatsDTO getUserRewardPeriodRewards(OID userOid, String yearMonthStr);

    /**
     * Returns a page of {@link UserRewardTransactionDTO} for the current user.
     * @param pageRequest Paging information for this request
     * @return {@link PageDataDTO} of {@link UserRewardTransactionDTO}
     */
    PageDataDTO<UserRewardTransactionDTO> getUserRewardTransactions(OID userOid, Pageable pageRequest);

    /**
     * Return a {@link FollowedUsersDTO} of users followed by the specified user.
     *
     * @param userOid OID of the user who is following other users.
     * @param scrollable Load more information for this request
     * @return {@link FollowedUsersDTO} listing users the specified user is following
     */
    FollowedUsersDTO getUsersFollowedByUser(OID userOid, FollowedItemScrollable scrollable);

    /**
     * Return a {@link FollowedNichesDTO} of Niches followed by the specified user.
     *
     * @param userOid OID of the user who is following Niches.
     * @param scrollable Load more information for this request
     * @return {@link FollowedNichesDTO} listing Niches the specified user is following
     */
    FollowedNichesDTO getNichesFollowedByUser(OID userOid, FollowedItemScrollable scrollable);

    /**
     * Return a {@link FollowedPublicationsDTO} of Publications followed by the specified user.
     *
     * @param userOid OID of the user who is following Publications.
     * @param scrollable Load more information for this request
     * @return {@link FollowedPublicationsDTO} listing Publications the specified user is following
     */
    FollowedPublicationsDTO getPublicationsFollowedByUser(OID userOid, FollowedItemScrollable scrollable);

    /**
     * Return a {@link UserFollowersDTO} of users following the specified user.
     *
     * @param userOid OID of the user who is being followed.
     * @param scrollable Load more information for this request
     * @return {@link UserFollowersDTO} listing users following the specified user
     */
    UserFollowersDTO getUsersFollowingUser(OID userOid, FollowedItemScrollable scrollable);

    /**
     * Get current user following status for the specified user
     * @param userOid OID the user to get the following status for the current user
     * @return {@link CurrentUserFollowedItemDTO} representing the user follow for the current user
     */
    CurrentUserFollowedItemDTO getUserFollowStatus(OID userOid);

    /**
     * Ensures that the current user is following the specified user.
     *
     * @param userOid OID The user the current user wants to start following.
     * @return {@link CurrentUserFollowedItemDTO} representing the user follow that was updated
     */
    CurrentUserFollowedItemDTO followUser(OID userOid);

    /**
     * Ensures that the current user is not following the specified user.
     *
     * @param userOid OID The user the current user wants to stop following.
     * @return {@link CurrentUserFollowedItemDTO} representing the user follow that was updated
     */
    CurrentUserFollowedItemDTO stopFollowingUser(OID userOid);
}
