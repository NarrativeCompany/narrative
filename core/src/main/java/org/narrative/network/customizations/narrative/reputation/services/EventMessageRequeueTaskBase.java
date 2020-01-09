package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.customizations.narrative.reputation.EventMessage;
import org.narrative.network.customizations.narrative.reputation.EventMessageStatus;
import org.narrative.network.shared.util.NetworkLogger;

/**
 * Date: 2018-12-15
 * Time: 12:52
 *
 * @author jonmark
 */
public abstract class EventMessageRequeueTaskBase extends EventMessageProcessingTask {
    private static final NetworkLogger logger = new NetworkLogger(EventMessageRequeueTaskBase.class);

    public EventMessageRequeueTaskBase(EventMessage event) {
        super(event);
    }

    protected abstract int getOriginalCount(EventMessage event);

    protected abstract void setNewCount(EventMessage event, int count);

    @Override
    protected void processEvent(EventMessage event) {
        int originalCount = getOriginalCount(event);
        int newCount = originalCount + 1;

        setNewCount(event, newCount);

        // jw: if the event failed too many times let's just short out and not requeue it.
        if (newCount >= 10) {
            // jw: since this should almost never happen, let's make it more discoverable when it does. Log an error, and
            //     record it into the statistic manager so it appears on error pages.
            String message = "Failed to process EventMessage/" + event.getEventId();
            logger.error(message);
            StatisticManager.recordException(UnexpectedError.getRuntimeException(message), false, null);

            event.setStatus(EventMessageStatus.FAILED_PROCESSING);
            return;
        }

        event.requeueWithExponentialBackoff(originalCount);
    }
}
