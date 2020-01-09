package org.narrative.network.core.user;

import com.google.common.annotations.VisibleForTesting;
import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.persistence.hibernate.StringEnumType;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.fileondisk.base.FileBaseType;
import org.narrative.network.core.fileondisk.base.services.filesystem.NetworkPath;
import org.narrative.network.core.locations.Country;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.user.dao.UserKycDAO;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * KYC details for a user
 */
@Data
@FieldNameConstants
@NoArgsConstructor
@Entity
@Proxy
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {UserKyc.FIELD__USER_DETAIL_HASH__NAME}, name = "userKyc_userDetailHash_uidx")}
)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserKyc implements DAOObject<UserKycDAO> {
    public static final String FIELD__USER_DETAIL_HASH__NAME = "userDetailHash";

    public static final int MINIMUM_AGE_US = 13;
    public static final int MINIMUM_AGE_WORLD = 16;

    public UserKyc(User user) {
        this.oid = user.getOid();
        this.user = user;
    }

    @Id
    private OID oid;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_userKyc_user")
    @PrimaryKeyJoinColumn
    private User user;

    @UpdateTimestamp
    private Instant lastUpdated;

    /**
     * User's current KYC status
     */
    @Type(type = IntegerEnumType.TYPE)
    @Column(columnDefinition = "tinyint")
    private UserKycStatus kycStatus = UserKycStatus.NONE;

    /**
     * Birth month of user
     */
    @Column(columnDefinition = "tinyint")
    private Integer birthMonth;

    /**
     * Birth year of user
     */
    @Column(columnDefinition = "smallint")
    private Integer birthYear;

    /**
     * Country of user
     */
    @Type(type = StringEnumType.TYPE)
    @Column(columnDefinition = Country.ENUM_FIELD_TYPE)
    private Country country;

    /**
     * SHA1 Hash of full name, country, birth date and document id
     */
    @Column(columnDefinition = "varchar(128)")
    private String userDetailHash;

    /**
     * Encryption salt used for encrypting this user's files on Google Cloud Storage
     */
    private byte[] encryptionSalt;

    /**
     * The count of submissions this user has made. Needed for filename generation.
     */
    private Integer submissionCount;

    /**
     * Event history
     */
    @VisibleForTesting
    @Setter(value = AccessLevel.PROTECTED)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = UserKycEvent.FIELD__USERKYC__NAME, cascade = CascadeType.ALL)
    @OrderBy(UserKycEvent.FIELD__CREATED__NAME)
    private List<UserKycEvent> events;

    /**
     * Eligible to be submitted? i.e. paid and waiting for submission
     */
    public boolean isEligibleForSubmission() {
        return getKycStatus().isReadyForVerification();
    }

    public void addEvent(UserKycEvent userKycEvent) {
        List<UserKycEvent> eventList = getEvents();
        if (eventList == null) {
            eventList = new ArrayList<>();
            setEvents(eventList);
        }
        getEvents().add(userKycEvent);
    }

    @Transient
    public InvoiceType getInvoiceType() {
        // jw: if the user cannot start certification, or they cannot use fiat payments
        if (!getKycStatus().isStartCheckEligible() || !getUser().isCanMakeFiatPayments()) {
            return null;
        }

        return InvoiceType.KYC_CERTIFICATION;
    }

    @VisibleForTesting
    LocalDate getNow() {
        return LocalDate.now();
    }

    /**
     * Calculate the user's age in years as of the start of the previous month
     *
     * @return int value indicating the user's age
     */
    public Integer getAgeInYears() {
        // bl: if the user isn't KYC certified, then their age is unknown so return null
        if (!getKycStatus().isApproved()) {
            return null;
        }

        if (getBirthYear() == null || getBirthMonth() == null) {
            throw UnexpectedError.getRuntimeException("User is KYC approved but has no birth month and/or year!  UserOID: " + getOid());
        }

        // (First day of previous month) - (birth year/month at first day of month) == (age in years)
        // This lines up with the spec which says that someone becomes 18 on the first day of the month *following*
        // the month of their 18th birthday.  KYC only hangs on to the year/month so this is a necessary approximation.
        LocalDate compareNowDate = getNow().withDayOfMonth(1).minusMonths(1);
        LocalDate userBirthdate = LocalDate.of(getBirthYear(), getBirthMonth(), 1);

        long ageInYears = ChronoUnit.YEARS.between(userBirthdate, compareNowDate);

        // bl: if the age is somehow zero or negative, then treat the user as unknown
        return ageInYears > 0 ? (int)ageInYears : null;
    }

    @Transient
    public Set<AgeRating> getPermittedAgeRatings() {
        return AgeRating.getPermittedRatingsForAge(getAgeInYears());
    }

    public NetworkPath getNetworkPath(KycImageType imageType) {
        assert getSubmissionCount()!=null : "Should only call this method after a submission count is set!";
        return getNetworkPath(imageType, getSubmissionCount());
    }

    public NetworkPath getNetworkPath(KycImageType imageType, int iteration) {
        return new NetworkPath(FileBaseType.KYC_IMAGE, getOid(), getFilenameBase(imageType, iteration));
    }

    public String getFilenameBase(KycImageType imageType) {
        assert getSubmissionCount()!=null : "Should only call this method after a submission count is set!";
        return getFilenameBase(imageType, getSubmissionCount());
    }

    public String getFilenameBase(KycImageType imageType, int iteration) {
        return iteration + "-" + imageType.getFilenameBase();
    }

    public static UserKycDAO dao() {
        return DAOImpl.getDAO(UserKyc.class);
    }
}
