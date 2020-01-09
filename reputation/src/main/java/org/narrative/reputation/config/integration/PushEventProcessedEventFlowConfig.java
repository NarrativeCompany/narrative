package org.narrative.reputation.config.integration;

import org.narrative.shared.event.EventProcessedEvent;
import org.narrative.shared.reputation.config.redisson.ReputationRedissonQueueService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

@Configuration
public class PushEventProcessedEventFlowConfig {
    private final ReputationRedissonQueueService redissonQueueService;

    public PushEventProcessedEventFlowConfig(ReputationRedissonQueueService redissonQueueService) {
        this.redissonQueueService = redissonQueueService;
    }

    /**
     * Push {@link org.narrative.shared.event.EventProcessedEvent} instances to a Redisson queue
     */
    @Bean
    public IntegrationFlow pushEventProcessedEventFlow() {
        return IntegrationFlows
                .from(ReputationMessageChannelConfig.PUSH_PROCESSED_EVENT)
                .handle(message ->
                                redissonQueueService.pushMessage(
                                        EventProcessedEvent.EVENT_TYPE.getEventQueueName(),
                                        message.getPayload()
                                )
                ).get();
    }
}
