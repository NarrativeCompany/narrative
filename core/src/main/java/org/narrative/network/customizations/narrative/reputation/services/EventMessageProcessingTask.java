package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.network.customizations.narrative.reputation.EventMessage;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2018-12-13
 * Time: 15:23
 *
 * @author jonmark
 */
public abstract class EventMessageProcessingTask extends GlobalTaskImpl<Object> {
    private final EventMessage event;

    public EventMessageProcessingTask(EventMessage event) {
        assert exists(event) : "EventMessage must be provided!";

        this.event = event;
    }

    protected abstract void processEvent(EventMessage event);

    @Override
    protected Object doMonitoredTask() {
        processEvent(event);

        return null;
    }
}
