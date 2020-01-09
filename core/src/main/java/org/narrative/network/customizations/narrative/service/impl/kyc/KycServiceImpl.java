package org.narrative.network.customizations.narrative.service.impl.kyc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.locations.Country;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.NarrativeAuthZoneMaster;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.core.user.UserKycEvent;
import org.narrative.network.core.user.UserKycEventType;
import org.narrative.network.core.user.dao.UserKycDAO;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.KycService;
import org.narrative.network.customizations.narrative.service.api.model.UserKycDTO;
import org.narrative.network.customizations.narrative.service.api.model.kyc.KycIdentificationType;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.impl.kyc.email.KycEmailTaskFactory;
import org.narrative.network.customizations.narrative.service.mapper.UserKycMapper;
import org.narrative.network.shared.email.NetworkMailUtil;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;

@Service
@Validated
public class KycServiceImpl implements KycService {
    private static final NetworkLogger log = new NetworkLogger(KycServiceImpl.class);
    /**
     * States that allow a status change via API
     */
    static final Set<UserKycStatus> USER_KYC_PERMITTED_STATUS_CHANGE_SET = Sets.immutableEnumSet(UserKycStatus.NONE, UserKycStatus.APPROVED, UserKycStatus.REJECTED, UserKycStatus.REVOKED, UserKycStatus.READY_FOR_VERIFICATION);

    private final AreaTaskExecutor areaTaskExec;
    private final UserKycMapper userKycMapper;
    private final MessageSourceAccessor messageSourceAccessor;
    private final StaticMethodWrapper staticMethodWrapper;
    private final KycEmailTaskFactory kycEmailTaskFactory = new KycEmailTaskFactory();

    public KycServiceImpl(AreaTaskExecutor areaTaskExec, UserKycMapper userKycMapper, MessageSourceAccessor messageSourceAccessor, StaticMethodWrapper staticMethodWrapper) {
        this.userKycMapper = userKycMapper;
        this.areaTaskExec = areaTaskExec;
        this.messageSourceAccessor = messageSourceAccessor;
        this.staticMethodWrapper = staticMethodWrapper;
    }

    @VisibleForTesting
    UserKycDAO getUserKycDAO() {
        return UserKyc.dao();
    }

    @VisibleForTesting
    UserKyc getUserKycForUser(User user) {
        return user.getUserKyc();
    }

    @Override
    public UserKycDTO getKycStateForUser(OID userOid) {
        UserKyc userKyc = getUserKycDAO().get(userOid);
        return userKycMapper.mapUserKycEntityToUserKycDTO(userKyc);
    }

    @Override
    public UserKycDTO submitKycApplicant(User user, KycIdentificationType kycIdentificationType, @NotNull File livePhotoFile, @NotNull File documentFrontFile, File documentBackFile) {
        // Make sure this is a registered user
        staticMethodWrapper.checkRegisteredUser();

        //Make sure we're in the right state before continuing
        final UserKycDAO dao = getUserKycDAO();
        UserKyc userKyc = getUserKycForUser(user);

        // Lock on the UserKyc before modifying
        dao.lock(userKyc);

        // bl: clear out the details since these will all need to be set from the new documents
        userKyc.setUserDetailHash(null);
        userKyc.setCountry(null);
        userKyc.setBirthYear(null);
        userKyc.setBirthMonth(null);

        areaTaskExec.executeAreaTask(new UploadKycImagesTask(userKyc, kycIdentificationType, livePhotoFile, documentFrontFile, documentBackFile));

        userKyc.setKycStatus(UserKycStatus.AWAITING_METADATA);
        appendKycEventAndSendEmailIfNecessary(userKyc, UserKycEventType.SUBMITTED);

        // bl: send a notification email that there is a new certification submission
        sendCertificationNotificationEmail();

        return userKycMapper.mapUserKycEntityToUserKycDTO(userKyc);
    }

    @VisibleForTesting
    void sendCertificationNotificationEmail() {
        NetworkMailUtil.sendJavaCreatedEmail(
                null,
                NarrativeAuthZoneMaster.INSTANCE.getReplyToEmailAddress(),
                "Narrative Certifications",
                null,
                Collections.singleton(NetworkRegistry.getInstance().isProductionServer() ? "certifications@narrative.org" : NetworkRegistry.getInstance().getNarrativeProperties().getCluster().getDevOpsEmailAddress()),
                Collections.singleton("Certifications"),
                "New Certification Submission",
                "There is a new Certification submission to review: " + NetworkRegistry.getInstance().getNarrativeKycQueueUrl(),
                true,
                true,
                false
        );
    }

    @Override
    public void updateKycData(User user, DocCheckUserProps docCheckUserProps, String actorDisplayName) {
        //Make sure we're in the right state before continuing
        final UserKycDAO dao = getUserKycDAO();
        UserKyc userKyc = getUserKycForUser(user);

        // Lock on the UserKyc before modifying
        dao.lock(userKyc);

        Country country = docCheckUserProps.getCountry();
        // the minimum age is 16, unless you are in the US, in which case the minimum age is 13
        int minimumAge = country.isUnitedStates() ? UserKyc.MINIMUM_AGE_US : UserKyc.MINIMUM_AGE_WORLD;

        long age = ChronoUnit.YEARS.between(docCheckUserProps.getBirthDate(), LocalDate.now());

        // Reject underage users (anyone under 16; 13 in US)
        if (age < minimumAge) {
            // The user is underage according to the TOS.
            userKyc.setKycStatus(UserKycStatus.REJECTED);
            appendKycEventAndSendEmailIfNecessary(userKyc, UserKycEventType.USER_UNDERAGE, messageSourceAccessor.getMessage("KycService.kycUnderage"));
            log.error("Error - Underage user/" + userKyc.getOid() + " age/" + age + " country/" + country);
            return;
        }

        // Calculate the hash for identity details
        String hash = docCheckUserProps.calculateHash();

        // Make sure there is no other user with this hash
        UserKyc userKycByHash = dao.getByUserDetailHashCode(hash);
        if (userKycByHash != null && !userKycByHash.getOid().equals(userKyc.getOid())) {
            // Possible duplicate identity - reject
            userKyc.setKycStatus(UserKycStatus.REJECTED);
            appendKycEventAndSendEmailIfNecessary(userKyc, UserKycEventType.REJECTED_DUPLICATE, messageSourceAccessor.getMessage("KycService.kycDuplicateIdentityDetected"));
            log.error("Error - Duplicate check hash code matches an existing user other than the current user for user OID " + userKyc.getOid());
            return;
        }

        // Set details
        userKyc.setUserDetailHash(hash);
        userKyc.setCountry(country);
        userKyc.setBirthYear(docCheckUserProps.getBirthDate().getYear());
        userKyc.setBirthMonth(docCheckUserProps.getBirthDate().getMonthValue());

        userKyc.setKycStatus(UserKycStatus.IN_REVIEW);

        appendKycEventAndSendEmailIfNecessary(userKyc, UserKycEventType.DOCUMENT_METADATA_ENTERED, actorDisplayName);
    }

    @Override
    public void updateKycUserStatus(@NotNull UserKyc userKyc, @NotNull UserKycStatus newUserKycStatus, UserKycEventType eventType, String actorDisplayName, String note) {
        if (userKyc == null) {
            throw UnexpectedError.getRuntimeException("UserKyc is null");
        }
        if (!USER_KYC_PERMITTED_STATUS_CHANGE_SET.contains(newUserKycStatus)) {
            throw UnexpectedError.getRuntimeException("New UserKycStatus is not an eligible status for transition via updateKycUserStatus: " + newUserKycStatus);
        }

        areaTaskExec.executeNarrativePlatformAreaTask(new AreaTaskImpl<UserKyc>() {
            @Override
            protected UserKyc doMonitoredTask() {
                UserKycDAO dao = getUserKycDAO();

                // Lock on the UserKyc before modifying
                dao.lock(userKyc);

                // jw: let's get the users original status before we update it. This will allow us to control the handling below more intelligently.
                UserKycStatus originalStatus = userKyc.getKycStatus();

                // Set the new status
                userKyc.setKycStatus(newUserKycStatus);

                // Append the new event
                appendKycEventAndSendEmailIfNecessary(userKyc, eventType, actorDisplayName, note);

                // Create a ledger entry/reputation event and enqueue the event if warranted by the event type
                if (newUserKycStatus.isApproved()) {
                    createLedgerEntryAndEnqueueRepEvent(userKyc.getUser(), LedgerEntryType.KYC_CERTIFICATION_APPROVED);

                // jw: the following LedgerEntries should only be created if the user was approved previously.
                } else if (originalStatus.isApproved()) {
                    // jw: we only want to do this if the user was approved when the chargeback came in:
                    if (newUserKycStatus.isRevoked()) {
                        createLedgerEntryAndEnqueueRepEvent(userKyc.getUser(), LedgerEntryType.KYC_CERTIFICATION_REVOKED);
                    }
                    if (eventType.isRefunded()) {
                        createLedgerEntryAndEnqueueRepEvent(userKyc.getUser(), LedgerEntryType.KYC_REFUND);
                    }
                }

                return userKyc;
            }
        });
    }

    @VisibleForTesting
    void createLedgerEntryAndEnqueueRepEvent(User user, LedgerEntryType ledgerEntryType) {
        // Create a ledger entry/reputation event and enqueue the event
        LedgerEntry entry = new LedgerEntry(staticMethodWrapper.getAreaUserRlmFromUser(user), ledgerEntryType);
        areaTaskExec.executeGlobalTask(new SaveLedgerEntryTask(entry));
    }

    /**
     * Mutate the passed {@link UserKyc} by adding an event and sending a notification email if necessary
     */
    @VisibleForTesting
    void appendKycEventAndSendEmailIfNecessary(UserKyc userKyc, UserKycEventType eventType, String actorDisplayName, String note) {
        UserKycEvent event = UserKycEvent.builder()
                .userKyc(userKyc)
                .type(eventType)
                // jw: this column does not currently allow null values, so let's use something 'meaningful'.
                .actorDisplayName(actorDisplayName==null
                        ? messageSourceAccessor.getMessage("KycService.serverActorName")
                        : actorDisplayName)
                .note(note)
                .build();

        userKyc.addEvent(event);

        if (kycEmailTaskFactory.eventTypeSendsEmail(eventType)) {
            areaTaskExec.executeNarrativePlatformAreaTask(kycEmailTaskFactory.buildEmailTask(eventType, userKyc.getUser()));
        }
    }

    @VisibleForTesting
    void appendKycEventAndSendEmailIfNecessary(UserKyc userKyc, UserKycEventType eventType, String note) {
        appendKycEventAndSendEmailIfNecessary(userKyc, eventType, null, note);
    }


    @VisibleForTesting
    void appendKycEventAndSendEmailIfNecessary(UserKyc userKyc, UserKycEventType eventType) {
        appendKycEventAndSendEmailIfNecessary(userKyc, eventType, null);
    }

}
