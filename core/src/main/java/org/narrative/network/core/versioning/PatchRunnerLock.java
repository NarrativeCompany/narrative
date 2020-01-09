package org.narrative.network.core.versioning;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.versioning.dao.PatchRunnerLockDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Date: Mar 24, 2010
 * Time: 8:27:51 AM
 *
 * @author brian
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PatchRunnerLock implements DAOObject<PatchRunnerLockDAO> {

    public static final OID LOCK_OID = new OID(1L);

    private OID oid;

    /**
     * @deprecated for hibernate use only
     */
    public PatchRunnerLock() {}

    public PatchRunnerLock(OID oid) {
        this.oid = oid;
    }

    @Id
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    public static PatchRunnerLockDAO dao() {
        return NetworkDAOImpl.getDAO(PatchRunnerLock.class);
    }
}
