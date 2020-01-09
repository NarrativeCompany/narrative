package org.narrative.network.core.area.user;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.util.NarrativeConstants;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.user.dao.AreaCredentialsDAO;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.Credentials;
import org.narrative.network.core.user.PasswordFields;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserAuth;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Sep 27, 2010
 * Time: 9:53:27 AM
 *
 * @author brian
 */
@Entity
@Proxy
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {AreaCredentials.FIELD__AREA_RLM__COLUMN, AreaCredentials.FIELD__EMAIL_ADDRESS__COLUMN})})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AreaCredentials implements DAOObject<AreaCredentialsDAO>, Credentials {

    private OID oid;

    private AreaRlm areaRlm;

    private String emailAddress;
    private PasswordFields passwordFields;

    private transient boolean hasLookedUpUser;
    private transient User user;

    public static final String FIELD__AREA_RLM__NAME = "areaRlm";
    public static final String FIELD__EMAIL_ADDRESS__NAME = "emailAddress";

    public static final String FIELD__AREA_RLM__COLUMN = FIELD__AREA_RLM__NAME + "_" + AreaRlm.FIELD__OID__NAME;
    public static final String FIELD__EMAIL_ADDRESS__COLUMN = FIELD__EMAIL_ADDRESS__NAME;

    /**
     * @deprecated for hibernate use only
     */
    public AreaCredentials() {}

    public AreaCredentials(AreaRlm areaRlm) {
        this.areaRlm = areaRlm;
        this.passwordFields = new PasswordFields();
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @ManyToOne(optional = false)
    @ForeignKey(name = "fk_areaCredentials_areaRlm")
    public AreaRlm getAreaRlm() {
        return areaRlm;
    }

    public void setAreaRlm(AreaRlm areaRlm) {
        this.areaRlm = areaRlm;
    }

    @NotNull
    @Length(min = NarrativeConstants.MIN_EMAIL_ADDRESS_LENGTH, max = NarrativeConstants.MAX_EMAIL_ADDRESS_LENGTH)
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @NotNull
    public PasswordFields getPasswordFields() {
        return passwordFields;
    }

    public void setPasswordFields(PasswordFields passwordFields) {
        this.passwordFields = passwordFields;
    }

    @Transient
    @Override
    public User getUser() {
        if (!hasLookedUpUser) {
            UserAuth userAuth = UserAuth.dao().getForZoneProviderIdentifier(getAuthZone(), getAuthZone().getInternalAuthProvider(), getOid().toString());
            if (exists(userAuth)) {
                user = userAuth.getUser();
            }
            hasLookedUpUser = true;
        }
        return user;
    }

    @Transient
    @Override
    public boolean isEmailVerified() {
        return getUser().getUserFields().isEmailVerified();
    }

    @Transient
    @Override
    public AuthZone getAuthZone() {
        return AreaRlm.getArea(getAreaRlm()).getAuthZone();
    }

    public static AreaCredentialsDAO dao() {
        return NetworkDAOImpl.getDAO(AreaCredentials.class);
    }
}
