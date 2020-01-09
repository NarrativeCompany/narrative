package org.narrative.network.shared.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.processes.ActionProcess;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 12, 2005
 * Time: 10:37:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class NetworkException extends ApplicationError {
    public NetworkException() {
        super(wordlet("error.title"), "");
    }

    public NetworkException(String message) {
        this(wordlet("error.title"), message);
    }

    public NetworkException(String title, String message) {
        super(title, message);
    }

    private static long getProcessOid() {
        ActionProcess ap = ActionProcess.getActionProcess();
        if (ap != null) {
            OID processOid = ap.getProcessOid();
            if (processOid != null) {
                return processOid.getValue();
            }
        }

        return 0;
    }
}
