package org.narrative.reputation.config.integration;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.service.EventManagementService;
import org.narrative.shared.event.Event;
import org.narrative.shared.event.reputation.ReputationEvent;
import org.narrative.shared.event.reputation.ReputationEventType;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.PollerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class ReputationFlowBuilder {
    private final EventManagementService eventManagementService;
    private final PlatformTransactionManager platformTransactionManager;
    private final DefaultListableBeanFactory beanFactory;
    private final UserOIDLockManager userOIDLockManager;

    public ReputationFlowBuilder(EventManagementService eventManagementService,
                                 PlatformTransactionManager platformTransactionManager,
                                 DefaultListableBeanFactory beanFactory,
                                 UserOIDLockManager userOIDLockManager) {
        this.eventManagementService = eventManagementService;
        this.platformTransactionManager = platformTransactionManager;
        this.beanFactory = beanFactory;
        this.userOIDLockManager = userOIDLockManager;
    }

    /**
     * Flow builder for simple event flows.  This flow type is for all events that apply to single user OID.
     *
     * @param eventType The event type to process
     * @param eventTypeConfig The configuration for that event type
     * @param processor The processing function for the event type
     */
    IntegrationFlow buildSimpleIntegrationFlow(ReputationEventType eventType,
                                               SpringIntegrationProperties.EventTypeConfig eventTypeConfig,
                                               Function<ReputationEvent, ReputationEvent> processor) {
        return buildIntegrationFlow(eventType, eventTypeConfig, processor, ReputationMessageChannelConfig.AFTER_SIMPLE_FLOW);
    }

    /**
     * Flow builder for bulk user event flows.  This flow type is for all events that apply to multiple user OIDs.
     *
     * @param eventType The event type to process
     * @param eventTypeConfig The configuration for that event type
     * @param processor The processing function for the event type
     */
    IntegrationFlow buildBulkUserIntegrationFlow(ReputationEventType eventType,
                                               SpringIntegrationProperties.EventTypeConfig eventTypeConfig,
                                               Function<ReputationEvent, ReputationEvent> processor,
                                               String postProcessingChannelName) {
        return buildIntegrationFlow(eventType, eventTypeConfig, processor, postProcessingChannelName);
    }

    /**
     * Flow builder for all event types.
     *
     * @param eventType The event type to process
     * @param eventTypeConfig The configuration for that event type
     * @param processor The processing function for the event type
     * @param postProcessingChannel The post processing channel to pass the event along to when this flow completes
     */
    private IntegrationFlow buildIntegrationFlow(ReputationEventType eventType,
                                         SpringIntegrationProperties.EventTypeConfig eventTypeConfig,
                                         Function<ReputationEvent, ReputationEvent> processor,
                                         String postProcessingChannel) {
        String errorChannelName = IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME;
        String queueName = eventType.getEventQueueName();

        return IntegrationFlows
                        /*
                         * Source messages from the specified endpoint
                         */
                        .from(buildMessageSource(queueName), e -> buildPollerSpec(e, queueName, eventTypeConfig))
                        .channel(MessageChannels.direct(queueName + "_input"))

                        /*
                         * Create locks and acquire for this event - this also registers a transaction synchronization
                         * that will unlock after transaction commit/rollback
                         */
                        .<Event>handle((payload, headers) -> userOIDLockManager.createAndAcquireLock(payload))

                        /*
                         * Set the error channel for this event
                         */
                        .enrichHeaders(s -> s.header(errorChannelName, errorChannelName, true))
                        .log(LoggingHandler.Level.DEBUG)

                        /*
                         * De-dupe and log when a duplicate is encountered.  The filter will cause the flow to stop
                         * on a duplicate message and will pass along to the discard flow.
                         */
                        .filter(Message.class,
                                source -> !eventManagementService.isDuplicateEvent((Event) source.getPayload()),
                                fes -> fes.discardFlow(i -> i.channel(ReputationMessageChannelConfig.DUPLICATE_MESSAGE))
                        )
                        .enrichHeaders(s -> s.header("step", "after filter", true))
                        .log(LoggingHandler.Level.DEBUG)

                        /*
                         * Process the event
                         */
                        .<ReputationEvent>handle((payload, headers) -> processor.apply(payload))
                        .enrichHeaders(s -> s.header("step", "after handle", true))
                        .log(LoggingHandler.Level.DEBUG)

                        /*
                         * Hand off to the appropriate post processor
                         */
                        .channel(postProcessingChannel)

                        .get();
    }

    /**
     * Build a {@link MessageSource} that polls a Redisson queue
     */
    private MessageSource<Event> buildMessageSource(String queueName) {
        return () -> {
            Event event = eventManagementService.pollEventFromQueue(queueName);
            return event != null ? MessageBuilder.withPayload(event).build() : null;
        };
    }

    /**
     * Build a poller spec for an event type
     */
    private SourcePollingChannelAdapterSpec buildPollerSpec(SourcePollingChannelAdapterSpec spec,
                                                            String queueName,
                                                            SpringIntegrationProperties.EventTypeConfig eventTypeConfig) {

        PollerSpec pollerSpec = Pollers
                .fixedDelay(eventTypeConfig.getPollInterval().toMillis())
                .maxMessagesPerPoll(eventTypeConfig.getMaxMessagesPerPoll())
                .errorChannel(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME);

        // Transaction support for message processing?
        if (eventTypeConfig.isTransactionalProcessing()) {
            pollerSpec.transactional(platformTransactionManager);
        }

        String pollerName = "poller_" + queueName;
        pollerSpec.taskExecutor(buildAndRegisterThreadPoolExecutor(pollerName, eventTypeConfig));

        SourcePollingChannelAdapterSpec res = spec.poller(pollerSpec);

        //So we can disable for testing if needed
        res.autoStartup(eventTypeConfig.isStartPoller());

        return res;
    }

    private ThreadPoolExecutor buildAndRegisterThreadPoolExecutor(String pollerName, SpringIntegrationProperties.EventTypeConfig eventTypeConfig) {
        //Register with Spring
        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(PollerThreadPoolExecutor.class);
        bdb.setLazyInit(false);
        bdb.setScope(BeanDefinition.SCOPE_SINGLETON);
        bdb.setDestroyMethodName("shutdown");
        bdb.addConstructorArgValue(pollerName);
        bdb.addConstructorArgValue(1);
        bdb.addConstructorArgValue(eventTypeConfig.getThreadCount());
        bdb.addConstructorArgValue(60L);
        bdb.addConstructorArgValue(TimeUnit.SECONDS);
        bdb.addConstructorArgValue(new LinkedBlockingQueue<>(5000));
        bdb.addConstructorArgValue(new ThreadFactoryBuilder().setNameFormat(pollerName + "-%d").build());

        String beanName = "ThreadPoolExecutor_" + pollerName;
        beanFactory.registerBeanDefinition(beanName, bdb.getBeanDefinition());

        return beanFactory.getBean(beanName, PollerThreadPoolExecutor.class);
    }

    static class PollerThreadPoolExecutor extends ThreadPoolExecutor {
        private final String pollerName;

        public PollerThreadPoolExecutor(String pollerName, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
            this.pollerName = pollerName;
        }

        @Override
        public void shutdown() {
            LoggerFactory.getLogger(pollerName).info("shutdown down executor for poller {}", pollerName);
            getQueue().clear();
            super.shutdown();
        }
    }
}
