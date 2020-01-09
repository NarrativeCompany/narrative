package org.narrative.reputation.config.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.MessageChannel;

@Configuration
public class ReputationMessageChannelConfig {
    /*
     * Duplicate events get pushed to this message channel
     */
    public static final String DUPLICATE_MESSAGE = "duplicateMessageChannel";
    /*
     * All events that utilize the simple workflow get pushed here after the common workflow steps are complete
     */
    public static final String AFTER_SIMPLE_FLOW = "afterSimpleFlowChannel";
    /*
     * VoteEndedEvent get pushed here after the common workflow steps are complete
     */
    public static final String AFTER_BULK_USER_FLOW_FINISHED = "afterBulkUserFlowFinished";
    /*
     * All processed events get pushed here as the final step in the workflow
     */
    public static final String MARK_PROCESSED_PUBLISH_EVENT = "markProcessedPublishEventChannel";
    /*
     * Processed events get pushed to this message channel to be published to Redis
     */
    public static final String PUSH_PROCESSED_EVENT = "pushProcessedEventChannel";

    @Bean(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME)
    MessageChannel errorChannel() {
        return MessageChannels.direct().get();
    }

    @Bean(DUPLICATE_MESSAGE)
    MessageChannel duplicateMessageChannel() {
        return MessageChannels.direct().get();
    }

    @Bean(AFTER_SIMPLE_FLOW)
    MessageChannel afterSimpleFlowChannel() {
        return MessageChannels.direct().get();
    }

    @Bean(AFTER_BULK_USER_FLOW_FINISHED)
    MessageChannel afterBulkUserFlowEndedChannel() {
        return MessageChannels.direct().get();
    }

    @Bean(MARK_PROCESSED_PUBLISH_EVENT)
    MessageChannel publishProcessedEventChannel() {
        return MessageChannels.direct().get();
    }

    @Bean(PUSH_PROCESSED_EVENT)
    MessageChannel pushProcessedEventChannel() {
        return MessageChannels.direct().get();
    }
}
