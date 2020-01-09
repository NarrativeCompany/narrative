package org.narrative.network.core.user;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateEnumSetType;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.NarrativeConstants;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.user.dao.EmailAddressDAO;
import org.narrative.network.customizations.narrative.services.ReactRoute;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-10
 * Time: 07:34
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
        @UniqueConstraint(name = "emailAddress_emailAddress_uidx", columnNames = {EmailAddress.FIELD__EMAIL_ADDRESS__COLUMN})
        ,@UniqueConstraint(name = "emailAddress_user_type_uidx", columnNames = {EmailAddress.FIELD__USER__COLUMN, EmailAddress.FIELD__TYPE__COLUMN})
})
public class EmailAddress implements DAOObject<EmailAddressDAO> {
    public static final String FIELD__EMAIL_ADDRESS__NAME = "emailAddress";
    public static final String FIELD__EMAIL_ADDRESS__COLUMN = FIELD__EMAIL_ADDRESS__NAME;

    public static final String FIELD__USER__NAME = "user";
    public static final String FIELD__USER__COLUMN = FIELD__USER__NAME + "_" + User.FIELD__OID__NAME;

    public static final String FIELD__TYPE__NAME = "type";
    public static final String FIELD__TYPE__COLUMN = FIELD__TYPE__NAME;

    public static final int MIN_EMAIL_ADDRESS_LENGTH = NarrativeConstants.MIN_EMAIL_ADDRESS_LENGTH;
    public static final int MAX_EMAIL_ADDRESS_LENGTH = NarrativeConstants.MAX_EMAIL_ADDRESS_LENGTH;

    private static final String EMAIL_CONFIRMATION_ID_PRIVATE_KEY = EmailAddress.class.getName() + "-EmailConfirmationPrivateKey-KxtGcHKnH*jA4X/zRqAx4Hr6LimYFEmaCqjVfvf)uWATaDyqvGXDqyGbnTHkx3uf";

    public static final Duration EMAIL_CHANGE_CONFIRMATION_WINDOW = Duration.ofDays(1);

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_emailAddress_user")
    private User user;

    @NotNull
    @Length(min = MIN_EMAIL_ADDRESS_LENGTH, max = MAX_EMAIL_ADDRESS_LENGTH)
    private String emailAddress;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private EmailAddressType type;

    @NotNull
    @Type(type = HibernateEnumSetType.TYPE, parameters = {@Parameter(name = HibernateEnumSetType.ENUM_CLASS, value = EmailAddressVerificationStep.TYPE)})
    private EnumSet<EmailAddressVerificationStep> verifiedSteps;

    @NotNull
    @Type(type = HibernateInstantType.TYPE)
    private Instant creationDatetime;

    public EmailAddress(User user, String emailAddress) {
        this.user = user;
        this.emailAddress = emailAddress;
        creationDatetime = Instant.now();

        boolean forRegistration = !exists(user);
        type = forRegistration
                ? EmailAddressType.PRIMARY
                : EmailAddressType.PENDING;
        verifiedSteps = forRegistration
                ? EnumSet.complementOf(EnumSet.of(EmailAddressVerificationStep.NEW_USER_STEP))
                : EnumSet.noneOf(EmailAddressVerificationStep.class);
    }

    public Instant getExpirationDatetime() {
        assert !getType().isPrimary() : "Should never use this method primary email address confirmations!";

        return getCreationDatetime().plus(EMAIL_CHANGE_CONFIRMATION_WINDOW);
    }

    public String getConfirmationId(EmailAddressVerificationStep verificationStep) {
        checkValidVerifiedEmailAddressType(verificationStep);

        List<Object> objectsForKey = getUser().getObjectsForSecurityToken();
        // jw: using the same format we were using from UserFields for the primary email address since there are registered
        //     users with their keys already generated from this.
        if (getType().isPrimary()) {
            objectsForKey.add(EMAIL_CONFIRMATION_ID_PRIVATE_KEY);
            return IPStringUtil.getMD5DigestFromObjects(objectsForKey);
        }

        // jw: since this is not account confirmation we can use a new format that includes the expiration
        // bl: add the new/pending email address in addition to the user's current email address that is already in objectsForKey
        objectsForKey.add(getEmailAddress());
        objectsForKey.add(getExpirationDatetime());
        objectsForKey.add(verificationStep.getPrivateKey());
        return IPStringUtil.getMD5DigestFromObjects(objectsForKey);
    }

    public String getConfirmationUrl(EmailAddressVerificationStep verificationStep) {
        checkValidVerifiedEmailAddressType(verificationStep);

        // jw: confirming the primary email address is a bit trickier, only because we need to maintain legacy confirmation ids and no time limit.
        if (getType().isPrimary()) {
            return ReactRoute.CONFIRM_EMAIL.getUrl(getUser().getOid().toString(), getConfirmationId(verificationStep));
        }

        return resolveEmailChangeReactRoute(ReactRoute.CONFIRM_EMAIL_CHANGE, verificationStep);
    }

    public String getCancelUrl(EmailAddressVerificationStep verificationStep) {
        return resolveEmailChangeReactRoute(ReactRoute.CANCEL_EMAIL_CHANGE, verificationStep);
    }

    private String resolveEmailChangeReactRoute(ReactRoute route, EmailAddressVerificationStep verificationStep) {
        assert !getType().isPrimary() : "Should never generate a email change URL for primary email address.";

        return route.getUrl(
                getUser().getOid().toString(),
                getOid().toString(),
                Integer.toString(verificationStep.getId()),
                getConfirmationId(verificationStep)
        );
    }

    public boolean isConfirmationIdValid(EmailAddressVerificationStep verificationStep, String confirmationId) {
        checkValidVerifiedEmailAddressType(verificationStep);

        // jw: first, if we don't have an ID then short out.
        if (isEmpty(confirmationId)) {
            return false;
        }

        // jw: next, if the ids don't match then short out.
        if (!IPStringUtil.isStringEqualIgnoreCase(confirmationId, getConfirmationId(verificationStep))) {
            return false;
        }

        // jw: finally, check the timeout, which does not apply when validating the primary email address
        if (!getType().isPrimary() && Instant.now().isAfter(getExpirationDatetime())) {
            return false;
        }

        return true;
    }

    private void checkValidVerifiedEmailAddressType(EmailAddressVerificationStep verificationStep) {
        if (verificationStep.isVerifyPending() && getType().isPrimary()) {
            throw UnexpectedError.getRuntimeException("Should never confirm the pending address against the primary EmailAddress object/"+getOid());
        }

    }

    public String getPrimaryEmailConfirmationUrl() {
        assert getType().isPrimary() : "This method should only be used on the primary email address!";
        assert !getVerifiedSteps().contains(EmailAddressVerificationStep.NEW_USER_STEP) : "This method should never be used when the NEW_USER_STEP is already verified.";

        return getConfirmationUrl(EmailAddressVerificationStep.NEW_USER_STEP);
    }

    public boolean isVerified() {
        return getVerifiedSteps().containsAll(EmailAddressVerificationStep.ALL);
    }

    public Set<EmailAddressVerificationStep> getIncompleteVerificationSteps() {
        return EnumSet.complementOf(getVerifiedSteps());
    }

    public static EmailAddressDAO dao() {
        return NetworkDAOImpl.getDAO(EmailAddress.class);
    }
}
