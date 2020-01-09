package org.narrative.network.core.versioning.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.versioning.PatchRunnerLock;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: Mar 24, 2010
 * Time: 8:28:01 AM
 *
 * @author brian
 */
public class PatchRunnerLockDAO extends GlobalDAOImpl<PatchRunnerLock, OID> {
    public PatchRunnerLockDAO() {
        super(PatchRunnerLock.class);
    }

    public void acquirePatchRunnerLock() {
        getLocked(PatchRunnerLock.LOCK_OID);
    }
}
