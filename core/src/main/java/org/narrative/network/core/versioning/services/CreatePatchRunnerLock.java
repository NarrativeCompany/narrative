package org.narrative.network.core.versioning.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.versioning.PatchRunnerLock;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Mar 24, 2010
 * Time: 8:29:23 AM
 *
 * @author brian
 */
public class CreatePatchRunnerLock extends GlobalTaskImpl<Object> {
    @Override
    protected Object doMonitoredTask() {
        PatchRunnerLock lock = PatchRunnerLock.dao().getReal(PatchRunnerLock.LOCK_OID);
        if (!exists(lock)) {
            try {
                PatchRunnerLock.dao().save(new PatchRunnerLock(PatchRunnerLock.LOCK_OID));
                PatchRunnerLock.dao().getGSession().flushSession();
            } catch (Throwable t) {
                lock = PatchRunnerLock.dao().getReal(PatchRunnerLock.LOCK_OID);
                if (!exists(lock)) {
                    throw UnexpectedError.getRuntimeException("Failed creating PatchRunnerLock!", t);
                }
            }
        }
        return null;
    }
}
