package org.narrative.reputation.config.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;

@Configuration
public class DuplicateMessageFlowConfig {
    /**
     * Integration flow for duplicate processing
     */
    @Bean
    public IntegrationFlow duplicateFlow() {
        return IntegrationFlows
                .from(ReputationMessageChannelConfig.DUPLICATE_MESSAGE)
                .log(LoggingHandler.Level.WARN, message -> "Duplicate message detected and discarded: " + message.getPayload())

                .get();
    }
}
