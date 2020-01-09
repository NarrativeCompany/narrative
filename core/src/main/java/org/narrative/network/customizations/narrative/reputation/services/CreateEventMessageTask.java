package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.network.customizations.narrative.reputation.EventMessage;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.shared.event.Event;

/**
 * Date: 2018-12-12
 * Time: 10:47
 *
 * @author jonmark
 */
public class CreateEventMessageTask extends GlobalTaskImpl<EventMessage> {
    private final Event event;

    public CreateEventMessageTask(Event event) {
        assert event != null : "Should always have an Event to create the EventMessage from!";
        this.event = event;
    }

    @Override
    protected EventMessage doMonitoredTask() {
        // jw: almost everything we need to do is handled by the constructor... As such, not much to do here right now.
        EventMessage eventMessage = new EventMessage(event);

        // jw: since the constructor will assume it is ready for sending, the reputationEvent should be configured as
        //     queued. All we need to do now is save it.
        EventMessage.dao().save(eventMessage);

        // jw: finally, let's return the object since
        return eventMessage;
    }
}
