package org.narrative.network.shared.processes;

import org.narrative.common.util.processes.GenericProcess;
import org.narrative.network.shared.context.NetworkContext;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 19, 2005
 * Time: 9:07:23 PM
 */
public abstract class NetworkTaskProcess extends GenericProcess {

    private final NetworkContext networkContext;

    protected NetworkTaskProcess(NetworkContext ctxt, String name) {
        super(name);
        networkContext = ctxt;
    }

    public String getOwner() {
        try {
            return networkContext.getPrimaryRole().getDisplayNameResolved();
        } catch (Throwable e) {
            //sometimes the process has already been cleared out by this point
            return "{unknown}";
        }
    }
}