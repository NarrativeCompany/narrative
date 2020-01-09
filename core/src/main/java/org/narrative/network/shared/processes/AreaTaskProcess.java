package org.narrative.network.shared.processes;

import org.narrative.network.shared.context.AreaContext;
import org.narrative.network.shared.context.NetworkContext;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Jan 5, 2006
 * Time: 2:33:07 PM
 */
public class AreaTaskProcess extends NetworkTaskProcess {

    private AreaContext areaContext = null;

    public AreaTaskProcess(AreaContext areaContext, NetworkContext networkContext, String name) {
        super(networkContext, name);
        this.areaContext = areaContext;
    }

    public String getAreaName() {
        return areaContext.getArea().getAreaNameResolved();
    }
}
