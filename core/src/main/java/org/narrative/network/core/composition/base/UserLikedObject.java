package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.DAO;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.user.User;
import javax.validation.constraints.NotNull;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 1/21/15
 * Time: 8:58 AM
 *
 * @author brian
 */
@MappedSuperclass
public class UserLikedObject<T extends DAO> implements DAOObject<T> {
    private OID oid;
    private OID userOid;
    private Timestamp creationDatetime;
    private boolean isRemoved;

    public static final String FIELD__USER_OID__NAME = "userOid";

    public static final String FIELD__USER_OID__COLUMN = FIELD__USER_OID__NAME;

    public UserLikedObject() {}

    public UserLikedObject(User user) {
        this.userOid = user.getOid();
        this.creationDatetime = now();
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @NotNull
    public OID getUserOid() {
        return userOid;
    }

    public void setUserOid(OID userOid) {
        this.userOid = userOid;
    }

    @Transient
    public User getUser() {
        return User.dao().get(userOid);
    }

    @NotNull
    public Timestamp getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Timestamp creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setRemoved(boolean isRemoved) {
        this.isRemoved = isRemoved;
    }
}
