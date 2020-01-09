package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.customizations.narrative.reputation.EventMessage;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;
import org.narrative.shared.event.EventProcessedEvent;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2018-12-14
 * Time: 09:48
 *
 * @author jonmark
 */
public class UpdateProcessedReputationEventTask extends GlobalTaskImpl<Object> {
    private static final NetworkLogger logger = new NetworkLogger(UpdateProcessedReputationEventTask.class);

    private final EventProcessedEvent processedEvent;

    public UpdateProcessedReputationEventTask(EventProcessedEvent processedEvent) {
        this.processedEvent = processedEvent;
    }

    @Override
    protected Object doMonitoredTask() {
        EventMessage event = EventMessage.dao().get(processedEvent.getEventId());

        // jw: if we could not find an event let's short out.
        if (!exists(event)) {
            String message = "Failed to find EventMessage from EventProcessedEvent for uuid/" + processedEvent.getEventId();
            logger.error(message);
            StatisticManager.recordException(UnexpectedError.getRuntimeException(message), false, null);
            return null;
        }

        // jw: if the event failed let's allow the failure task to determine what to do with it (resend or ignore if there
        //     has already been too many failures.
        if (!processedEvent.isSuccessful()) {
            getNetworkContext().doGlobalTask(new HandleEventMessageFailureTask(event));
            return null;
        }

        getNetworkContext().doGlobalTask(new HandleSuccessfulEventTask(event.getEvent()));

        // jw: since the event was processed by the reputation module, let's go ahead and remove it since it's no longer necessary.
        EventMessage.dao().delete(event);

        return null;
    }
}
