package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.InvalidParamError;
import org.narrative.common.util.UnexpectedError;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.area.user.AreaUserStats;
import org.narrative.network.core.area.user.WatchedUser;
import org.narrative.network.core.area.user.services.RemoveWatchedUserTask;
import org.narrative.network.core.area.user.services.UpdateWatchedUserTask;
import org.narrative.network.core.fileondisk.base.services.DeleteFileOnDisk;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.UploadedFileData;
import org.narrative.network.core.content.base.UploadedImageFileData;
import org.narrative.network.core.content.base.services.ContentList;
import org.narrative.network.core.content.base.services.ContentSort;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.narrative.rewards.ContentCreatorRewardRole;
import org.narrative.network.core.narrative.rewards.NicheModeratorReward;
import org.narrative.network.core.narrative.rewards.NicheOwnerReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.rewards.RoleContentReward;
import org.narrative.network.core.narrative.rewards.UserActivityBonus;
import org.narrative.network.core.narrative.rewards.UserActivityReward;
import org.narrative.network.core.narrative.rewards.UserElectorateReward;
import org.narrative.network.core.narrative.rewards.UserTribunalReward;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserStats;
import org.narrative.network.core.user.services.ExportUserProfileAsJsonTask;
import org.narrative.network.core.user.services.preferences.AreaNotificationType;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.core.user.services.preferences.NotificationSettingsHelper;
import org.narrative.network.core.user.services.preferences.UserPreferences;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.controller.UserController;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheAssociationSlot;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheUserAssociation;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.customizations.narrative.posts.ChannelContent;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.ContentStreamService;
import org.narrative.network.customizations.narrative.service.api.RewardsService;
import org.narrative.network.customizations.narrative.service.api.UserService;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
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
import org.narrative.network.customizations.narrative.service.api.services.ParseObjectFromUnknownIdTask;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil;
import org.narrative.network.customizations.narrative.service.impl.user.follow.BuildFollowedNichesTask;
import org.narrative.network.customizations.narrative.service.impl.user.follow.BuildFollowedPublicationsTask;
import org.narrative.network.customizations.narrative.service.impl.user.follow.BuildFollowedUsersTask;
import org.narrative.network.customizations.narrative.service.impl.user.follow.BuildUserFollowersTask;
import org.narrative.network.customizations.narrative.service.mapper.EmailAddressMapper;
import org.narrative.network.customizations.narrative.service.mapper.NicheDerivativeMapper;
import org.narrative.network.customizations.narrative.service.mapper.NicheMapper;
import org.narrative.network.customizations.narrative.service.mapper.NicheUserAssociationMapper;
import org.narrative.network.customizations.narrative.service.mapper.PostMapper;
import org.narrative.network.customizations.narrative.service.mapper.PublicationMapper;
import org.narrative.network.customizations.narrative.service.mapper.RewardPeriodMapper;
import org.narrative.network.customizations.narrative.service.mapper.UserMapper;
import org.narrative.network.customizations.narrative.service.mapper.WalletTransactionMapper;
import org.narrative.network.customizations.narrative.services.RestUploadUtils;
import org.narrative.network.customizations.narrative.util.FollowedItemScrollable;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.commons.lang3.time.DateUtils;
import org.jooq.lambda.function.Consumer2;
import org.jooq.lambda.function.Consumer4;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private static final int MAX_NICHE_SLOTS = NicheAssociationSlot.values().length;
    private static final NetworkLogger logger = new NetworkLogger(UserServiceImpl.class);

    private final AreaTaskExecutor areaTaskExecutor;
    private final UserMapper userMapper;
    private final NicheUserAssociationMapper nicheUserAssociationMapper;
    private final NicheDerivativeMapper nicheDerivativeMapper;
    private final PostMapper postMapper;
    private final NicheMapper nicheMapper;
    private final RewardPeriodMapper rewardPeriodMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final EmailAddressMapper emailAddressMapper;
    private final PublicationMapper publicationMapper;
    private final RewardsService rewardsService;
    private final StaticMethodWrapper staticMethodWrapper;
    private final NarrativeProperties narrativeProperties;
    private final ContentStreamService contentStreamService;

    public UserServiceImpl(
            AreaTaskExecutor areaTaskExecutor,
            UserMapper userMapper,
            NicheUserAssociationMapper nicheUserAssociationMapper,
            NicheDerivativeMapper nicheDerivativeMapper,
            PostMapper postMapper,
            NicheMapper nicheMapper,
            RewardPeriodMapper rewardPeriodMapper,
            WalletTransactionMapper walletTransactionMapper,
            EmailAddressMapper emailAddressMapper,
            PublicationMapper publicationMapper,
            RewardsService rewardsService,
            StaticMethodWrapper staticMethodWrapper,
            NarrativeProperties narrativeProperties,
            ContentStreamService contentStreamService
    ) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.userMapper = userMapper;
        this.nicheUserAssociationMapper = nicheUserAssociationMapper;
        this.nicheDerivativeMapper = nicheDerivativeMapper;
        this.postMapper = postMapper;
        this.nicheMapper = nicheMapper;
        this.rewardPeriodMapper = rewardPeriodMapper;
        this.walletTransactionMapper = walletTransactionMapper;
        this.emailAddressMapper = emailAddressMapper;
        this.publicationMapper = publicationMapper;
        this.rewardsService = rewardsService;
        this.staticMethodWrapper = staticMethodWrapper;
        this.narrativeProperties = narrativeProperties;
        this.contentStreamService = contentStreamService;
    }

    public List<NicheUserAssociationDTO> getCurrentUserNicheAssociations() {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<List<NicheUserAssociationDTO>>(false) {
            @Override
            protected List<NicheUserAssociationDTO> doMonitoredTask() {
                //Must be a registered user
                getNetworkContext().getPrimaryRole().checkRegisteredUser();

                return getNicheAssociationsForUser(getNetworkContext().getUser());
            }
        });
    }

    @Override
    public List<NicheUserAssociationDTO> getNicheAssociationsForUser(User user) {
        // jw: note: this is basically a implementation that mirrors what mapstruct would have done, It is just sourcing
        //     from the User object instead of a List.
        if (user == null) {
            return null;
        }

        AreaUserRlm areaUserRlm = AreaUser.getAreaUserRlm(user.getLoneAreaUser());

        List<NicheUserAssociation> associations = new ArrayList<>(areaUserRlm.getNicheUserAssociations().values());
        associations.sort(NicheUserAssociation.NICHE_COMPARATOR);

        List<Niche> niches = associations.stream().map(NicheUserAssociation::getNiche).collect(Collectors.toList());
        FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(staticMethodWrapper.networkContext().getUser(), niches);

        return nicheUserAssociationMapper.mapNicheUserAssociationEntitiesToNicheUserAssociations(associations);
    }

    @Override
    public List<PublicationUserAssociationDTO> getPublicationAssociationsForUser(User user) {
        // jw: note: this is basically a implementation that mirrors what mapstruct would have done, It is just sourcing
        //     from the User object instead of a List.
        if (user == null) {
            return Collections.emptyList();
        }

        List<ObjectPair<Publication, ChannelUser>> associations = Publication.dao().getAssociatedPublications(user);

        // jw: let's short out if there is nothing to show.
        if (associations.isEmpty()) {
            return Collections.emptyList();
        }

        // jw: next, we should populate the channel consumer followed by for all of those publications.
        FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(staticMethodWrapper.networkContext().getUser(), ObjectPair.getAllOnes(associations));

        // jw: we are finally able to perform the mapping.
        return publicationMapper.mapPublicationUserAssociationsToDtos(associations, user);
    }

    @Override
    public User getUser(OID oid) throws UserPrincipalNotFoundException {
        if (oid == null) {
            throw new UserPrincipalNotFoundException("Oid is NULL.");
        }

        User user = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<User>(false) {
            @Override
            protected User doMonitoredTask() {
                return User.dao().getForApiParam(oid, "user_oid");
            }
        });

        if (user == null) {
            throw new UserPrincipalNotFoundException("User " + oid.getValue() + " was not found.");
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        User user = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<User>(false) {
            @Override
            protected User doMonitoredTask() {
                return User.dao().getUserByUsername(getNetworkContext().getAuthZone(), username);
            }
        });

        if (user == null) {
            throw new InvalidParamError(UserController.USERNAME_PARAM, username);
        }

        return user;
    }

    private User getUserByParam(OID userOid) {
        return User.dao().getForApiParam(userOid, UserController.USER_OID_PARAM);
    }

    @Override
    public UserDetailDTO getUserProfile(String userId) {
        User user = areaTaskExecutor.executeAreaTask(new ParseObjectFromUnknownIdTask<User>(userId, UserController.USER_ID_PARAM) {
            @Override
            protected User getFromOid(OID oid) {
                return User.dao().get(oid);
            }
            @Override
            protected User getFromPrettyUrlString(String prettyUrlString) {
                return User.dao().getUserByUsername(getNetworkContext().getAuthZone(), prettyUrlString);
            }
        });

        if (!exists(user)) {
            return null;
        }

        return userMapper.mapUserEntityToUserDetail(user);
    }

    @Override
    public UserDetailDTO getUserProfileByUsername(String username) {
        return userMapper.mapUserEntityToUserDetail(getUserByUsername(username));
    }

    @Override
    public ResponseEntity<Resource> getCurrentUserProfileZip() {
        ExportUserProfileAsJsonTask task = new ExportUserProfileAsJsonTask(staticMethodWrapper.networkContext().getUser());
        File zipFile = staticMethodWrapper.networkContext().doGlobalTask(task);
        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, task.getContentDisposition())
                .body(new FileSystemResource(zipFile));
    }

    @Override
    public UserDTO registerUser(RegisterUserInput registerInput) {
        User user = staticMethodWrapper.networkContext().doGlobalTask(new RegisterUserTask(registerInput));
        return userMapper.mapUserEntityToUserDTO(user);
    }

    @Override
    public ScalarResultDTO<String> validateRegisterUserStepOne(RegisterUserStepOneInput stepOneInput) {
        String recaptchaToken = staticMethodWrapper.networkContext().doGlobalTask(new ValidateRegisterUserStepOneTask(stepOneInput));
        return ScalarResultDTO.<String>builder().value(recaptchaToken).build();
    }

    @Override
    public User getCurrentUser(Locale locale) {
        // jw: let's ensure that this is only being requested by a registered user.
        networkContext().getPrimaryRole().checkRegisteredUser();

        User currentUser = networkContext().getUser();

        updateUserFormatPreferencesIfNecessary(locale, currentUser);

        updateUserLastLoginTime(currentUser);

        return currentUser;
    }

    protected void updateUserLastLoginTime(User currentUser) {
        Timestamp newLogin = new Timestamp(System.currentTimeMillis());
        Timestamp lastLoginDateTime = currentUser.getUserStats().getLastLoginDatetime();

        if (lastLoginDateTime == null || newLogin.after(DateUtils.addHours(lastLoginDateTime, 1))) {
            PartitionGroup.addEndOfPartitionGroupRunnableForUtilityThread(() -> TaskRunner.doRootAreaTask(currentUser.getAuthZone().getOid(), new AreaTaskImpl<Object>() {
                protected Object doMonitoredTask() {
                    //update the user stats
                    UserStats userStats = UserStats.dao().getLocked(currentUser.getUserStats().getOid());
                    userStats.setLastLoginDatetime(newLogin);

                    AreaUserStats areaUserStats = AreaUserStats.dao().getLocked(currentUser.getLoneAreaUser().getOid());
                    areaUserStats.updateLoginTime(userStats.getLastLoginDatetime());
                    return null;
                }
            }));
        }
    }

    protected void updateUserFormatPreferencesIfNecessary(Locale locale, User currentUser) {
        Locale originalLocale = currentUser.getFormatPreferences().getLocale();

        // Update the format preferences for the current user if they changed
        if (locale != originalLocale) {
            FormatPreferences formatPreferences = currentUser.getFormatPreferences();
            formatPreferences.setLocale(locale);

            updateFormatPreferencesForUser(currentUser, formatPreferences);
        }
    }

    @Override
    public UserReferralDetailsDTO getUserReferralDetails(OID userOid) {
        User user = getUserByParam(userOid);

        Integer rank = user.getConfirmedWaitListInviteCount() == 0 ? null : User.dao().getWaitListInviteRank(user);
        NrveValue nrveEarned = user.getConfirmedWaitListInviteCount() == 0 ? null : WalletTransaction.dao().getTransactionSumToWallet(user.getWallet(), WalletTransactionType.REFERRAL_TYPES);

        return userMapper.mapUseToReferralDetailsDTO(user, rank, nrveEarned);
    }

    @Override
    public UserDTO uploadAvatar(MultipartFile file) {
        networkContext().getPrimaryRole().checkRegisteredUser();

        // create the UploadedFileData from the provided file.
        UploadedFileData uploadedFileData = RestUploadUtils.getUploadedFileData(file, FileUsageType.AVATAR);

        Consumer4<NetworkContext, UploadedFileData, FileType, ValidationContext> validateAvatarUploadFn = RestUploadUtils::validateUploadedFileData;

        //Pass the validation function into the task implementation but curry all but the last argument which is the validation context that will be injected by the task
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<UserDTO>(true, validateAvatarUploadFn.acceptPartially(networkContext(), uploadedFileData, null)) {
            @Override
            protected UserDTO doMonitoredTask() {
                // bl: process the file synchronously now
                uploadedFileData.postUploadProcess(uploadedFileData.getFileUploadProcessOid());

                User user = getNetworkContext().getUser();

                ImageOnDisk existingCustomAvatar = user.getAvatar();

                user.updateAvatar(new ImageOnDisk((UploadedImageFileData) uploadedFileData, user));

                // jw: if we had a uploaded avatar from before this update that is no longer associated then delete it fully!
                if (exists(existingCustomAvatar)) {
                    getNetworkContext().doGlobalTask(new DeleteFileOnDisk(existingCustomAvatar));
                }

                // bl: flush all sessions so that we save the files to GCP before we delete them all
                PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

                // bl: now that we are done, let's delete all of the temp files created internally in the UploadedImageFileData
                uploadedFileData.deleteAllTempFiles();

                return userMapper.mapUserEntityToUserDTO(user);
            }
        });
    }

    @Override
    public UserDTO deleteAvatar() {
        networkContext().getPrimaryRole().checkRegisteredUser();
        User user = networkContext().getUser();

        ImageOnDisk avatar = user.getAvatar();

        if (exists(avatar)) {
            user.updateAvatar(null);
            areaTaskExecutor.executeGlobalTask(new DeleteFileOnDisk(avatar));
        }

        return userMapper.mapUserEntityToUserDTO(user);
    }

    @Override
    public UserNotificationSettingsDTO findUserNotificationSettings() {
        //Build a lambda that points at our validation method
        Consumer2<NetworkContext, ValidationContext> validateRequestFn = UserServiceImpl.this::validateNotificationSettingsRequest;

        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<UserNotificationSettingsDTO>(false, validateRequestFn.acceptPartially(networkContext())) {
            @Override
            protected UserNotificationSettingsDTO doMonitoredTask() {
                User user = getNetworkContext().getUser();

                NotificationSettingsHelper<AreaNotificationType> helper = constructHelper(user, getAreaContext().getArea());

                return createUserNotificationSettingsDTO(user, helper);
            }
        });
    }

    @Override
    public UserNotificationSettingsDTO updateUserNotificationSettings(UserNotificationSettingsDTO pendingNotificationSettings) {
        //Build a lambda that points at our validation method
        Consumer2<NetworkContext, ValidationContext> validateRequestFn = UserServiceImpl.this::validateNotificationSettingsRequest;

        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<UserNotificationSettingsDTO>(true, validateRequestFn.acceptPartially(networkContext())) {
            @Override
            protected UserNotificationSettingsDTO doMonitoredTask() {
                User user = getNetworkContext().getUser();

                NotificationSettingsHelper<AreaNotificationType> helper = constructHelper(user, getAreaContext().getArea());
                UserPreferences userPreferences = user.getPreferences();

                //Set the new preference values - these calls set values on the underlying entities which are
                //flushed at the end of task execution
                userPreferences.setSuspendAllEmails(pendingNotificationSettings.getSuspendAllEmails());
                helper.setNotificationFlag(AreaNotificationType.SOMEONE_FOLLOWED_ME, pendingNotificationSettings.getNotifyWhenFollowed());
                helper.setNotificationFlag(AreaNotificationType.SOMEONE_MENTIONS_ME, pendingNotificationSettings.getNotifyWhenMentioned());

                return createUserNotificationSettingsDTO(user, helper);
            }
        });
    }

    private UserNotificationSettingsDTO createUserNotificationSettingsDTO(User user, NotificationSettingsHelper<AreaNotificationType> helper) {
        UserNotificationSettingsDTO.UserNotificationSettingsDTOBuilder dtoBuilder = UserNotificationSettingsDTO.builder();
        dtoBuilder.suspendAllEmails(user.isSuspendAllEmails());
        dtoBuilder.notifyWhenFollowed(helper.isNotificationSet(AreaNotificationType.SOMEONE_FOLLOWED_ME));
        dtoBuilder.notifyWhenMentioned(helper.isNotificationSet(AreaNotificationType.SOMEONE_MENTIONS_ME));

        return dtoBuilder.build();
    }

    private void validateNotificationSettingsRequest(NetworkContext networkContext, ValidationContext validationContext) {
        //Must be a registered user to fetch or update preferences
        networkContext.getPrimaryRole().checkRegisteredUser();
    }

    private NotificationSettingsHelper<AreaNotificationType> constructHelper(User user, Area area) {
        AreaUser areaUser = user.getAreaUserByArea(area);
        return areaUser.getNotificationSettingsHelper();
    }

    @Override
    public UserPersonalSettingsDTO getPersonalSettingsForCurrentUser() {
        User user = validateAndGetCurrentUser();
        return createUserPersonalSettingsDTO(user);
    }

    @Override
    public UserPersonalSettingsDTO updatePersonalSettingsForCurrentUser(UserPersonalSettingsDTO settings) {
        User user = validateAndGetCurrentUser();
        UserPreferences preferences = user.getPreferences();
        preferences.setContentQualityFilter(settings.getQualityFilter());
        preferences.setHideMyFollowers(settings.isHideMyFollowers());
        preferences.setHideMyFollows(settings.isHideMyFollows());
        // bl: only allow this flag to be set if the user is allowed to see restricted content
        if(AgeRating.ageRatingsContainRestricted(user.getPermittedAgeRatings())) {
            preferences.setDisplayAgeRestrictedContent(settings.isDisplayAgeRestrictedContent());
        }
        return createUserPersonalSettingsDTO(user);
    }

    private UserPersonalSettingsDTO createUserPersonalSettingsDTO(User user) {
        UserPreferences prefs = user.getPreferences();

        UserPersonalSettingsDTO.UserPersonalSettingsDTOBuilder builder = UserPersonalSettingsDTO.builder();
        builder.qualityFilter(prefs.getContentQualityFilter());
        builder.hideMyFollowers(prefs.isHideMyFollowers());
        builder.hideMyFollows(prefs.isHideMyFollows());
        // bl: display age restricted content if the user's preferred age ratings contains it.
        // using this to derive the value instead of the flag on UserPreferences directly since
        // this will ensure we honor the user's permitted age ratings (e.g. if the user is no longer certified)
        builder.displayAgeRestrictedContent(AgeRating.ageRatingsContainRestricted(user.getPreferredAgeRatings()));
        return builder.build();
    }

    private User validateAndGetCurrentUser() {
        //Must be a registered user to fetch or update preferences
        networkContext().getPrimaryRole().checkRegisteredUser();
        return networkContext().getUser();
    }

    @Override
    public UserOwnedChannelsDTO getUserOwnedChannels() {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<UserOwnedChannelsDTO>(false) {
            @Override
            protected UserOwnedChannelsDTO doMonitoredTask() {
                //Must be a registered user
                networkContext().getPrimaryRole().checkRegisteredUser();

                User user = networkContext().getUser();
                AreaUserRlm areaUserRlm = getAreaContext().getAreaUserRlm();
                if (areaUserRlm == null) {
                    throw UnexpectedError.getRuntimeException("AreaUserRlm is null for user! OID: " + user.getOid());
                }

                return userMapper.mapAreaUserRlmToUserOwnedChannelsDTO(areaUserRlm);
            }
        });
    }

    @Override
    public void enableTwoFactorSecretForCurrentUser(String secret) {
        areaTaskExecutor.executeAreaTask(new AreaTaskImpl<Boolean>(true) {
            @Override
            protected Boolean doMonitoredTask() {
                //Must be a registered user
                getNetworkContext().getPrimaryRole().checkRegisteredUser();

                //Cannot set a secret if 2FA is already enabled - it must be disabled first
                if (getNetworkContext().getUser().isTwoFactorAuthenticationEnabled()) {
                    throw UnexpectedError.getRuntimeException("You cannot set a secret for a user that already has 2FA enabled - disable first!");
                }

                getNetworkContext().getUser().updateTwoFactorAuthenticationSecretKey(secret);

                return true;
            }
        });
    }

    @Override
    public void disableTwoFactorSecretForCurrentUser(DisableTwoFactorAuthInput disableTwoFactorAuthInput) {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        User user = primaryRole.getUser();
        areaTaskExecutor.executeAreaTask(new UpdateProfileAccountConfirmationBaseTask<Boolean>(user, disableTwoFactorAuthInput) {
            @Override
            protected Boolean doMonitoredTask() {
                //Remove the key
                getNetworkContext().getUser().updateTwoFactorAuthenticationSecretKey(null);

                return true;
            }
        });
    }

    /**
     * Is two factor authentication enabled for the current user?
     */
    @Override
    public boolean isTwoFactorAuthEnabledForCurrentUser() {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<Boolean>(false) {
            @Override
            protected Boolean doMonitoredTask() {
                //Must be a registered user
                getNetworkContext().getPrimaryRole().checkRegisteredUser();

                return getNetworkContext().getUser().isTwoFactorAuthenticationEnabled();
            }
        });
    }

    @Override
    public void recoverPassword(RecoverPasswordInput recoverPasswordInput) {
        areaTaskExecutor.executeGlobalTask(new RecoverPasswordTask(recoverPasswordInput));
    }

    @Override
    public PWResetURLValidationResultDTO validateResetURLParams(OID userOid, Long timestamp, String key) {
        User user = User.dao().get(userOid);

        PWResetURLValidationResultDTO.PWResetURLValidationResultDTOBuilder resBuilder = PWResetURLValidationResultDTO.builder();
        try {
            ResetPasswordTask.validateResetURLParams(user, staticMethodWrapper.getAreaContext(), timestamp, key);

            // Check to see if the URL timestamp has expired - only need to do this when the rest of the URL is valid
            boolean expired = ResetPasswordTask.isURLTimestampExpired(timestamp);
            if (expired) {
                resBuilder.expired(true);
                logger.info("Password reset URL timestamp has expired");
            } else {
                // jw: since this is valid let's set it up accordingly.
                resBuilder.valid(true)
                    .twoFactorEnabled(user.isTwoFactorAuthenticationEnabled());
            }
        } catch (Exception e) {
            logger.warn("Error validating reset password URL parameters: " + e.getMessage());
        }

        return resBuilder.build();
    }

    @Override
    public void resetPassword(OID userOid, ResetPasswordInput input) {
        User user = User.dao().getForApiParam(userOid, UserController.USER_OID_PARAM);

        areaTaskExecutor.executeAreaTask(new ResetPasswordTask(user, input));
    }

    @Override
    public UserDTO updateUserProfileForCurrentUser(UpdateUserProfileInput updateProfileInput) {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        User user = primaryRole.getUser();

        user = staticMethodWrapper.networkContext().doGlobalTask(new UpdateUserProfileTask(user, updateProfileInput));

        return userMapper.mapUserEntityToUserDTO(user);
    }

    @Override
    public UserEmailAddressDetailDTO getEmailAddressDetailForCurrentUser() {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        return userMapper.mapUserToUserEmailAddressDetailDTO(primaryRole.getUser());
    }

    @Override
    public UserEmailAddressDetailDTO updateEmailAddressForCurrentUser(UpdateEmailAddressInput updateEmailAddress) {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        staticMethodWrapper.getAreaContext().doAreaTask(new UpdateUserEmailAddressTask(primaryRole.getUser(), updateEmailAddress));

        return userMapper.mapUserToUserEmailAddressDetailDTO(primaryRole.getUser());
    }

    @Override
    public void verifyEmailAddressForUser(User user, VerifyEmailAddressInput input) {
        staticMethodWrapper.getAreaContext().doAreaTask(new VerifyPrimaryEmailAddressTask(user, input));
    }

    @Override
    public VerifyEmailAddressResultDTO verifyPendingEmailAddressForUser(User user, VerifyPendingEmailAddressInput input) {
        EmailAddress emailAddress = staticMethodWrapper.getAreaContext().doAreaTask(new VerifyPendingEmailAddressTask(user, input));

        return emailAddressMapper.mapEmailAddressToVerifyEmailAddressResultDTO(emailAddress);
    }

    @Override
    public ScalarResultDTO<String> cancelPendingEmailAddressChangeForUser(User user, VerifyPendingEmailAddressInput input) {
        return staticMethodWrapper.getAreaContext().doAreaTask(new CancelUserEmailAddressChangeTask(user, input));
    }

    @Override
    public void resendVerificationEmailForCurrentUser() {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        staticMethodWrapper.networkContext().doGlobalTask(new ResendVerificationEmailTask(primaryRole.getUser()));
    }

    @Override
    public TokenDTO updatePasswordForCurrentUser(UpdatePasswordInput updatePassword) {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        return staticMethodWrapper.getAreaContext().doAreaTask(new UpdateUserPasswordTask(primaryRole.getUser(), updatePassword));
    }

    @Override
    public void deleteCurrentUser(DeleteUserInput deleteUserInput) {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        staticMethodWrapper.getAreaContext().doAreaTask(new DeleteNarrativeUserTask(primaryRole.getUser(), deleteUserInput));
    }

    @Override
    public void renewTosAgreementForCurrentUser(UserRenewTosAgreementInput renewTosInput) {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        staticMethodWrapper.getAreaContext().doAreaTask(new RenewTosAgreementForUserTask(primaryRole.getUser()));
    }

    @Override
    public void updateFormatPreferencesForUser(User user, FormatPreferences formatPreferences) {
        PartitionGroup.getCurrentPartitionGroup().addEndOfGroupRunnable(() -> TaskRunner.doRootGlobalTask(new UpdateUserFormatPreferencesTask(user.getOid(), formatPreferences)));
    }

    @Override
    public SuspendEmailValidationDTO validateSuspendEmailPreference(User user, SuspendEmailInput suspendEmailInput) {
        SuspendEmailValidationDTO.SuspendEmailValidationDTOBuilder builder = SuspendEmailValidationDTO.builder().user(userMapper.mapUserEntityToUserDTO(user));
        try {
            SuspendUserEmailsTask.validate(user, suspendEmailInput);
        } catch (ApplicationError error) {
            // jw: pass any errors down to the consumer. Note: can't throw the error since we want to present the error
            //     as an alert and redirect them to home. Without this they will get a popup and remain on the page with
            //     no easy way to know what was wrong. This gives the front end much more control over how this issue is
            //     handled and presented to the user.
            builder.error(error.getMessage());
        }

        return builder.build();
    }

    @Override
    public void setSuspendEmailPreference(User user, SuspendEmailInput suspendEmailInput) {
        staticMethodWrapper.networkContext().doGlobalTask(new SuspendUserEmailsTask(user, suspendEmailInput));
    }

    @Override
    public Collection<NicheDTO> getNichesMostPostedToByCurrentUser(int count) {
        // jw: first, let's ensure that we have a current user to base this process of of.
        AreaRole areaRole = staticMethodWrapper.getAreaContext().getAreaRole();
        areaRole.getPrimaryRole().checkRegisteredUser();

        // jw: now we can try to get the niches they have posted to most in the last ~ six months.
        List<Niche> niches = ChannelContent.dao().getNichesMostPostedToByUser(
                areaRole.getAreaUserRlm(),
                Instant.now().minus(180, ChronoUnit.DAYS),
                count
        );

        if (niches.isEmpty()) {
            niches = ChannelContent.dao().getNichesMostPostedToByUser(
                    areaRole.getAreaUserRlm(),
                    // jw: if we failed to find any niches this user has posted to in the last ~ six months, then let's
                    //     try expanding it to any niche they have ever posted to in the last year.
                    Instant.now().minus(365, ChronoUnit.DAYS),
                    count
            );
        }

        // jw: we need to setup the followedByCurrentUser for all results.
        FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(areaRole.getUser(), niches);

        return nicheDerivativeMapper.mapNicheEntityListToNicheList(niches);
    }

    @Override
    public PageDataDTO<PostDTO> getPublishedPostsByCurrentUser(Pageable pageRequest) {
        ContentList criteria = getContentListForCurrentUserPosts(pageRequest);
        criteria.setSort(ContentSort.LIVE_DATETIME);
        criteria.setSortAsc(false);

        return getPostsPageData(criteria, pageRequest, false);
    }

    @Override
    public PageDataDTO<PostDTO> getDraftPostsByCurrentUser(Pageable pageRequest) {
        ContentList criteria = getContentListForCurrentUserPosts(pageRequest);
        criteria.setSort(ContentSort.SAVE_DATETIME);
        criteria.setSortAsc(false);

        // jw: restrict this list to drafts only.
        criteria.setDraft(true);

        return getPostsPageData(criteria, pageRequest, false);
    }

    @Override
    public PageDataDTO<PostDTO> getPendingPostsByCurrentUser(Pageable pageRequest) {
        ContentList criteria = getContentListForCurrentUserPosts(pageRequest);
        criteria.setSort(ContentSort.LIVE_DATETIME);
        criteria.setSortAsc(false);

        // bl: restrict this list to moderated/pending posts only.
        criteria.setModerated(true);

        return getPostsPageData(criteria, pageRequest, true);
    }

    private ContentList getContentListForCurrentUserPosts(Pageable pageRequest) {
        networkContext().getPrimaryRole().checkRegisteredUser();

        ContentList criteria = new ContentList(areaContext().getPortfolio(), ContentType.NARRATIVE_POST);
        // jw: these content lists are concerned with drafts and posts by the author.
        criteria.setAuthor(areaContext().getAreaUserRlm());

        // jw: ensure that we set this up based on the request
        PageUtil.mutateCriteriaListWithPagingCriteria(criteria, pageRequest);

        return criteria;
    }

    private PageDataDTO<PostDTO> getPostsPageData(ContentList criteria, Pageable pageRequest, boolean isPendingPosts) {
        // jw: ensure that we are getting a count of all results, since we will need that to drive the PageDataDTO
        criteria.doCount(true);

        List<PostDTO> postDtos = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<List<PostDTO>>(false) {
            @Override
            protected List<PostDTO> doMonitoredTask() {
                // jw: first, let's execute the criteria and get our results.
                List<Content> contents = getAreaContext().doAreaTask(criteria);

                Set<Channel> publishedToChannels = contents.stream().map(Content::getPublishedToChannels).flatMap(Collection::stream).collect(Collectors.toSet());

                // bl: if we are returning pending posts, we need to include the publications in which posts are pending
                if(isPendingPosts) {
                    // bl: we need to include the Publication on each post so it can be displayed in the pending list
                    contents.forEach(content -> content.setIncludePublicationForModeratedContent(true));

                    publishedToChannels.addAll(contents.stream().map(Content::getSubmittedToPublication).filter(Objects::nonNull).map(Publication::getChannel).collect(Collectors.toSet()));
                }
                FollowedChannel.dao().populateChannelsFollowedByCurrentUserField(getNetworkContext().getUser(), publishedToChannels);

                return postMapper.mapContentListToPostDTOList(contents);
            }
        });

        //Build a page from the results
        return PageUtil.buildPage(postDtos, pageRequest, criteria.getCount());
    }

    @Override
    public Set<AgeRating> getPermittedAgeRatingsForCurrentUser() {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        return primaryRole.getUser().getPermittedAgeRatings();
    }

    @Override
    public ScalarResultDTO<NrveUsdValue> getCurrentUserRewardsBalance() {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        return ScalarResultDTO.<NrveUsdValue>builder().value(new NrveUsdValue(primaryRole.getUser().getWallet().getBalance())).build();
    }

    @Override
    public List<RewardPeriodDTO> getUserRewardPeriods(OID userOid) {
        User user = getUserByParam(userOid);

        List<RewardPeriod> rewardPeriods = RewardPeriod.dao().getRewardPeriodsForUser(user);
        return rewardPeriodMapper.mapRewardPeriodEntityListToRewardPeriodDTOList(rewardPeriods);
    }

    @Override
    public UserRewardPeriodStatsDTO getUserRewardPeriodRewards(OID userOid, String yearMonthStr) {
        User user = getUserByParam(userOid);
        User currentUser = staticMethodWrapper.networkContext().getUser();

        RewardPeriod rewardPeriod = rewardsService.getRewardPeriodFromParam(yearMonthStr, false);

        Wallet rewardWallet = rewardPeriod.getWallet();
        Wallet userWallet = user.getWallet();

        NrveValue totalContentCreationReward = WalletTransaction.dao().getTransactionSumBetweenWallets(rewardWallet, userWallet, Collections.singleton(WalletTransactionType.CONTENT_REWARD));

        NrveValue totalNicheOwnershipRewards = NrveValue.ZERO;
        List<ObjectPair<Niche,NrveUsdValue>> nicheOwnershipRewards = new LinkedList<>();
        {
            List<NicheOwnerReward> nicheOwnerRewards = NicheOwnerReward.dao().getForUserRewardPeriod(user, rewardPeriod);
            List<Niche> niches = nicheOwnerRewards.stream().map(nor -> nor.getNicheReward().getNiche()).collect(Collectors.toList());
            FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(currentUser, niches);
            for (NicheOwnerReward nicheOwnerReward : nicheOwnerRewards) {
                NrveValue amount = nicheOwnerReward.getTransaction().getNrveAmount();
                NrveUsdValue nrveUsdValue = new NrveUsdValue(amount);
                Niche niche = nicheOwnerReward.getNicheReward().getNiche();
                nicheOwnershipRewards.add(new ObjectPair<>(niche, nrveUsdValue));
                totalNicheOwnershipRewards = totalNicheOwnershipRewards.add(nrveUsdValue.getNrve());
            }
        }

        NrveValue totalNicheModerationReward = NicheModeratorReward.dao().getTransactionSumForUserRewardPeriod(user, rewardPeriod);

        UserActivityReward userActivityReward = UserActivityReward.dao().getForUserRewardPeriod(user, rewardPeriod);
        NrveValue totalActivityReward = exists(userActivityReward) ? userActivityReward.getTransaction().getNrveAmount() : NrveValue.ZERO;
        int activityBonusPercentage = exists(userActivityReward) ? userActivityReward.getBonus().getBonusPercentage() : 0;

        UserTribunalReward userTribunalReward = UserTribunalReward.dao().getForUserRewardPeriod(user, rewardPeriod);
        NrveValue totalTribunalReward = exists(userTribunalReward) ? userTribunalReward.getTransaction().getNrveAmount() : NrveValue.ZERO;

        UserElectorateReward userElectorateReward = UserElectorateReward.dao().getForUserRewardPeriod(user, rewardPeriod);
        NrveValue totalElectorateReward = exists(userElectorateReward) ? userElectorateReward.getTransaction().getNrveAmount() : NrveValue.ZERO;

        Map<RewardSlice,NrveUsdValue> rewardBySlice = new HashMap<>();
        rewardBySlice.put(RewardSlice.CONTENT_CREATORS, new NrveUsdValue(totalContentCreationReward));
        rewardBySlice.put(RewardSlice.NICHE_OWNERS, new NrveUsdValue(totalNicheOwnershipRewards));
        rewardBySlice.put(RewardSlice.NICHE_MODERATORS, new NrveUsdValue(totalNicheModerationReward));
        rewardBySlice.put(RewardSlice.USER_ACTIVITY, new NrveUsdValue(totalActivityReward));
        rewardBySlice.put(RewardSlice.ELECTORATE, new NrveUsdValue(totalElectorateReward));
        rewardBySlice.put(RewardSlice.TRIBUNAL, new NrveUsdValue(totalTribunalReward));

        NrveValue totalRewards = rewardBySlice.values().stream().map(NrveUsdValue::getNrve).reduce(NrveValue.ZERO, NrveValue::add);

        BigDecimal percentageOfTotalPayout = totalRewards.getValue().multiply(BigDecimal.valueOf(100)).divide(rewardPeriod.getTotalRewardsDisbursed().getValue(), 4, RoundingMode.HALF_UP);
        // bl: if the value is larger than 1, let's only show 2 decimal places. otherwise, we'll keep it at 4
        if(percentageOfTotalPayout.compareTo(BigDecimal.ONE) >= 0) {
            percentageOfTotalPayout = percentageOfTotalPayout.setScale(2, RoundingMode.HALF_UP);
        }

        return UserRewardPeriodStatsDTO.builder()
                .rewardPeriodRange(rewardPeriod.getRewardYearMonth().getRewardPeriodRange())

                .totalContentCreationReward(rewardBySlice.get(RewardSlice.CONTENT_CREATORS))
                .totalNicheOwnershipReward(rewardBySlice.get(RewardSlice.NICHE_OWNERS))
                .totalNicheModerationReward(rewardBySlice.get(RewardSlice.NICHE_MODERATORS))
                .totalActivityRewards(rewardBySlice.get(RewardSlice.USER_ACTIVITY))
                .activityBonusPercentage(activityBonusPercentage)
                .totalElectorateReward(rewardBySlice.get(RewardSlice.ELECTORATE))
                .totalTribunalReward(rewardBySlice.get(RewardSlice.TRIBUNAL))
                .totalReward(new NrveUsdValue(totalRewards))

                .nicheOwnershipRewards(nicheMapper.mapNicheOwnershipRewards(nicheOwnershipRewards))

                .percentageOfTotalPayout(percentageOfTotalPayout)

                .build();
    }

    @Override
    public PageDataDTO<UserRewardTransactionDTO> getUserRewardTransactions(OID userOid, Pageable pageRequest) {
        User user = getUserByParam(userOid);

        // bl: filter transactions if not the current user.
        Collection<WalletTransactionType> excludeTypes;
        if(user.isCurrentUserThisUser()) {
            // bl: a little silly, but in order to use the same queries, let's just pick a type that doesn't
            // apply when the user is viewing his/her own list. this way, all transactions should be included.
            excludeTypes = EnumSet.of(WalletTransactionType.INITIAL_TOKEN_MINT);
        } else {
            // bl: when viewing someone else's transaction list, you don't get to see tips or redemptions
            excludeTypes = WalletTransactionType.PRIVATE_USER_TYPES;
        }
        long transactionCount = WalletTransaction.dao().getTransactionCountForUser(user, excludeTypes);
        List<WalletTransaction> transactions = WalletTransaction.dao().getTransactionsForUser(user, excludeTypes, pageRequest.getPageNumber() + 1, pageRequest.getPageSize());

        // bl: now that we have the transactions, we need to populate the metadata items on the transaction
        Map<WalletTransactionType,Set<WalletTransaction>> typeToMetadataTransactions = new HashMap<>();
        for (WalletTransaction transaction : transactions) {
            WalletTransactionType type = transaction.getType();
            if(WalletTransactionType.METADATA_TYPES.contains(type)) {
                Set<WalletTransaction> transactionsForType = typeToMetadataTransactions.computeIfAbsent(type, k -> new HashSet<>());
                transactionsForType.add(transaction);
            }

            // bl: always set the user on the WalletTransaction so we know if the amount is a positive or negative value
            transaction.setUserForTransactionList(user);
        }

        // CONTENT_REWARD: WalletTransaction.metadataPost and WalletTransaction.metadataContentCreatorRewardRole
        {
            Set<WalletTransaction> contentTransactions = typeToMetadataTransactions.remove(WalletTransactionType.CONTENT_REWARD);
            if(!isEmptyOrNull(contentTransactions)) {
                Map<OID,ObjectPair<OID, ContentCreatorRewardRole>> transactionOidToRewardMetadata = RoleContentReward.dao().getTransactionOidToRewardMetadata(contentTransactions);
                assert transactionOidToRewardMetadata.size()==contentTransactions.size() : "Should always find a ContentReward for every CONTENT_REWARD WalletTransaction! oids/" + contentTransactions.stream().map(WalletTransaction::getOid).collect(Collectors.toList());
                Map<OID,Content> contentOidToContent = Content.dao().getIDToObjectsFromObjects(Content.dao().getObjectsFromIDsWithCache(ObjectPair.getAllOnes(transactionOidToRewardMetadata.values())));
                for (WalletTransaction transaction : contentTransactions) {
                    ObjectPair<OID, ContentCreatorRewardRole> pair = transactionOidToRewardMetadata.get(transaction.getOid());
                    OID contentOid = pair.getOne();
                    ContentCreatorRewardRole role = pair.getTwo();
                    Content content = contentOidToContent.get(contentOid);
                    // bl: check that the Content actually exists. we can get uninitialized proxies from the cache that
                    // correspond to deleted Content, in which case the response breaks. note that null is perfectly
                    // acceptable to use as a value here, as the front end will handle it properly as a deleted post.
                    transaction.setMetadataPost(exists(content) ? content : null);
                    transaction.setMetadataContentCreatorRewardRole(role);
                }
            }
        }

        // NICHE_OWNERSHIP_REWARD: WalletTransaction.metadataNiche
        {
            Set<WalletTransaction> nicheTransactions = typeToMetadataTransactions.remove(WalletTransactionType.NICHE_OWNERSHIP_REWARD);
            if(!isEmptyOrNull(nicheTransactions)) {
                Map<OID,Niche> transactionOidToNiche = NicheOwnerReward.dao().getTransactionOidToNiche(nicheTransactions);
                assert transactionOidToNiche.size()==nicheTransactions.size() : "Should always find a NicheOwnerReward for every NICHE_OWNERSHIP_REWARD WalletTransaction! oids/" + nicheTransactions.stream().map(WalletTransaction::getOid).collect(Collectors.toList());
                for (WalletTransaction transaction : nicheTransactions) {
                    transaction.setMetadataNiche(transactionOidToNiche.get(transaction.getOid()));
                }
            }
        }

        // NICHE_MODERATION_REWARD: WalletTransaction.metadataNiche
        {
            Set<WalletTransaction> nicheTransactions = typeToMetadataTransactions.remove(WalletTransactionType.NICHE_MODERATION_REWARD);
            if(!isEmptyOrNull(nicheTransactions)) {
                Map<OID,Niche> transactionOidToNiche = NicheModeratorReward.dao().getTransactionOidToNiche(nicheTransactions);
                assert transactionOidToNiche.size()==nicheTransactions.size() : "Should always find a NicheModeratorReward for every NICHE_MODERATION_REWARD WalletTransaction! oids/" + nicheTransactions.stream().map(WalletTransaction::getOid).collect(Collectors.toList());
                for (WalletTransaction transaction : nicheTransactions) {
                    transaction.setMetadataNiche(transactionOidToNiche.get(transaction.getOid()));
                }
            }
        }

        // ACTIVITY_REWARD: WalletTransaction.metadataActivityBonusPercentage
        {
            Set<WalletTransaction> activityTransactions = typeToMetadataTransactions.remove(WalletTransactionType.ACTIVITY_REWARD);
            if(!isEmptyOrNull(activityTransactions)) {
                Map<OID,UserActivityBonus> transactionOidToActivityBonus = UserActivityReward.dao().getTransactionOidToActivityBonus(activityTransactions);
                assert transactionOidToActivityBonus.size()==activityTransactions.size() : "Should always find a UserActivityReward for every ACTIVITY_REWARD WalletTransaction! oids/" + activityTransactions.stream().map(WalletTransaction::getOid).collect(Collectors.toList());
                for (WalletTransaction transaction : activityTransactions) {
                    UserActivityBonus bonus = transactionOidToActivityBonus.get(transaction.getOid());
                    // bl: only include the bonus if there is one
                    if(!bonus.isNone()) {
                        transaction.setMetadataActivityBonusPercentage(bonus.getBonusPercentage());
                    }
                }
            }
        }

        // USER_TIP: WalletTransaction.metadataUser
        {
            Set<WalletTransaction> userTransactions = typeToMetadataTransactions.remove(WalletTransactionType.USER_TIP);
            if(!isEmptyOrNull(userTransactions)) {
                Set<Wallet> otherUserWallets = new HashSet<>();
                for (WalletTransaction transaction : userTransactions) {
                    otherUserWallets.add(transaction.getOtherWallet());
                }
                Map<OID,User> walletOidToUser = User.dao().getWalletOidToUser(otherUserWallets);
                for (WalletTransaction transaction : userTransactions) {
                    Wallet otherWallet = transaction.getOtherWallet();
                    User otherUser = walletOidToUser.get(otherWallet.getOid());
                    assert otherUser!=null : "Failed to identify other user for tip! transaction/" + transaction.getOid();
                    transaction.setMetadataUser(otherUser);
                }
            }
        }

        // NICHE_REFUND: WalletTransaction.metadataNiche
        {
            Set<WalletTransaction> nicheTransactions = typeToMetadataTransactions.remove(WalletTransactionType.NICHE_REFUND);
            if(!isEmptyOrNull(nicheTransactions)) {
                Map<OID,Niche> transactionOidToNiche = NicheAuctionInvoice.dao().getRefundTransactionOidToNiche(nicheTransactions);
                assert transactionOidToNiche.size()==nicheTransactions.size() : "Should always find a NicheAuctionInvoice payment with a refund for every NICHE_REFUND WalletTransaction! oids/" + nicheTransactions.stream().map(WalletTransaction::getOid).collect(Collectors.toList());
                for (WalletTransaction transaction : nicheTransactions) {
                    transaction.setMetadataNiche(transactionOidToNiche.get(transaction.getOid()));
                }
            }
        }

        // REFUND_REVERSAL: WalletTransaction.metadataNiche
        {
            Set<WalletTransaction> nicheTransactions = typeToMetadataTransactions.remove(WalletTransactionType.REFUND_REVERSAL);
            if(!isEmptyOrNull(nicheTransactions)) {
                // will need to update this once we support publications
                Map<OID,Niche> transactionOidToNiche = NicheAuctionInvoice.dao().getReversalTransactionOidToNiche(nicheTransactions);
                assert transactionOidToNiche.size()==nicheTransactions.size() : "Should always find a NicheAuctionInvoice payment with a reversal for every REFUND_REVERSAL WalletTransaction! oids/" + nicheTransactions.stream().map(WalletTransaction::getOid).collect(Collectors.toList());
                for (WalletTransaction transaction : nicheTransactions) {
                    transaction.setMetadataNiche(transactionOidToNiche.get(transaction.getOid()));
                }
            }
        }

        assert typeToMetadataTransactions.isEmpty() : "Should have processed all WalletTransactionTypes with metadata at this point! Need to add support for publications? remaining types/" + typeToMetadataTransactions.keySet();

        Set<Channel> allChannels = new HashSet<>();
        // bl: add all niches that are direct associations with transactions
        allChannels.addAll(transactions.stream()
                .map(WalletTransaction::getMetadataNiche)
                .filter(Objects::nonNull)
                .map(Niche::getChannel)
                .collect(Collectors.toSet()));
        // bl: also have to add all niches and publications to which posts are published
        allChannels.addAll(transactions.stream()
                .map(WalletTransaction::getMetadataPost)
                .filter(CoreUtils::exists)
                .map(Content::getPublishedToChannels)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));
        FollowedChannel.dao().populateChannelsFollowedByCurrentUserField(staticMethodWrapper.networkContext().getUser(), allChannels);
        List<UserRewardTransactionDTO> transactionDtos = walletTransactionMapper.mapWalletTransactionEntityListToUserRewardTransactionDTOList(transactions);
        return PageUtil.buildPage(transactionDtos, pageRequest, transactionCount);
    }

    @Override
    public FollowedUsersDTO getUsersFollowedByUser(OID userOid, FollowedItemScrollable scrollable) {
        return areaTaskExecutor.executeAreaTask(new BuildFollowedUsersTask(
                userOid,
                scrollable.getScrollParams(),
                scrollable.getResolvedCount(narrativeProperties)
        ));
    }

    @Override
    public FollowedNichesDTO getNichesFollowedByUser(OID userOid, FollowedItemScrollable scrollable) {
        return areaTaskExecutor.executeAreaTask(new BuildFollowedNichesTask(
                userOid,
                scrollable.getScrollParams(),
                scrollable.getResolvedCount(narrativeProperties)
        ));
    }


    @Override
    public FollowedPublicationsDTO getPublicationsFollowedByUser(OID userOid, FollowedItemScrollable scrollable) {
        return areaTaskExecutor.executeAreaTask(new BuildFollowedPublicationsTask(
                userOid,
                scrollable.getScrollParams(),
                scrollable.getResolvedCount(narrativeProperties)
        ));
    }

    @Override
    public UserFollowersDTO getUsersFollowingUser(OID userOid, FollowedItemScrollable scrollable) {
        return areaTaskExecutor.executeAreaTask(new BuildUserFollowersTask(
                userOid,
                scrollable.getScrollParams(),
                scrollable.getResolvedCount(narrativeProperties)
        ));
    }

    @Override
    public CurrentUserFollowedItemDTO getUserFollowStatus(OID userOid) {
        // bl: the task here is really a no-op; just return the same WatchedUser since we're just fetching, not updating
        return doFollowUserTask(userOid, (currentUser, userToFollow, watchedUser) -> watchedUser);
    }

    @Override
    public CurrentUserFollowedItemDTO followUser(OID userOid) {
        return doFollowUserTask(userOid,
                (currentUser, userToFollow, watchedUser) ->
                        areaTaskExecutor.executeAreaTask(new UpdateWatchedUserTask(userToFollow, currentUser, watchedUser, false))
        );
    }

    @Override
    public CurrentUserFollowedItemDTO stopFollowingUser(OID userOid) {
        return doFollowUserTask(userOid, (currentUser, userToFollow, watchedUser) -> {
            if(exists(watchedUser)) {
                areaTaskExecutor.executeAreaTask(new RemoveWatchedUserTask(watchedUser));
            }
            return null;
        });
    }

    private CurrentUserFollowedItemDTO doFollowUserTask(OID userOid, FollowUserTask task) {
        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        primaryRole.checkRegisteredUser();

        User userToFollow = User.dao().getForApiParam(userOid, UserController.USER_OID_PARAM);

        if(userToFollow.isCurrentUserThisUser()) {
            throw UnexpectedError.getRuntimeException("Can't follow yourself!");
        }

        WatchedUser watchedUser = userToFollow.getWatchedUserForCurrentUserWatchingThisUser();

        watchedUser = task.doTask(primaryRole.getUser(), userToFollow, watchedUser);

        userToFollow.setCurrentUserFollow(watchedUser);

        return userMapper.mapUserEntityToCurrentUserFollowedItem(userToFollow);
    }

    private interface FollowUserTask {
        WatchedUser doTask(User currentUser, User userToFollow, WatchedUser watchedUser);
    }
}
