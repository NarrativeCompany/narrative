package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.config.StaticConfig;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.customizations.narrative.reputation.EventMessage;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.narrative.shared.event.EventProcessedEvent;
import org.narrative.shared.reputation.config.redisson.ReputationRedissonQueueService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * Date: 2018-12-14
 * Time: 09:22
 *
 * @author jonmark
 */
@DisallowConcurrentExecution
public class ProcessSentReputationEventsJob extends NetworkJob {
    private static final NetworkLogger logger = new NetworkLogger(ProcessSentReputationEventsJob.class);

    @Deprecated // Quartz only
    public ProcessSentReputationEventsJob() { }

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting to process sent ReputationEvents.");
        }

        // jw: first things first, we need to process through the EventProcessedEvents from the reputation-module
        int totalProcessedEvents = 0;
        EventProcessedEvent processedEvent;
        while ((processedEvent = getNextEventProcessedEvent()) != null) {
            TaskRunner.doRootGlobalTask(new UpdateProcessedReputationEventTask(processedEvent));
            totalProcessedEvents++;
            if (totalProcessedEvents % 50 == 0 && logger.isDebugEnabled()) {
                logger.debug("Processed " + totalProcessedEvents + " processed events.");
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Finished processing " + totalProcessedEvents + " processed events. Moving on to stalled sent ReputationEvent objects.");
        }

        // jw: next, we need to look for all sent events that have not been picked up by the reputation-module, and queue
        //     them for resend.
        int totalEventsQueued = 0;
        int eventsJustQueued;
        while ((eventsJustQueued = queueStalledEvents()) > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Queued ReputationEvents " + totalEventsQueued + "-" + (totalEventsQueued + eventsJustQueued) + " and still going.");
            }
            totalEventsQueued += eventsJustQueued;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Queued " + totalEventsQueued + " total ReputationEvents.");
        }
    }

    private EventProcessedEvent getNextEventProcessedEvent() {
        ReputationRedissonQueueService service = StaticConfig.getBean(ReputationRedissonQueueService.class);

        return service.popMessage(EventProcessedEvent.EVENT_TYPE.getEventQueueName());
    }

    // jw: requeues the next chunk of events which have timed out for processing after having been sent.
    private int queueStalledEvents() {
        return TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Integer>() {
            @Override
            protected Integer doMonitoredTask() {
                List<EventMessage> events = EventMessage.dao().getStalledEventMessages(50);

                for (EventMessage event : events) {
                    getNetworkContext().doGlobalTask(new RequeueEventMessageTask(event));
                }

                return events.size();
            }
        });
    }
}
