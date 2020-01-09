package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/27/18
 * Time: 8:23 AM
 */
public abstract class UpdateReferendumTask extends AreaTaskImpl<Object> {
    private final Referendum referendum;

    protected UpdateReferendumTask(Referendum referendum) {
        this.referendum = referendum;
    }

    protected abstract void updateReferendum(Referendum referendum);

    @Override
    protected Object doMonitoredTask() {
        updateReferendum(referendum);

        return null;
    }
}
