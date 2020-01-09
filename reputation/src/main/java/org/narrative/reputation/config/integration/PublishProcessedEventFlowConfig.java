package org.narrative.reputation.config.integration;

import org.narrative.reputation.service.EventManagementService;
import org.narrative.shared.event.Event;
import org.narrative.shared.event.EventProcessedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;

@Configuration
public class PublishProcessedEventFlowConfig {
    private final EventManagementService eventManagementService;

    public PublishProcessedEventFlowConfig(EventManagementService eventManagementService) {
        this.eventManagementService = eventManagementService;
    }

    /**
     * Publish a processed event to notify the sender that the event has been processed successfully
     */
    @Bean
    IntegrationFlow markProcessedAndPublishProcessedEventFlow() {
        return IntegrationFlows
                .from(ReputationMessageChannelConfig.MARK_PROCESSED_PUBLISH_EVENT)
                /*
                 * Notify interested parties that processing is complete for this event
                 */
                .transform(source -> EventProcessedEvent.builder().eventId(((Event) source).getEventId()).successful(true).build())
                .enrichHeaders(s -> s.header("step", "after transform", true))
                .log(LoggingHandler.Level.DEBUG)
                .handle(source -> eventManagementService.markProcessedAndPublishEventProcessedEvent((EventProcessedEvent) source.getPayload()))

                .get();
    }
}
