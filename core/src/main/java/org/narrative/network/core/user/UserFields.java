package org.narrative.network.core.user;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Sep 24, 2010
 * Time: 8:24:56 AM
 *
 * @author brian
 */
@Embeddable
public class UserFields {

    private EmailAddress emailAddress;

    private Timestamp registrationDate;

    private Timestamp agreedToTosDatetime;

    public static final String FIELD__EMAIL_ADDRESS__NAME = "emailAddress";
    public static final String FIELD__EMAIL_ADDRESS__COLUMN = FIELD__EMAIL_ADDRESS__NAME + "_" + EmailAddress.FIELD__OID__NAME;

    /**
     * @deprecated for hibernate use only
     */
    public UserFields() {}

    public UserFields(boolean init) {
        if (init) {
            this.registrationDate = now();
        }
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_user_emailAddress")
    public EmailAddress getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(EmailAddress emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Transient
    public EmailAddress getPendingEmailAddress() {
        return EmailAddress.dao().getForUserAndType(getEmailAddress().getUser(), EmailAddressType.PENDING);
    }

    @Transient
    public boolean isEmailVerified() {
        return getEmailAddress().isVerified();
    }

    //@Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    public Timestamp getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Timestamp registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Transient
    public void setHasUserAgreedToTos(boolean hasUserAgreedToTos) {
        if (hasUserAgreedToTos) {
            setAgreedToTosDatetime(now());
        } else {
            setAgreedToTosDatetime(null);
        }
    }

    public Timestamp getAgreedToTosDatetime() {
        return agreedToTosDatetime;
    }

    public void setAgreedToTosDatetime(Timestamp agreedToTosDatetime) {
        this.agreedToTosDatetime = agreedToTosDatetime;
    }
}
