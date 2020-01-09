package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.customizations.narrative.reputation.EventMessage;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * Date: 2018-12-13
 * Time: 14:15
 *
 * @author jonmark
 */
@DisallowConcurrentExecution
public class SendQueuedReputationEventsJob extends NetworkJob {
    private static final NetworkLogger logger = new NetworkLogger(SendQueuedReputationEventsJob.class);

    @Deprecated // Quartz only
    public SendQueuedReputationEventsJob() { }

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting to send queued EventMessages.");
        }

        int totalEventsSent = 0;
        int eventsJustSent;
        while ((eventsJustSent = sendQueuedEvents()) > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Sent EventMessages " + totalEventsSent + "-" + (totalEventsSent + eventsJustSent) + " and still going.");
            }
            totalEventsSent += eventsJustSent;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sent " + totalEventsSent + " total EventMessages.");
        }
    }

    // jw: sends the next chunk of events queued for processing.
    private int sendQueuedEvents() {
        return TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Integer>() {
            @Override
            protected Integer doMonitoredTask() {
                List<EventMessage> events = EventMessage.dao().getQueuedEventMessages(50);

                for (EventMessage event : events) {
                    getNetworkContext().doGlobalTask(new SendEventMessageTask(event));
                }

                return events.size();
            }
        });
    }
}
