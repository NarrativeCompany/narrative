package org.narrative.network.customizations.narrative.niches.niche.services;

import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/15/18
 * Time: 10:31 AM
 */
public abstract class NicheTaskBase extends AreaTaskImpl<Object> {
    private final Niche niche;

    protected NicheTaskBase(Niche niche) {
        this.niche = niche;
    }

    protected abstract void processNiche(Niche niche);

    @Override
    protected Object doMonitoredTask() {
        processNiche(niche);

        return null;
    }
}
