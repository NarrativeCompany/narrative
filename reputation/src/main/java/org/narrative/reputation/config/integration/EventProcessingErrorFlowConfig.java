package org.narrative.reputation.config.integration;

import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.service.EventManagementService;
import org.narrative.shared.event.Event;
import org.narrative.shared.event.EventProcessedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;

import java.util.Objects;

@Slf4j
@Configuration
public class EventProcessingErrorFlowConfig {
    private final EventManagementService eventManagementService;

    public EventProcessingErrorFlowConfig(EventManagementService eventManagementService) {
        this.eventManagementService = eventManagementService;
    }

    /**
     * Integration flow for error processing
     */
    @Bean
    public IntegrationFlow errorFlow() {
        return IntegrationFlows
                .from(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME)
                /*
                 *  First log the message
                 */
                .log(LoggingHandler.Level.ERROR, Message::getPayload)

                 /*
                 * Next, if this is a message handling exception, extract the {@link Event} and send a
                 * {@link EventProcessedEvent} indicating that processing failed for the message.
                 */
                .transform(errorMessage -> {
                    if (errorMessage instanceof MessageHandlingException) {
                        Message failedMessage = ((MessageHandlingException) errorMessage).getFailedMessage();
                        if (failedMessage != null) {
                            log.error("Processing failed for message {}", failedMessage);

                            // Build the processed event with a failed status
                            return EventProcessedEvent.builder().eventId(((Event) failedMessage.getPayload()).getEventId()).successful(false).build();
                        } else {
                            log.error("MessageHandlingException failed message is null!");
                            return null;
                        }
                    } else {
                        return null;
                    }
                })

                .filter(Objects::nonNull)

                .handle(source -> eventManagementService.markProcessedAndPublishEventProcessedEvent((EventProcessedEvent) source.getPayload()))

                .get();
    }

}
