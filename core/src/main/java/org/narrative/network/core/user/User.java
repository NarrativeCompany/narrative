package org.narrative.network.core.user;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.IntBitmaskType;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateEnumSetType;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.SandboxedAreaUser;
import org.narrative.network.core.area.user.WatchedUser;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.composition.base.ShareableObject;
import org.narrative.network.core.fileondisk.base.services.filesystem.NetworkPath;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.fileondisk.image.ImageType;
import org.narrative.network.core.narrative.wallet.NeoTransaction;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletRef;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.security.area.base.AreaGuest;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.security.area.community.advanced.services.GlobalSecurable;
import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import org.narrative.network.core.user.dao.UserDAO;
import org.narrative.network.core.user.services.MobilePushNotificationTask;
import org.narrative.network.core.user.services.SendUserNeoWalletChangedEmail;
import org.narrative.network.core.user.services.UsernameUtils;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.core.user.services.preferences.InstantNotificationDeliveryMethod;
import org.narrative.network.core.user.services.preferences.UserPreferences;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.permissions.UserPermission;
import org.narrative.network.customizations.narrative.personaljournal.PersonalJournal;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationWaitListEntry;
import org.narrative.network.customizations.narrative.reputation.UserReputation;
import org.narrative.network.customizations.narrative.service.api.model.UserAgeStatus;
import org.narrative.network.customizations.narrative.service.api.model.permissions.PermissionDTO;
import org.narrative.network.customizations.narrative.service.impl.user.SuspendUserEmailsTask;
import org.narrative.network.customizations.narrative.services.GoogleCloudStorageFileHandler;
import org.narrative.network.customizations.narrative.services.ReactRoute;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.tasktypes.NetworkTaskImpl;
import org.narrative.shared.event.reputation.ConductEventType;
import org.narrative.shared.event.reputation.ConductStatusEvent;
import org.narrative.shared.event.reputation.NegativeQualityEvent;
import org.narrative.shared.event.reputation.NegativeQualityEventType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:22:38 PM
 */
@Entity
@Proxy
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "user_emailAddress_uidx", columnNames = {User.FIELD__AUTH_ZONE__COLUMN, User.FIELD__EMAIL_ADDRESS__COLUMN}),
        @UniqueConstraint(columnNames = {User.FIELD__AUTH_ZONE__COLUMN, User.FIELD__USERNAME__COLUMN}),
        @UniqueConstraint(name = "user_wallet_uidx", columnNames = {User.FIELD__WALLET__COLUMN})
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User extends PrimaryRole implements DAOObject<UserDAO>, ShareableObject, WalletRef {

    public static final String DEFAULT_AVATAR_PATH = "/images/site/page/narrative-avatar.png";
    public static final String DEFAULT_LARGE_AVATAR_PATH = "/images/site/page/narrative-avatar-large.png";

    private static final String HAS_JIT_INITED_FOLLOWED_USER_PROPERTY = User.class.getName() + "-HasJITInitedFollowedUser";

    public static final BigDecimal REDEMPTION_MINIMUM_IN_USD = BigDecimal.valueOf(20L);
    public static final BigDecimal REDEMPTION_MAXIMUM_PER_YEAR_IN_USD = BigDecimal.valueOf(20000L);
    public static final Duration NEO_ADDRESS_WAITING_PERIOD = Duration.ofDays(2);

    private AuthZone authZone;
    private UserFields userFields;
    private UserPreferences preferences;
    private FormatPreferences formatPreferences;
    private IntBitmaskType<UserStatus> userStatus;

    private Set<AreaUser> areaUsers;

    private UserStats userStats;

    private PersonalJournal personalJournal;

    private Wallet wallet;

    private ImageOnDisk avatar;
    private String displayName;
    private String username;

    private String twoFactorAuthenticationSecretKey;
    private EnumSet<TwoFactorAuthenticationBackupCode> usedTwoFactorAuthenticationBackupCodes;

    // jw: using a Instant to represent this so that we can do more advanced things with this if necessary. For example,
    //     if we decide to allow the user to start using fiat payments again once they do something, we can use this time
    //     to determine whether that action took place after their last chargeback.
    private Instant lastPaymentChargebackDatetime;

    private int confirmedWaitListInviteCount;
    private Instant lastConfirmedWaitListInviteDatetime;

    private Instant firstAvatarUploadDatetime;

    private Instant lastWalletAddressChangeDatetime;

    private Map<AuthProvider, UserAuth> userAuths;

    private List<FollowedChannel> followedChannels;

    private Map<Channel, ChannelUser> channelUsers;

    // jw: currently Hibernate only allows one many to one association from an one object type to another object type.  Because of
    //     this I am going to exclude the users this user is watching and instead keep the one for users watching this
    //     user.  Since this user is being deleted we should never query for their records, and ultimately we dont delete
    //     users, so this shouldnt matter anyways.
    //private List<WatchedUser> watchingUsers;
    private List<WatchedUser> usersWatching;

    private transient boolean hasLookedUpInternalCredentials;
    private transient Credentials credentials;

    private transient Boolean watchedByCurrentUser;

    public static final int MIN_DISPLAY_NAME_LENGTH = 1;
    public static final int MAX_DISPLAY_NAME_LENGTH = 40;

    public static final String FIELD__AUTH_ZONE__NAME = "authZone";
    public static final String FIELD__USER_FIELDS__NAME = "userFields";
    public static final String FIELD__PROFILE__NAME = "profile";

    public static final String FIELD__AUTH_ZONE__COLUMN = FIELD__AUTH_ZONE__NAME;
    public static final String FIELD__EMAIL_ADDRESS__COLUMN = FIELD__USER_FIELDS__NAME + "_" + UserFields.FIELD__EMAIL_ADDRESS__COLUMN;
    public static final String FIELD__DISPLAY_NAME__NAME = "displayName";
    public static final String FIELD__USERNAME__NAME = "username";
    public static final String FIELD__USERNAME__COLUMN = FIELD__USERNAME__NAME;
    public static final String FIELD__USER_STATUS__NAME = "userStatus";

    public static final String FIELD__USERS_WATCHING__NAME = "usersWatching";

    public static final String FIELD__WALLET__NAME = "wallet";
    public static final String FIELD__WALLET__COLUMN = FIELD__WALLET__NAME+"_"+Wallet.FIELD__OID__NAME;

    public static String getUserFieldNestedPropertyName(String userFieldsPropertyName) {
        return FIELD__USER_FIELDS__NAME + "." + userFieldsPropertyName;
    }

    /**
     * @deprecated hibernate use only
     */
    public User() {}

    public User(AuthZone authZone, EmailAddress emailAddress, Wallet wallet, FormatPreferences formatPreferences) {
        super(true);
        this.authZone = authZone;
        userStats = new UserStats(this);
        preferences = new UserPreferences(true);

        if (formatPreferences == null) {
            formatPreferences = FormatPreferences.getDefaultFormatPreferences();
        }

        this.formatPreferences = formatPreferences;
        userStatus = new IntBitmaskType<>();
        this.userFields = new UserFields(true);
        // jw: now that it is saved, we can associate the two entities
        this.userFields.setEmailAddress(emailAddress);
        emailAddress.setUser(this);
        this.userAuths = newHashMap();
        this.channelUsers = newHashMap();
        this.usersWatching = new LinkedList<>();
        this.wallet = wallet;
        this.usedTwoFactorAuthenticationBackupCodes = EnumSet.noneOf(TwoFactorAuthenticationBackupCode.class);
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return super.getOid();
    }

    @NotNull
    @Type(type = AuthZoneDataType.TYPE)
    public AuthZone getAuthZone() {
        return authZone;
    }

    public void setAuthZone(AuthZone authZone) {
        this.authZone = authZone;
    }

    @NotNull
    public UserFields getUserFields() {
        return userFields;
    }

    public void setUserFields(UserFields userFields) {
        this.userFields = userFields;
    }

    @Transient
    public String getEmailAddress() {
        return getUserFields().getEmailAddress().getEmailAddress();
    }

    @NotNull
    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }

    @NotNull
    public FormatPreferences getFormatPreferences() {
        return formatPreferences;
    }

    public void setFormatPreferences(FormatPreferences formatPreferences) {
        this.formatPreferences = formatPreferences;
    }

    /**
     * @return the area users for this user
     * @deprecated hibernate bug.  AVOID THIS METHOD AT ALL COSTS. It has major performance overhead,
     * especially when run against the default metadata owner user who has almost 40k AreaUser records.
     */
    @OneToMany(mappedBy = AreaUser.FIELD__USER__NAME, fetch = FetchType.LAZY, cascade = javax.persistence.CascadeType.ALL)
    // bl: commenting this out since it seems to cause problems with session flushing.  need to wait for hibernate
    // to fix a bug that is causing the "collection was not processed by flush()" error.
    //@Size(min=1)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Set<AreaUser> getAreaUsers() {
        return areaUsers;
    }

    public void setAreaUsers(Set<AreaUser> areaUsers) {
        this.areaUsers = areaUsers;
    }

    @NotNull
    public int getConfirmedWaitListInviteCount() {
        return confirmedWaitListInviteCount;
    }

    public void setConfirmedWaitListInviteCount(int confirmedWaitListInvites) {
        this.confirmedWaitListInviteCount = confirmedWaitListInvites;
    }

    @Type(type = HibernateInstantType.TYPE)
    public Instant getLastConfirmedWaitListInviteDatetime() {
        return lastConfirmedWaitListInviteDatetime;
    }

    public void setLastConfirmedWaitListInviteDatetime(Instant lastConfirmedWaitListDatetime) {
        this.lastConfirmedWaitListInviteDatetime = lastConfirmedWaitListDatetime;
    }

    @Type(type = HibernateInstantType.TYPE)
    public Instant getFirstAvatarUploadDatetime() {
        return firstAvatarUploadDatetime;
    }

    public void setFirstAvatarUploadDatetime(Instant firstAvatarUploadDatetime) {
        this.firstAvatarUploadDatetime = firstAvatarUploadDatetime;
    }

    @Type(type = HibernateInstantType.TYPE)
    public Instant getLastWalletAddressChangeDatetime() {
        return lastWalletAddressChangeDatetime;
    }

    public void setLastWalletAddressChangeDatetime(Instant lastWalletAddressChangeDatetime) {
        this.lastWalletAddressChangeDatetime = lastWalletAddressChangeDatetime;
    }

    @Transient
    public AreaUser getAreaUserByArea(Area area) {
        return AreaUser.dao().getAreaUserFromUserAndArea(getOid(), area.getOid());
    }

    @Transient
    public Credentials getInternalCredentials() {
        if (!hasLookedUpInternalCredentials) {
            final UserAuth userAuth = getUserAuthForAuthProvider(getAuthZone().getInternalAuthProvider());
            credentials = getAuthZone().getInternalCredentials(userAuth);
            hasLookedUpInternalCredentials = true;
        }
        return credentials;
    }

    @Transient
    public boolean isHasInternalCredentials() {
        return exists(getInternalCredentials());
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @JoinColumn(name = User.FIELD__OID__NAME)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    public UserStats getUserStats() {
        return userStats;
    }

    public void setUserStats(UserStats userStats) {
        this.userStats = userStats;
    }

    // jw: because of how Channel works, and how we need the oid from User/Niche before we can create it, this MUST be
    //     setup as optional. Otherwise we will get a constraint violation trying to setup these objects.
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @JoinColumn(name = User.FIELD__OID__NAME)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    public PersonalJournal getPersonalJournal() {
        return personalJournal;
    }

    public void setPersonalJournal(PersonalJournal personalJournal) {
        this.personalJournal = personalJournal;
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @ForeignKey(name = "fk_user_wallet")
    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    @NotNull
    public IntBitmaskType<UserStatus> getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(IntBitmaskType<UserStatus> userStatus) {
        this.userStatus = userStatus;
    }

    @Transient
    public boolean isRegisteredUser() {
        return true;
    }

    @Transient
    public User getUser() {
        return this;
    }

    @Transient
    @org.jetbrains.annotations.NotNull
    public AreaRole getAreaRoleForArea(Area area) {
        assert isEqual(getAuthZone(), area.getAuthZone()) : "Should never attempt to get an AreaRole for a User that has a different AuthZone than the specified Area! oid/" + getOid() + " uAz/" + getAuthZone() + " a/" + area.getOid() + " aAz/" + area.getAuthZone();

        AreaUser areaUser = AreaUser.dao().getAreaUserFromUserAndArea(getOid(), area.getOid());
        if (exists(areaUser)) {
            return areaUser;
        }

        // user is authenticated at the network level, but user doesn't have an AreaUser
        // in this Area.  thus, the scenario is that the PrimaryRole is set to the User,
        // but the AreaRole is just a guest.
        return new AreaGuest(area, this);
    }

    @Transient
    public boolean isCurrentUserThisUser() {
        User user = networkContext().getUser();
        return exists(user) && isEqual(this, user);
    }

    @Transient
    public List<String> getCircleLabels() {
        return getLoneAreaUser().getEffectiveAreaCircles().stream().filter(Objects::nonNull).filter(a -> !a.isViewableByAdminsOnly()).filter(a -> !StringUtils.isEmpty(a.getLabel())).map(AreaCircle::getLabel).collect(Collectors.toList());
    }

    @Transient
    public String getProfileUrl() {
        return ReactRoute.USER_PROFILE.getUrl(getIdForUrl());
    }

    @Transient
    public String getKycDetailsUrl() {
        return ReactRoute.MEMBER_CERTIFICATION.getUrl();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "FK285FEB101B5B4E")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    public ImageOnDisk getAvatar() {
        return avatar;
    }

    public void setAvatar(ImageOnDisk avatar) {
        this.avatar = avatar;
    }

    public void updateAvatar(ImageOnDisk avatar) {
        setAvatar(avatar);

        // jw: if this is the first time an avatar has been set on this user, then let's track when this happened
        if (exists(avatar) && getFirstAvatarUploadDatetime()==null) {
            // jw: I had considered clearing this when the provided avatar is null, but this column is used for tracking
            //     when the user first uploads an avatar, if they later remove it and then add a new one we do not want
            //     to track that and then give them credit again. Basically, this is not meant to only exist if the user
            //     currently has an avatar, but just to tell us when they uploaded their first one.
            setFirstAvatarUploadDatetime(Instant.now());
        }
    }

    @Transient
    public String getLargeAvatarUrl() {
        return getAvatarUrl(ImageType.LARGE);
    }

    @Transient
    public String getSquareThumbnailAvatarUrl() {
        return getAvatarUrl(ImageType.SQUARE_THUMBNAIL);
    }

    @Transient
    public String getLargeSquareThumbnailAvatarUrl() {
        return getAvatarUrl(ImageType.LARGE_SQUARE_THUMBNAIL);
    }

    @Transient
    private String getAvatarUrl(ImageType imageType) {
        ImageOnDisk avatar = getAvatar();
        if(!exists(avatar)) {
            return null;
        }
        // jw: if we are aiming to render the large square thumbnail, but the squareThumbnail is the largest we have lets
        //     go ahead and fall back to the Square Thumbnail.
        if (imageType.isLargeSquareThumbnail()) {
            // jw: let's just return the primarySquareAvatarImageUrl as defined by ImageOnDisk, in case the primary image is too small for large.
            return avatar.getPrimarySquareAvatarImageUrl();
        }

        NetworkPath networkPath = avatar.getNetworkPathForImageType(imageType);
        return GoogleCloudStorageFileHandler.IMAGES.getFileUri(networkPath);
    }

    @Transient
    public boolean isHasAvatarSet() {
        return exists(getAvatar());
    }

    @NotNull
    @Length(min = MIN_DISPLAY_NAME_LENGTH, max = MAX_DISPLAY_NAME_LENGTH)
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Transient
    public String getDisplayNameForHtml() {
        return HtmlTextMassager.disableHtml(getDisplayName());
    }

    @Transient
    @Override
    public String getDisplayNameResolved() {
        // bl: changing the implementation here to just always return the HTML-friendly version of display name.
        // this method should really only be used currently where we want it in HTML.
        return HtmlTextMassager.disableHtml(getDisplayNameResolvedForJson());
    }

    @Transient
    public String getDisplayNameResolvedForJson() {
        // bl: no longer clearing out display names when we delete users.
        // bl: we used to change display names to the format "u" + OID when deleting a user.
        // we technically only need to do that on the SandboxedAreaUser table now, which has the unique
        // constraint.  thus, detect any old deleted users that had the u + OID format, and just
        // use the default guest name for those.
        if (isDeleted()) {
            return wordlet("role.formerNarrator");
        }

        // bl: in all other cases, just show the display name.  it should not be hyperlinked if the user is deleted.
        return getDisplayName();
    }

    @Transient
    public void updateDisplayName(String displayName) {
        // bl: do nothing if the display name isn't changing.
        if (isEqual(displayName, getDisplayName())) {
            return;
        }
        setDisplayName(displayName);
        for (AreaUser areaUser : getAreaUsers()) {
            areaUser.setDisplayName(displayName);
        }
        getSandboxedAreaUser().setDisplayName(displayName);
    }

    @Length(max = UsernameUtils.MAX_LENGTH)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Transient
    public String getIdForUrl() {
        if (!isEmpty(getUsername())) {
            return getUsername();
        }
        return getOid().toString();
    }

    @Column(columnDefinition = "tinytext")
    public String getTwoFactorAuthenticationSecretKey() {
        return twoFactorAuthenticationSecretKey;
    }

    public void setTwoFactorAuthenticationSecretKey(String twoFactorAuthenticationSecretKey) {
        this.twoFactorAuthenticationSecretKey = twoFactorAuthenticationSecretKey;
    }

    // jw: utility function to ensure that the used 2FA backup codes get cleared along with a changing secret key.
    public void updateTwoFactorAuthenticationSecretKey(String twoFactorAuthenticationSecretKey) {
        String secretKey = getTwoFactorAuthenticationSecretKey();
        setTwoFactorAuthenticationSecretKey(twoFactorAuthenticationSecretKey);

        // jw: if the secret key changed, then let's clear the used backup codes.
        if (!isEqual(secretKey, twoFactorAuthenticationSecretKey)) {
            setUsedTwoFactorAuthenticationBackupCodes(EnumSet.noneOf(TwoFactorAuthenticationBackupCode.class));
        }
    }

    @NotNull
    @Type(type = HibernateEnumSetType.TYPE, parameters = {@Parameter(name = HibernateEnumSetType.ENUM_CLASS, value = TwoFactorAuthenticationBackupCode.TYPE)})
    public EnumSet<TwoFactorAuthenticationBackupCode> getUsedTwoFactorAuthenticationBackupCodes() {
        return usedTwoFactorAuthenticationBackupCodes;
    }

    public void setUsedTwoFactorAuthenticationBackupCodes(EnumSet<TwoFactorAuthenticationBackupCode> usedTwoFactorAuthenticationCodes) {
        this.usedTwoFactorAuthenticationBackupCodes = usedTwoFactorAuthenticationCodes;
    }

    private transient EnumSet<TwoFactorAuthenticationBackupCode> availableTwoFactorAuthenticationBackupCodes;

    @Transient
    public EnumSet<TwoFactorAuthenticationBackupCode> getAvailableTwoFactorAuthenticationBackupCodes() {
        if (availableTwoFactorAuthenticationBackupCodes == null) {
            // jw: this is pretty easy, just need to get the complement of this!
            availableTwoFactorAuthenticationBackupCodes = EnumSet.complementOf(getUsedTwoFactorAuthenticationBackupCodes());
        }

        return availableTwoFactorAuthenticationBackupCodes;
    }

    @Transient
    public boolean isTwoFactorAuthenticationEnabled() {
        return !isEmpty(getTwoFactorAuthenticationSecretKey());
    }

    @Type(type = HibernateInstantType.TYPE)
    public Instant getLastPaymentChargebackDatetime() {
        return lastPaymentChargebackDatetime;
    }

    public void setLastPaymentChargebackDatetime(Instant lastPaymentChargebackDatetime) {
        this.lastPaymentChargebackDatetime = lastPaymentChargebackDatetime;
    }

    @Transient
    public boolean isHasReceivedPaymentChargeback() {
        return getLastPaymentChargebackDatetime() != null;
    }

    @Transient
    public boolean isActive() {
        return getUserStatus().isThis(UserStatus.ACTIVE);
    }

    @Transient
    public boolean isVisible() {
        // bl: users are visible if they are active and also when deactivated (had previously revoked agreement to the TOS)
        return isActive() || isDeactivated();
    }

    @Transient
    public boolean isDeactivated() {
        return getUserStatus().isThis(UserStatus.DEACTIVATED);
    }

    @Transient
    public boolean isDeleted() {
        return getUserStatus().isThis(UserStatus.DELETED);
    }

    @Transient
    public boolean isSuspendAllEmails() {
        // suspend emails for non-active users
        if (!isActive()) {
            return true;
        }

        // suspend emails for users who chose to manually suspend emails
        return preferences.isSuspendAllEmails();
    }

    @Transient
    public boolean isPendingEmailVerification() {
        return !getUserFields().isEmailVerified();
    }

    @Transient
    public boolean isSpider() {
        return false;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = UserAuth.FIELD__USER__NAME)
    @Cascade({CascadeType.REMOVE})
    @MapKey(name = UserAuth.FIELD__AUTH_PROVIDER__NAME)
    @OrderBy(UserAuth.FIELD__AUTH_PROVIDER__NAME)
    public Map<AuthProvider, UserAuth> getUserAuths() {
        return userAuths;
    }

    public void setUserAuths(Map<AuthProvider, UserAuth> userAuths) {
        this.userAuths = userAuths;
    }

    public UserAuth addUserAuth(AuthProvider authProvider, String identifier) {
        UserAuth userAuth = new UserAuth(this, authProvider, identifier);
        getUserAuths().put(authProvider, userAuth);
        UserAuth.dao().save(userAuth);

        return userAuth;
    }

    @Transient
    public UserAuth getUserAuthForAuthProvider(AuthProvider authProvider) {
        Map<AuthProvider, UserAuth> userAuths = getUserAuths();
        return userAuths.get(authProvider);
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = FollowedChannel.FIELD__FOLLOWER__NAME)
    @Cascade({CascadeType.REMOVE})
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    public List<FollowedChannel> getFollowedChannels() {
        return followedChannels;
    }

    public void setFollowedChannels(List<FollowedChannel> followedChannels) {
        this.followedChannels = followedChannels;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = ChannelUser.Fields.user)
    @Cascade({CascadeType.REMOVE})
    @MapKey(name = ChannelUser.Fields.channel)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Map<Channel, ChannelUser> getChannelUsers() {
        return channelUsers;
    }

    public void setChannelUsers(Map<Channel, ChannelUser> channelUsers) {
        this.channelUsers = channelUsers;
    }

    @Transient
    public int getCountOwnedPublications() {
        return Publication.dao().getCountOwnedPublications(this);
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = WatchedUser.FIELD__WATCHED_USER__NAME)
    @Cascade({CascadeType.REMOVE})
    public List<WatchedUser> getUsersWatching() {
        return usersWatching;
    }

    public void setUsersWatching(List<WatchedUser> usersWatching) {
        this.usersWatching = usersWatching;
    }

    @Transient
    public WatchedUser getWatchedUserForCurrentUserWatchedByThisUser() {
        if (!networkContext().isLoggedInUser()) {
            return null;
        }
        User currentUser = networkContext().getUser();
        // for simplicity, if this is the current user then there is no WatchedUser, you cannot watch yourself.
        if (isEqual(currentUser, this)) {
            return null;
        }

        return WatchedUser.dao().getForUserWatchingUser(this, currentUser);
    }

    @Transient
    public WatchedUser getWatchedUserForCurrentUserWatchingThisUser() {
        if (!networkContext().isLoggedInUser()) {
            return null;
        }
        User currentUser = networkContext().getUser();
        // for simplicity, if this is the current user then there is no WatchedUser, you cannot watch yourself.
        if (isEqual(currentUser, this)) {
            return null;
        }

        return WatchedUser.dao().getForUserWatchingUser(currentUser, this);
    }

    @Transient
    public Boolean getWatchedByCurrentUser() {
        // bl: if the current user isn't signed in, then short-circuit here.
        if (!networkContext().getPrimaryRole().isRegisteredUser()) {
            return null;
        }

        if(watchedByCurrentUser == null) {
            PartitionGroup.getCurrentPartitionGroup().performSingleInstanceJitSafetyChecks(HAS_JIT_INITED_FOLLOWED_USER_PROPERTY);

            setCurrentUserFollow(getWatchedUserForCurrentUserWatchingThisUser());
        }

        return watchedByCurrentUser;
    }

    public void setWatchedByCurrentUser(boolean watchedByCurrentUser) {
        this.watchedByCurrentUser = watchedByCurrentUser;
    }

    @Transient
    public void setCurrentUserFollow(WatchedUser currentUserFollow) {
        setWatchedByCurrentUser(exists(currentUserFollow) && !currentUserFollow.isBlocked());
    }

    @Transient
    public boolean isBlockingCurrentUser() {
        WatchedUser watchedUser = getWatchedUserForCurrentUserWatchedByThisUser();

        return exists(watchedUser) && watchedUser.isBlocked();
    }

    @Transient
    public AreaUser getLoneAreaUser() {
        AreaUser areaUser = getAreaUserByArea(getAuthZone().getArea());

        assert areaUser != null : "Should always find a lone AreaUser! user/" + getOid() + " cachedAreaUserOid/" + AreaUser.dao().getAreaUserOidFromUserAndAreaCache(getOid(), getAuthZone().getArea().getOid()) + " (0 means NULL_OID).";
        return areaUser;
    }

    @Transient
    public SandboxedAreaUser getSandboxedAreaUser() {
        // bl: worth some explaining. just make sure that the current area matches the AuthZone's area.
        assert areaContext() != null && isEqual(areaContext().getArea(), getAuthZone().getArea()) : "Should only attempt to get the SandboxedAreaUser when the area is already in scope!";
        return AreaUser.getAreaUserRlm(getLoneAreaUser()).getSandboxedAreaUser();
    }

    @Transient
    public String getSuspendAllEmailsUrl() {
        return ReactRoute.UNSUBSCRIBE.getUrl(
                getOid().toString()
                , getEmailAddress()
                , SuspendUserEmailsTask.getAuthKeyForSuspendAllEmails(this)
        );
    }

    @Transient
    public String getProfileKeywords() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDisplayNameResolved());

        // Append username
        sb.append(" ").append(getUsername());
        return sb.toString();
    }

    @Transient
    public String getPendingRegistrationMessage(boolean forRegistration) {
        assert isPendingEmailVerification() : "Should never attempt to get the pending registration message for a user that is not pending in some way (plan limit, parental approval, moderation, email unverified)";

        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("<div class=\"descriptionParagraph\">");
        errorMessage.append(wordlet(forRegistration ? "user.pendingRegistrationReasons" : "user.pendingReasons"));
        errorMessage.append("</div><div class=\"descriptionParagraph\"><ul class=\"pendingUserReasons\">");
        errorMessage.append("<li>");
        errorMessage.append(wordlet("user.emailVerificationRequired"));
        errorMessage.append("</li>");
        errorMessage.append("</ul></div>");

        return errorMessage.toString();
    }

    @Override
    @Transient
    public String getDisplayUrl() {
        return getProfileUrl();
    }

    @Override
    @Transient
    public String getPermalinkUrl() {
        return getProfileUrl();
    }

    @Transient
    @Override
    public String getRoleStringForLogging() {
        StringBuilder sb = new StringBuilder();
        sb.append("User/").append(getUniqueName()).append("/").append(getOid());
        return sb.toString();
    }

    // jw: Unlike the method above, this method only returns methods that are enabled on their account, so if they disable email, it will not be included.
    private Set<InstantNotificationDeliveryMethod> deliveryMethodsForInstantNotification;

    @Transient
    public Set<InstantNotificationDeliveryMethod> getDeliveryMethodsForInstantNotification() {
        if (deliveryMethodsForInstantNotification == null) {
            // jw: we want to include only options that are both selected, and available, so lets get the intersection of those two sets.
            Set<InstantNotificationDeliveryMethod> methods = EnumSet.noneOf(InstantNotificationDeliveryMethod.class);

            // jw: for narrative, let's only support email notifications!
            // jw: followup: we should still honor their preference to suspend all emails.
            if (!isSuspendAllEmails()) {
                methods.add(InstantNotificationDeliveryMethod.EMAIL);
            }
            deliveryMethodsForInstantNotification = Collections.unmodifiableSet(methods);
        }

        return deliveryMethodsForInstantNotification;
    }

    public <T extends NetworkTaskImpl & MobilePushNotificationTask> void sendInstantNotification(T task) {
        // jw: Iterate through the InstantNotificationDeliveryMethods that the user has selected to receive instant notifications by.
        for (InstantNotificationDeliveryMethod method : getDeliveryMethodsForInstantNotification()) {
            method.sendInstantNotification(task, this);
        }
    }

    @Transient
    public Map<GlobalSecurable, PermissionDTO> getGlobalPermissions() {
        assert isCurrentUserThisUser() : "Should only get permissions for a User when they are the current user!";
        Map<GlobalSecurable, PermissionDTO> ret = new LinkedHashMap<>();
        for (GlobalSecurable securable : GlobalSecurable.NARRATIVE_PERMISSIONS) {
            ret.put(securable, securable.getPermissionDtoForAreaRole(getLoneAreaRole()));
        }
        return ret;
    }

    @Transient
    public Map<NarrativePermissionType, UserPermission> getNarrativePermissions() {
        assert isCurrentUserThisUser() : "Should only get permissions for a User when they are the current user!";
        Map<NarrativePermissionType, UserPermission> ret = new LinkedHashMap<>();
        for (NarrativePermissionType permission : NarrativePermissionType.values()) {
            ret.put(permission, permission.getUserPermissionForAreaRole(getLoneAreaRole()));
        }
        return ret;
    }

    @Transient
    public UserReputation getReputation() {
        return getReputationResolved(true);
    }

    @Transient
    public UserReputation getReputationWithoutCache() {
        return getReputationResolved(false);
    }

    @Transient
    public int getVotePoints(Referendum forReferendum) {
        if (forReferendum.getType().isTribunalReferendum()) {
            return UserReputation.MAX_POINTS_PER_VOTE;
        }

        return getReputationAdjustedVotePoints();
    }

    private UserReputation getReputationResolved(boolean useCache) {
        // bl: if the user is deleted, then don't include any reputation!
        if(isDeleted()) {
            return null;
        }
        UserReputation userReputation = UserReputation.dao().get(getOid());
        if(!exists(userReputation)) {
            // if the user doesn't have a UserReputation entity yet, create a default transient instance
            // with default values.
            userReputation = UserReputation.builder().userOid(getOid()).build();
        } else {
            // if we found a UserReputation and we don't want to load from the cache, then refresh
            // the UserReputation object so that we have fresh data. this is necessary when calculating
            // vote points, where we need to make sure everything is as up-to-date as possible.
            if(!useCache) {
                UserReputation.dao().refresh(userReputation);
            }
        }
        return userReputation;
    }

    @Transient
    @Override
    public int getReputationAdjustedVotePoints() {
        // bl: for users, the reputation-adjusted vote points come from the Reputation, of course
        return getReputationWithoutCache().getAdjustedVotePoints();
    }

    public ConductStatusEvent createConductStatusEvent(Instant eventTimestamp, ConductEventType type) {
        assert type != null : "Should always specify a ConductEventType when calling this method!";

        return ConductStatusEvent.builder()
                .userOid(getOid().getValue())
                .conductEventType(type)
                .eventTimestamp(eventTimestamp)
                .build();
    }

    public NegativeQualityEvent createNegativeQualityEvent(Instant eventTimestamp, NegativeQualityEventType type) {
        assert type != null : "Should always specify a NegativeQualityEventType when calling this method!";

        return NegativeQualityEvent.builder()
                .userOid(getOid().getValue())
                .negativeQualityEventType(type)
                .eventTimestamp(eventTimestamp)
                .build();
    }

    @Transient
    public boolean isCanMakeFiatPayments() {
        // jw: note: doing this test first because it plays nicer with tests.
        // jw: as soon as a user has a chargeback we will no longer allow them to use fiat payments.
        if (isHasReceivedPaymentChargeback()) {
            return false;
        }

        return getAuthZone().isFiatPaymentsEnabled();
    }

    public static final Comparator<User> DISPLAY_NAME_COMPARATOR = (o1, o2) -> {
        int ret = o1.getDisplayNameResolved().compareToIgnoreCase(o2.getDisplayNameResolved());
        if (ret != 0) {
            return ret;
        }
        return OID.compareOids(o1.getOid(), o2.getOid());
    };

    @Transient
    public UserKyc getUserKyc() {
        return UserKyc.dao().get(getOid());
    }

    /**
     * Get the permitted age ratings for this user.  See also #getPreferredAgeRatings
     */
    @Transient
    public Set<AgeRating> getPermittedAgeRatings() {
        return getUserKyc().getPermittedAgeRatings();
    }

    @Transient
    public UserAgeStatus getUserAgeStatus() {
        return UserAgeStatus.getForUserKyc(getUserKyc());
    }

    public static NrveValue getMinimumRedemptionNrveValue() {
        return GlobalSettingsUtil.getGlobalSettings().getNrveValue(REDEMPTION_MINIMUM_IN_USD, 2, RoundingMode.UP);
    }

    private transient UserRedemptionStatus redemptionStatus;

    @Transient
    public UserRedemptionStatus getRedemptionStatus() {
        if (redemptionStatus == null) {
            redemptionStatus = getRedemptionStatusInternal();
        }

        return redemptionStatus;
    }

    @Transient
    private UserRedemptionStatus getRedemptionStatusInternal() {
        // jw: they need to have a neo wallet.
        if (!exists(getWallet().getNeoWallet())) {
            return UserRedemptionStatus.WALLET_UNSPECIFIED;
        }

        assert getLastWalletAddressChangeDatetime() != null : "If the user has a neo wallet, then their last wallet address change datetime should always have a value!";

        // jw: the NEO wallet should have been set long enough to consider it safe to use.
        if (getLastWalletAddressChangeDatetime().isAfter(Instant.now().minus(NEO_ADDRESS_WAITING_PERIOD))) {
            return UserRedemptionStatus.WALLET_IN_WAITING_PERIOD;
        }

        // jw: let's make sure they do not already have a transaction pending
        List<WalletTransaction> transactions = WalletTransaction.dao().getForFromWalletAndStatusAndType(
                getWallet(),
                WalletTransactionStatus.PENDING,
                WalletTransactionType.USER_REDEMPTION
        );

        if (!transactions.isEmpty()) {
            return UserRedemptionStatus.HAS_PENDING_REDEMPTION;
        }

        // jw: since they passed all tests above then they can use this address.
        return UserRedemptionStatus.REDEMPTION_AVAILABLE;
    }

    private transient BigDecimal totalRedemptionsThisYear;

    @Transient
    public BigDecimal getTotalRedemptionAmountForCurrentYear() {
        if (totalRedemptionsThisYear==null) {
            totalRedemptionsThisYear = WalletTransaction.dao().getTotalUsdForNonPendingFromWalletAndTypeAfter(
                    getWallet(),
                    EnumSet.of(WalletTransactionType.USER_REDEMPTION),
                    // jw: let's include all transactions since the start of the year.
                    LocalDate.now(ZoneOffset.UTC).with(TemporalAdjusters.firstDayOfYear()).atStartOfDay().toInstant(ZoneOffset.UTC)
            );
        }

        return totalRedemptionsThisYear;
    }

    public void updateNeoWallet(NeoWallet neoWallet) {
        assert neoWallet == null || neoWallet.getType().isUser() : "Should only ever update a users neo wallet with a USER NeoWallet!";

        NeoWallet originalWallet = getWallet().getNeoWallet();

        // jw: first, update the neoWallet on the users Wallet
        getWallet().setNeoWallet(neoWallet);

        // jw: next, let's update the last wallet change datetime.
        setLastWalletAddressChangeDatetime(Instant.now());

        // jw: finally, let's clear the cached neoWalletStatus so that a new one will get generated if necessary.
        redemptionStatus = null;

        boolean hasOriginalWallet = exists(originalWallet);

        // bl: delete the original NeoWallet if we can
        // jw: Okay, if the user had a previous neoWallet we have a bit more work to do before we are fully done.
        if (hasOriginalWallet) {
            // jw: now that the neo wallet is no longer associated to this user, we can check to see if it can be removed.
            if (canNeoWalletBeRemoved(originalWallet)) {
                NeoWallet.dao().delete(originalWallet);
            }
        }

        // jw: let's email the user and let them know about this change to their account.
        areaContext().doAreaTask(new SendUserNeoWalletChangedEmail(getUser(), hasOriginalWallet));
    }

    private static boolean canNeoWalletBeRemoved(NeoWallet neoWallet) {
        // jw: first, if the wallet has any transactions then we need to keep it around.
        if (NeoTransaction.dao().isDoesWalletHaveTransactions(neoWallet)) {
            return false;
        }

        // jw: next: if the wallet is in use by another user we need to keep it around.
        if (Wallet.dao().isNeoWalletInUse(neoWallet)) {
            return false;
        }

        // jw: guess we are clear to remove it.
        return true;
    }

    @Transient
    public Instant getNeoWalletWaitingPeriodEndDatetime() {
        // jw: if the users status is anything but pending waiting period then return null.
        if (!getRedemptionStatus().isWalletInWaitingPeriod()) {
            return null;
        }

        assert getLastWalletAddressChangeDatetime() != null : "Should always have a wallet change datetime set by now!";

        return getLastWalletAddressChangeDatetime().plus(NEO_ADDRESS_WAITING_PERIOD);
    }

    /**
     * Derive the filtered set of permitted age ratings for this user based on their preferences.  This is the
     * appropriate place to incorporate filtering of what age ratings are available to this user vs. what the user's
     * preferences indicate they would like to see/not see.
     */
    @Transient
    public Set<AgeRating> getPreferredAgeRatings() {
        Set<AgeRating> permittedAgeRatings = getPermittedAgeRatings();
        // if the user can't see restricted content, nothing further to filter.
        if(!AgeRating.ageRatingsContainRestricted(permittedAgeRatings)) {
            return permittedAgeRatings;
        }
        // if the user can see restricted content but has chosen not to, then filter restricted out.
        if(!getPreferences().isDisplayAgeRestrictedContent()) {
            // these sets are immutable, so just return the appropriate set directly.
            return AgeRating.ALL_AUDIENCES_ONLY;
        }
        return permittedAgeRatings;
    }

    @Transient
    public boolean isEligibleForPublicationDiscount() {
        // bl: you are eligible for a discount as long as the expiration date hasn't passed and you're on the wait list
        return PublicationWaitListEntry.isAreWaitListDiscountsAllowedForNewPublications() &&
                PublicationWaitListEntry.dao().isUserEligibleForDiscount(this);
    }

    @Transient
    public List<Object> getObjectsForSecurityToken() {
        // bl: use user OID, email address OID, current email address, and UserAuth OID for security tokens.
        List<Object> ret = new LinkedList<>();
        ret.add(getUser().getOid());
        ret.add(getUserFields().getEmailAddress().getOid());
        ret.add(getEmailAddress());
        ret.add(getUserAuthForAuthProvider(AuthProvider.SANDBOXED_AREA).getOid());
        return ret;
    }

    public static UserDAO dao() {
        return DAOImpl.getDAO(User.class);
    }
}
