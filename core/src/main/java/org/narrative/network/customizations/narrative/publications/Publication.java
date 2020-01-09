package org.narrative.network.customizations.narrative.publications;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.content.base.SEOObject;
import org.narrative.network.core.content.base.services.CreateContentTask;
import org.narrative.network.core.fileondisk.base.services.filesystem.NetworkPath;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.fileondisk.image.ImageType;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.propertyset.area.AreaPropertySet;
import org.narrative.network.core.propertyset.base.PropertySetType;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeUtil;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.customizations.narrative.publications.dao.PublicationDAO;
import org.narrative.network.customizations.narrative.services.GoogleCloudStorageFileHandler;
import org.narrative.network.customizations.narrative.services.ReactRoute;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.util.NetworkConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-07-23
 * Time: 10:11
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {
        @UniqueConstraint(name="publication_prettyUrlString_uidx", columnNames = {Publication.FIELD__PRETTY_URL_STRING__COLUMN})
})
public class Publication implements DAOObject<PublicationDAO>, ChannelConsumer, SEOObject {
    private static final String HAS_JIT_INITED_CURRENT_USER_ROLES_PROPERTY = Channel.class.getName() + "-HasJITInitedCurrentUserRoles";

    public static final String FIELD__NAME__NAME = "name";
    public static final String FIELD__NAME__COLUMN = FIELD__NAME__NAME;

    public static final String FIELD__PRETTY_URL_STRING__NAME = "prettyUrlString";
    public static final String FIELD__PRETTY_URL_STRING__COLUMN = FIELD__PRETTY_URL_STRING__NAME;

    // jw:: Duration does not support years directly because anything over days is considered an estimated scale.
    public static final Period PLAN_PERIOD = Period.ofYears(1);

    public static final Period TRIAL_PERIOD = Period.ofMonths(6);
    // jw: the renewal period starts 30 days before the expiration of the current endDatetime;
    public static final Duration RENEWAL_DURATION = Duration.ofDays(30);
    // jw: how long should we keep non-active publications around before we permanently remove them.
    public static final Duration NON_ACTIVE_DURATION = Duration.ofDays(30);

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @NotNull
    @Length(min = ChannelConsumer.MIN_NAME_LENGTH, max = ChannelConsumer.MAX_NAME_LENGTH)
    private String name;

    @Length(min = ChannelConsumer.MIN_DESCRIPTION_LENGTH, max = ChannelConsumer.MAX_DESCRIPTION_LENGTH)
    private String description;

    @NotNull
    @Type(type=IntegerEnumType.TYPE)
    private PublicationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_publication_owner")
    private User owner;

    @NotNull
    @Length(min = NetworkConstants.MIN_PRETTY_URL_STRING_LENGTH, max = NetworkConstants.MAX_PRETTY_URL_STRING_LENGTH)
    private String prettyUrlString;

    // jw: due to how the Channel.oid is derived from the ChannelConsumer, we need to make this association optional so
    //     that we can create the consumer first, and then the Channel after. In practice, all Consumers should have a
    //     Channel associated with them.
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @JoinColumn(name = Publication.Fields.oid)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    private Channel channel;

    @NotNull
    @Type(type=IntegerEnumType.TYPE)
    private PublicationPlanType plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_publication_logo")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private ImageOnDisk logo;

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_publication_headerImage")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private ImageOnDisk headerImage;

    @NotNull
    @Type(type=HibernateInstantType.TYPE)
    private Instant creationDatetime;

    @NotNull
    @Type(type=HibernateInstantType.TYPE)
    private Instant endDatetime;

    @NotNull
    private PublicationContentRewardWriterShare contentRewardWriterShare;

    private PublicationContentRewardRecipientType contentRewardRecipient;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @ForeignKey(name = "fk_publication_settingsSet")
    private AreaPropertySet settingsSet;

    private transient PublicationSettings settings;

    private transient Set<PublicationPlanType> renewalPlans;
    private transient Set<PublicationPlanType> upgradePlans;
    private transient Set<PublicationPlanType> availablePlans;

    private transient Set<PublicationRole> currentUserRoles;

    public Publication(String name, String description, String prettyUrlString, PublicationPlanType plan, User owner) {
        assert !isEmpty(name) : "The name should always be provided!";
        assert plan!=null : "We should always be provided with a plan!";
        assert exists(owner) : "Publications should always be created by a user, who inherently is the owner!";

        this.name = name;
        this.description = description;
        this.prettyUrlString = prettyUrlString;
        this.plan = plan;
        this.owner = owner;

        creationDatetime = Instant.now();
        // jw: let's consider this the start of their trial and set their end datetime accordingly.
        endDatetime = creationDatetime.atOffset(RewardUtils.REWARDS_ZONE_OFFSET).plus(TRIAL_PERIOD).toInstant();
        contentRewardWriterShare = PublicationContentRewardWriterShare.ONE_HUNDRED_PERCENT;
        settingsSet = new AreaPropertySet(PropertySetType.getPropertySetTypeByInterface(PublicationSettings.class).getDefaultPropertySet());
        status = PublicationStatus.ACTIVE;
    }

    public void updateName(String name) {
        // jw: if the name matches the current name just short out.
        if (isEqual(getName(), name)) {
            return;
        }

        setName(name);
        // generate a new prettyUrlString for this niche based on its name. note that the current ID can be re-used
        // if the new name results in the same ID.
        String prettyUrlString = CreateContentTask.getPrettyUrlStringValue(Publication.dao(), this, null, null, null, name);
        setPrettyUrlString(prettyUrlString);
    }

    public String getNameForHtml() {
        return HtmlTextMassager.disableHtml(getName());
    }

    public Timestamp getEndDatetimeTimestamp() {
        return new Timestamp(getEndDatetime().toEpochMilli());
    }

    public Instant getDeletionDatetime() {
        return getEndDatetime().plus(NON_ACTIVE_DURATION);
    }

    public Instant getDeletionDatetimeWhenExpired() {
        if (getStatusResolved().isExpired()) {
            return getDeletionDatetime();
        }

        return null;
    }

    public String getIdForUrl() {
        if (isEmpty(getPrettyUrlString())) {
            return "_"+getOid();
        }

        return getPrettyUrlString();
    }

    public String getDisplayUrl() {
        // jw: if the publication has a primary domain then we should be using that instead of the react route.
        if (exists(getChannel().getPrimaryDomain())) {
            return "https://"+getChannel().getPrimaryDomain().getDomainName();
        }

        // jw: otherwise this is pretty straight forward, just use the ReactRoute with the idForUrl.
        return ReactRoute.PUBLICATION_DETAILS.getUrl(getIdForUrl());
    }

    public String getManagePowerUsersUrl() {
        return ReactRoute.PUBLICATION_MANAGE_POWER_USERS.getPublicationUrl(this);
    }

    public String getManageReviewQueueUrl() {
        return ReactRoute.PUBLICATION_MANAGE_REVIEW_QUEUE.getPublicationUrl(this);
    }

    public String getPowerUserInvitationUrl() {
        return ReactRoute.PUBLICATION_POWER_USER_INVITATION.getPublicationUrl(this);
    }

    @Override
    public String getPermalinkUrl() {
        throw UnexpectedError.getRuntimeException("This should never be used for Publications!");
    }

    public Instant getRenewalPeriodStartDatetime() {
        return getEndDatetime().minus(RENEWAL_DURATION);
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.PUBLICATION;
    }

    public PublicationSettings getSettings() {
        if (settings == null) {
            settings = PropertySetTypeUtil.getPropertyWrapper(PublicationSettings.class, getSettingsSet());
        }
        return settings;
    }

    public String getLogoUrl() {
        return getImageUrl(getLogo(), ImageType.LARGE_SQUARE_THUMBNAIL);
    }

    public String getHeaderImageUrl() {
        return getImageUrl(getHeaderImage(), ImageType.LARGE);
    }

    private String getImageUrl(ImageOnDisk image, ImageType imageType) {
        if (!exists(image)) {
            return null;
        }
        // jw: if we are aiming to render the large square thumbnail, but the squareThumbnail is the largest we have lets
        //     go ahead and fall back to the Square Thumbnail.
        if (imageType.isLargeSquareThumbnail()) {
            // jw: let's just return the primarySquareAvatarImageUrl as defined by ImageOnDisk, in case the primary image is too small for large.
            return image.getPrimarySquareAvatarImageUrl();
        }

        NetworkPath networkPath = image.getNetworkPathForImageType(imageType);
        return GoogleCloudStorageFileHandler.IMAGES.getFileUri(networkPath);
    }

    public PublicationStatus getStatusResolved() {
        // jw: if there is an explicit non-active status set then use that.
        if (!getStatus().isActive()) {
            return getStatus();
        }

        // jw: otherwise, if the publication is past its endDatetime then they are expired.
        if (getEndDatetime().isBefore(Instant.now())) {
            return PublicationStatus.EXPIRED;
        }

        // jw: technically, this should always be ACTIVE, but I am going to use the status from the publication in case
        //     the logic above is ever updated or changed.
        return getStatus();
    }

    public boolean isInTrialPeriod() {
        return !exists(getChannel().getPurchaseInvoice());
    }

    public boolean isRenewable() {
        // jw: While the Publication is in a trial period we will consider all plans as renewable.
        if (isInTrialPeriod()) {
            return true;
        }

        // jw: otherwise, the publication can be renewed as long as its after the renewal period start datetime
        return getRenewalPeriodStartDatetime().isBefore(Instant.now());
    }

    @Override
    public User getChannelOwner() {
        return getOwner();
    }

    public boolean isCurrentRoleOwner() {
        return isAreaRoleOwner(areaContext().getAreaRole());
    }

    public void checkCurrentRoleOwner() {
        if(!isCurrentRoleOwner()) {
            throw new AccessViolation(wordlet("publication.accessViolation"));
        }
    }

    public boolean isAreaRoleOwner(AreaRole areaRole) {
        if (!areaRole.isActiveRegisteredAreaUser()) {
            return false;
        }

        return isEqual(getOwner(), areaRole.getPrimaryRole());
    }

    @Override
    public TribunalIssueType getPossibleTribunalIssueType() {
        // jw: because the lock datetime won't be set until the issue is resolved, this works out really well.
        if (!getChannel().isCanAppealStatus()) {
            return null;
        }

        return TribunalIssueType.RATIFY_PUBLICATION;
    }

    public boolean isCanCurrentUserAppeal() {
        // bl: you can't appeal a Publication you own
        if(isCurrentRoleOwner()) {
            return false;
        }

        // bl: otherwise, you can appeal if there is a possible issue type
        return getPossibleTribunalIssueType()!=null;
    }

    public void lockForInvoiceProcessing() {
        Publication.dao().refreshForLock(this);
    }

    public int getUserCountForRole(PublicationRole role) {
        assert role != null : "Should always be given a role here!";

        // jw: capturing this into a reference so that the generics can work their magic and the compiler will not complain.
        Map<PublicationRole, Integer> countLookup = getChannel().getUserCountsByRole();

        return countLookup.get(role);
    }

    public int getEditorLimit() {
        return PublicationRole.EDITOR.getPlanLimit(getPlan());
    }

    public int getWriterLimit() {
        return PublicationRole.WRITER.getPlanLimit(getPlan());
    }

    public List<User> getAdmins() {
        return getUsersByRole(PublicationRole.ADMIN);
    }

    public List<User> getEditors() {
        return getUsersByRole(PublicationRole.EDITOR);
    }

    public List<User> getWriters() {
        return getUsersByRole(PublicationRole.WRITER);
    }

    public List<User> getUsersByRole(PublicationRole publicationRole) {
        return getChannel().getUsersByRole().getOrDefault(publicationRole, Collections.emptyList());
    }

    public List<User> getInvitedAdmins() {
        return getChannel().getInvitedUsersByRole().getOrDefault(PublicationRole.ADMIN, Collections.emptyList());
    }

    public List<User> getInvitedEditors() {
        return getChannel().getInvitedUsersByRole().getOrDefault(PublicationRole.EDITOR, Collections.emptyList());
    }

    public List<User> getInvitedWriters() {
        return getChannel().getInvitedUsersByRole().getOrDefault(PublicationRole.WRITER, Collections.emptyList());
    }

    public int getFollowerCount() {
        return FollowedChannel.dao().getFollowerCount(getChannel());
    }

    private void buildPlanSets() {
        if (availablePlans == null) {
            Set<PublicationPlanType> renewals = EnumSet.noneOf(PublicationPlanType.class);
            Set<PublicationPlanType> upgrades = EnumSet.noneOf(PublicationPlanType.class);
            for (PublicationPlanType plan : PublicationPlanType.values()) {
                if (!plan.isAvailableToPublication(this)) {
                    continue;
                }

                if (isUpgrade(plan)) {
                    upgrades.add(plan);
                    continue;
                }

                // jw: note: while in trial all available plans will be considered "renewals"
                if (isRenewable()) {
                    renewals.add(plan);
                }
            }

            Set<PublicationPlanType> available = EnumSet.copyOf(upgrades);
            available.addAll(renewals);

            renewalPlans = Collections.unmodifiableSet(renewals);
            upgradePlans = Collections.unmodifiableSet(upgrades);
            availablePlans = Collections.unmodifiableSet(available);
        }
    }

    public Set<PublicationPlanType> getRenewalPlans() {
        buildPlanSets();

        return renewalPlans;
    }

    public Set<PublicationPlanType> getUpgradePlans() {
        buildPlanSets();

        return upgradePlans;
    }

    public Set<PublicationPlanType> getAvailablePlans() {
        buildPlanSets();

        return availablePlans;
    }

    public boolean isUpgrade(PublicationPlanType plan) {
        // jw: if the publication is still in trial then all plans are considered renewals, since they technically have not paid
        //     for a plan yet and we want to treat it like a renewal. This means that once they buy it their endDatetime
        //     will be calculated from the end of their trial if they are still within their trial period.
        if (!exists(getChannel().getPurchaseInvoice())) {
            return false;
        }

        return getPlan().isUpgrade(plan);
    }

    public Set<PublicationRole> getCurrentUserRoles() {
        // never need to initialize a value here for guests
        if (!networkContext().getPrimaryRole().isRegisteredUser()) {
            return Collections.emptySet();
        }
        if (currentUserRoles == null) {
            PartitionGroup.getCurrentPartitionGroup().performSingleInstanceJitSafetyChecks(HAS_JIT_INITED_CURRENT_USER_ROLES_PROPERTY);

            if (getStatusResolved().isActive()) {
                currentUserRoles = getUserRoles(networkContext().getUser());
            } else {
                currentUserRoles = Collections.emptySet();
            }
        }

        return currentUserRoles;
    }

    private Set<PublicationRole> getUserRoles(User user) {
        ChannelUser channelUser = ChannelUser.dao().getForChannelAndUser(getChannel(), user);

        return exists(channelUser)
                ? Collections.unmodifiableSet(channelUser.getPublicationRoles())
                : Collections.emptySet();
    }

    @Override
    public boolean isCanCurrentRolePost() {
        // jw: posting is only available while the publication is active.
        if (!getStatusResolved().isActive()) {
            return false;
        }
        return isCanCurrentRoleAccess(PublicationRole.WRITER);
    }

    public boolean isCanCurrentRoleAccess(PublicationRole role) {
        return getCurrentUserRoles().contains(role);
    }

    public void checkCurrentRoleAccess(PublicationRole role) {
        assert role!=null : "Should always specify a role when calling this method.";

        if (!isCanCurrentRoleAccess(role)) {
            throw new AccessViolation(wordlet("publication.accessViolation"));
        }
    }

    public boolean isDoesPrimaryRoleHaveAccess(PrimaryRole primaryRole, PublicationRole role) {
        if(!primaryRole.isRegisteredUser()) {
            return false;
        }
        return getUserRoles(primaryRole.getUser()).contains(role);
    }

    public Set<PublicationRole> getCurrentUserCanManageRoles() {
        // bl: the owner can always invite all other roles
        if(isCurrentRoleOwner()) {
            return EnumSet.allOf(PublicationRole.class);
        }
        // bl: if you aren't the owner, then you can only invite based on your roles
        return getCurrentUserRoles().stream().map(PublicationRole::getInviteRoles).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public Set<PublicationRole> getCurrentUserAllowedInviteRoles() {
        Set<PublicationRole> roles = getCurrentUserCanManageRoles();

        // jw: filter this down to just the roles that are not at their limit.
        return roles.stream().filter(role -> !role.isPlanLimitReached(this)).collect(Collectors.toSet());
    }

    public void checkCurrentRoleInviteAnyRoles() {
        if(getCurrentUserCanManageRoles().isEmpty()) {
            throw new AccessViolation(wordlet("publication.accessViolation"));
        }
    }

    public void checkCurrentRoleInviteRoles(Set<PublicationRole> roles) {
        assert !isEmptyOrNull(roles) : "Should never attempt a Publication role permission check without supplying at least one role!";

        // bl: make sure all of the roles being invited are supported by the current user's allowed invite roles
        if(!getCurrentUserCanManageRoles().containsAll(roles)) {
            throw new AccessViolation(wordlet("publication.accessViolation"));
        }
    }

    public void checkCurrentRoleAnyRole() {
        if(!isCurrentRoleOwner() && getCurrentUserRoles().isEmpty()) {
            throw new AccessViolation(wordlet("publication.accessViolation"));
        }
    }

    public boolean isEligibleForWaitListDiscount() {
        // bl: Publications are only eligible for the discount on the first invoice (i.e. in the trial period)
        // and also only if they were on the wait list.
        return isInTrialPeriod() && PublicationWaitListEntry.dao().isPublicationEligibleForDiscount(this);
    }

    public void assertNotExpired(boolean allowForOwnerWhenExpired) {
        PublicationStatus status = getStatusResolved();

        // jw: If the publication is active we can just short out.
        if (status.isActive()) {
            return;
        }

        assert status.isExpired() : "Do we have a new status type, because we expected expired, not/"+status;

        // jw: let's carve out an exception for the owner if requested.
        if (allowForOwnerWhenExpired && getOwner().isCurrentUserThisUser()) {
            return;
        }

        throw new ExpiredPublicationError(this);
    }

    public static PublicationDAO dao() {
        return NetworkDAOImpl.getDAO(Publication.class);
    }
}
