package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.config.StaticConfig;
import org.narrative.network.customizations.narrative.reputation.EventMessage;
import org.narrative.network.customizations.narrative.reputation.EventMessageStatus;
import org.narrative.shared.event.Event;
import org.narrative.shared.reputation.config.redisson.ReputationRedissonQueueService;

import java.time.Instant;

/**
 * Date: 2018-12-13
 * Time: 15:22
 *
 * @author jonmark
 */
public class SendEventMessageTask extends EventMessageProcessingTask {
    public SendEventMessageTask(EventMessage event) {
        super(event);
    }

    @Override
    protected void processEvent(EventMessage event) {
        boolean sent = sendEvent(event);

        // jw: if we failed to send for some reason, let's use the failure processor so that it will be requeued if appropriate.
        if (!sent) {
            getNetworkContext().doGlobalTask(new HandleEventMessageFailureTask(event));
            return;
        }

        event.setStatus(EventMessageStatus.SENT);
        event.setSendCount(event.getSendCount() + 1);
        event.setLastSentDatetime(Instant.now());
    }

    private boolean sendEvent(EventMessage eventMessage) {
        ReputationRedissonQueueService service = StaticConfig.getBean(ReputationRedissonQueueService.class);
        Event event = eventMessage.getEvent();

        service.pushMessage(event.getEventType().getEventQueueName(), event);

        return true;
    }
}
