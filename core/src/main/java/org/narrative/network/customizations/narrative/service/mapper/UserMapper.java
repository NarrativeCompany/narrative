package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.CoreUtils;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.EmailAddressVerificationStep;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserStats;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.controller.postbody.user.UserNotificationSettingsInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.user.UserPersonalSettingsInputDTO;
import org.narrative.network.customizations.narrative.service.api.model.CurrentUserFollowedItemDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedUserDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardLeaderboardUserDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserEmailAddressDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserNeoWalletDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserNotificationSettingsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserOwnedChannelsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserPersonalSettingsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserReferralDetailsDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserStatsDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

@Mapper(config = ServiceMapperConfig.class, uses = {UserReputationMapper.class})
public abstract class UserMapper {

    /**
     * Map from {@link User} entity to {@link UserDTO}.
     *
     * @param userEntity The incoming user entity to map
     * @return The mapped user
     */
    @Mapping(source = "largeSquareThumbnailAvatarUrl", target = "avatarSquareUrl")
    @Mapping(source = "circleLabels", target = "labels")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "displayNameResolvedForJson", target = "displayName")
    public abstract UserDTO mapUserEntityToUserDTO(User userEntity);

    UserDTO mapAreaUserEntityToUser(AreaUser areaUser) {
        return mapUserEntityToUserDTO(areaUser.getUser());
    }

    public abstract List<UserDTO> mapUserEntityListToUserList(List<User> users);

    public abstract List<UserDTO> mapAreaUserEntityListToUserList(List<AreaUser> areaUsers);

    /**
     * Map from {@link User} to {@link UserDetailDTO}.
     *
     * @param userEntity Incoming user entity to map.
     * @return The mapped user.
     */
    @Mapping(source = "userFields.registrationDate", target = "joined")
    @Mapping(source = "userStats.lastLoginDatetime", target = "lastVisit")
    @Mapping(source = "preferences.hideMyFollowers", target = "hideMyFollowers")
    @Mapping(source = "preferences.hideMyFollows", target = "hideMyFollows")
    public abstract UserDetailDTO mapUserEntityToUserDetail(User userEntity);

    /**
     * Map from {@link UserStats} entity to {@link UserStatsDTO}.
     *
     * @param userStatsEntity The incoming user stats entity to map
     * @return The mapped user stats
     */
    public abstract UserStatsDTO mapUserStatsEntityToUserStats(UserStats userStatsEntity);

    /**
     * Map from {@link AreaUserRlm} entity to {@link UserDTO}.
     *
     * @param areaUserRlm of the User
     * @return The User
     */
    UserDTO areaUserRlmToUserDTO(AreaUserRlm areaUserRlm) {
        if (!CoreUtils.exists(areaUserRlm)) {
            return null;
        } else {
            return mapUserEntityToUserDTO(areaUserRlm.getUser());
        }
    }

    /**
     * Map from {@link UserNotificationSettingsInputDTO} to {@link UserNotificationSettingsDTO}
     *
     * @param inputDTO The incoming request DTO
     * @return The mapped settings DTO
     */
    public abstract UserNotificationSettingsDTO mapUserNotificationsSettingsReqToDTO(UserNotificationSettingsInputDTO inputDTO);

    /**
     * Map from {@link UserPersonalSettingsInputDTO} to {@link UserPersonalSettingsDTO}
     *
     * @param inputDTO The incoming request DTO
     * @return The mapped settings DTO
     */
    public abstract UserPersonalSettingsDTO mapUserPersonalSettingsInputToDTO(UserPersonalSettingsInputDTO inputDTO);

    @Mapping(expression = "java(areaUserRlm.getOwnedNiches().size())", target = UserOwnedChannelsDTO.Fields.ownedNiches)
    @Mapping(source = "user.countOwnedPublications", target = UserOwnedChannelsDTO.Fields.ownedPublications)
    public abstract UserOwnedChannelsDTO mapAreaUserRlmToUserOwnedChannelsDTO(AreaUserRlm areaUserRlm);

    /**
     * Map from {@link User} to {@link UserReferralDetailsDTO}
     *
     * @param user The incoming settings DTO
     * @return The mapped request DTO
     */
    @Mapping(source = "rank", target = "rank")
    @Mapping(source = "nrveEarned", target = "nrveEarned")
    @Mapping(source = "user.confirmedWaitListInviteCount", target = "friendsJoined")
    public abstract UserReferralDetailsDTO mapUseToReferralDetailsDTO(User user, Integer rank, NrveValue nrveEarned);

    /**
     * Map from {@link User} entity to {@link CurrentUserFollowedItemDTO}.
     *
     * @param userEntity The User entity to map
     * @return The mapped {@link CurrentUserFollowedItemDTO}
     */
    @Mapping(source = "watchedByCurrentUser", target = "followed")
    public abstract CurrentUserFollowedItemDTO mapUserEntityToCurrentUserFollowedItem(User userEntity);

    /**
     * Map from {@link User} to {@link FollowedUserDTO}
     *
     * @param user The {@link User} to map
     * @return The mapped {@link FollowedUserDTO}
     */
    @Mapping(source = "user", target = "user")
    @Mapping(target = "currentUserFollowedItem", ignore = true)
    public abstract FollowedUserDTO mapUserEntityToFollowedUser(User user);

    @AfterMapping
    void map(User user, @MappingTarget FollowedUserDTO.FollowedUserDTOBuilder builder) {
        if(user.getWatchedByCurrentUser()!=null) {
            builder.currentUserFollowedItem(mapUserEntityToCurrentUserFollowedItem(user));
        }
    }

    public abstract List<FollowedUserDTO> mapUserEntitiesToFollowedUsers(List<User> user);

    public RewardLeaderboardUserDTO mapUserRewardObjectPairToRewardLeaderboardUserDTO(ObjectPair<User, NrveValue> pair) {
        return RewardLeaderboardUserDTO.builder()
                .user(mapUserEntityToUserDTO(pair.getOne()))
                .reward(new NrveUsdValue(pair.getTwo()))
                .build();
    }

    public abstract List<RewardLeaderboardUserDTO> mapUserRewardObjectPairListToRewardLeaderboardUserDTOList(List<ObjectPair<User, NrveValue>> pairs);

    @Mapping(source = "wallet.neoWallet.neoAddress", target = UserNeoWalletDTO.Fields.neoAddress)
    @Mapping(source = "neoWalletWaitingPeriodEndDatetime", target = UserNeoWalletDTO.Fields.waitingPeriodEndDatetime)
    @Mapping(source = "wallet.balanceDetail", target = UserNeoWalletDTO.Fields.currentBalance)
    public abstract UserNeoWalletDTO mapUserToUserNeoWalletDTO(User user);

    @Mapping(source = "oid", target = UserEmailAddressDetailDTO.Fields.oid)
    @Mapping(source = "userFields.emailAddress.emailAddress", target = UserEmailAddressDetailDTO.Fields.emailAddress)
    @Mapping(source = "userFields.pendingEmailAddress.emailAddress", target = UserEmailAddressDetailDTO.Fields.pendingEmailAddress)
    @Mapping(source = "userFields.pendingEmailAddress.expirationDatetime", target = UserEmailAddressDetailDTO.Fields.pendingEmailAddressExpirationDatetime)
    // jw: these fields will be mapped by the map method below!
    @Mapping(ignore = true, target = UserEmailAddressDetailDTO.Fields.incompleteVerificationSteps)
    public abstract UserEmailAddressDetailDTO mapUserToUserEmailAddressDetailDTO(User user);

    @AfterMapping
    void map(User user, @MappingTarget UserEmailAddressDetailDTO.UserEmailAddressDetailDTOBuilder builder) {
        EmailAddress emailAddress = user.getUserFields().getEmailAddress();
        EmailAddress pendingEmailAddress = user.getUserFields().getPendingEmailAddress();

        // jw: if we have a pending email address, let's give precedence to that.
        if (exists(pendingEmailAddress)) {
            builder.incompleteVerificationSteps(pendingEmailAddress.getIncompleteVerificationSteps());

        // jw: if we do not have a pending email address, let's add any pending verification steps for the primary email address instead.
        } else if (!emailAddress.isVerified()) {
            Set<EmailAddressVerificationStep> incompleteVerificationSteps = emailAddress.getIncompleteVerificationSteps();

            assert incompleteVerificationSteps.size() == 1 : "Should only ever have one missing verification step for PRIMARY email addresses.";
            assert incompleteVerificationSteps.contains(EmailAddressVerificationStep.NEW_USER_STEP) : "The only missing verification step for the PRIMARY email address should only ever be the NEW_USER_STEP.";

            builder.incompleteVerificationSteps(incompleteVerificationSteps);
        }
    }
}
