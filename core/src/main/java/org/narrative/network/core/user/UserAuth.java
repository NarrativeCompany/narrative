package org.narrative.network.core.user;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.user.dao.UserAuthDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * Date: Sep 23, 2010
 * Time: 8:07:42 AM
 *
 * @author brian
 */
@Entity
@Proxy
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {UserAuth.FIELD__USER__COLUMN, UserAuth.FIELD__AUTH_PROVIDER__COLUMN}), @UniqueConstraint(columnNames = {UserAuth.FIELD__AUTH_ZONE__NAME, UserAuth.FIELD__AUTH_PROVIDER__COLUMN, UserAuth.FIELD__IDENTIFIER__COLUMN})})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserAuth implements DAOObject<UserAuthDAO> {

    private OID oid;
    private User user;
    private AuthZone authZone;
    private AuthProvider authProvider;
    private String identifier;

    public static final String FIELD__USER__NAME = "user";
    public static final String FIELD__AUTH_ZONE__NAME = "authZone";
    public static final String FIELD__AUTH_PROVIDER__NAME = "authProvider";
    public static final String FIELD__IDENTIFIER__NAME = "identifier";

    public static final String FIELD__USER__COLUMN = FIELD__USER__NAME + "_" + User.FIELD__OID__NAME;
    public static final String FIELD__AUTH_ZONE__COLUMN = FIELD__AUTH_ZONE__NAME;
    public static final String FIELD__AUTH_PROVIDER__COLUMN = FIELD__AUTH_PROVIDER__NAME;
    public static final String FIELD__IDENTIFIER__COLUMN = FIELD__IDENTIFIER__NAME;

    /**
     * @deprecated for hibernate use only
     */
    public UserAuth() {}

    public UserAuth(User user, AuthProvider authProvider, String identifier) {
        this.user = user;
        this.authZone = user.getAuthZone();
        this.authProvider = authProvider;
        this.identifier = identifier;
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
    @ForeignKey(name = "fk_userAuth_user")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
    @Type(type = IntegerEnumType.TYPE)
    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    private static final int MIN_IDENTIFIER_LENGTH = 1;
    private static final int MAX_IDENTIFIER_LENGTH = 255;

    @NotNull
    @Length(min = MIN_IDENTIFIER_LENGTH, max = MAX_IDENTIFIER_LENGTH)
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public static UserAuthDAO dao() {
        return NetworkDAOImpl.getDAO(UserAuth.class);
    }

}
