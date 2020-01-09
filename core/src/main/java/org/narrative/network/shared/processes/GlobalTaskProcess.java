package org.narrative.network.shared.processes;

import org.narrative.network.shared.context.NetworkContext;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Jan 5, 2006
 * Time: 2:33:07 PM
 */
public class GlobalTaskProcess extends NetworkTaskProcess {

    public GlobalTaskProcess(NetworkContext networkContext, String name) {
        super(networkContext, name);
    }

    public String getAreaName() {
        return "{master request}";
    }
}
