package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.shared.event.Event;
import org.narrative.shared.event.reputation.ConductStatusEvent;
import org.narrative.shared.event.reputation.ReputationEventType;

import java.util.Collections;

/**
 * Date: 2019-02-13
 * Time: 20:29
 *
 * @author brian
 */
public class HandleSuccessfulEventTask extends GlobalTaskImpl<Object> {
    private final Event event;

    public HandleSuccessfulEventTask(Event event) {
        this.event = event;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: for all conduct status events, we need to send an email to the user
        if(ReputationEventType.CONDUCT_STATUS_EVENT.equals(event.getEventType())) {
            ConductStatusEvent conductStatusEvent = (ConductStatusEvent) event;
            Area narrativeArea = Area.dao().getNarrativePlatformArea();
            getNetworkContext().doAreaTask(narrativeArea, new SendConductNegativeStartedEmailTask(Collections.singletonList(OID.valueOf(conductStatusEvent.getUserOid())), conductStatusEvent.getConductEventType()));
        }
        return null;
    }
}
