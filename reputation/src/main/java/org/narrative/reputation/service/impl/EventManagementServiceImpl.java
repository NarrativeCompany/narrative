package org.narrative.reputation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.config.integration.SpringIntegrationProperties;
import org.narrative.reputation.model.entity.EventDedupEntity;
import org.narrative.reputation.repository.EventDedupRepository;
import org.narrative.reputation.service.EventManagementService;
import org.narrative.shared.event.Event;
import org.narrative.shared.event.EventProcessedEvent;
import org.narrative.shared.reputation.config.redisson.ReputationRedissonQueueService;
import org.narrative.shared.spring.metrics.TimedService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.narrative.reputation.config.integration.ReputationMessageChannelConfig.*;

@Slf4j
@Service
@TimedService(percentiles = {0.8, 0.9, 0.99})
public class EventManagementServiceImpl implements EventManagementService {
    private final ReputationRedissonQueueService redissonQueueService;
    private final MessageChannel pushProcessedEventChannel;
    private final EventDedupRepository eventDedupRepository;
    private final SpringIntegrationProperties siProperties;
    private final TransactionTemplate txTemplate;

    public EventManagementServiceImpl(ReputationRedissonQueueService redissonQueueService,
                                      @Qualifier(PUSH_PROCESSED_EVENT) MessageChannel pushProcessedEventChannel,
                                      EventDedupRepository eventDedupRepository,
                                      ReputationProperties reputationProperties,
                                      PlatformTransactionManager txManager) {
        this.redissonQueueService = redissonQueueService;
        this.pushProcessedEventChannel = pushProcessedEventChannel;
        this.eventDedupRepository = eventDedupRepository;
        this.siProperties = reputationProperties.getSi();
        this.txTemplate = new TransactionTemplate(txManager);
        this.txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        this.txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public boolean isDuplicateEvent(Event event) {
        log.info("Checking for duplicate event with: {} " , event);

        AtomicBoolean isDuplicate = new AtomicBoolean(false);

        // 99% case - first try to insert outside of the open event processing tx.  This will succeed if the event
        // is not a duplicate
        try {
            txTemplate.execute(status -> {
                    EventDedupEntity eventDedupEntity = EventDedupEntity.builder().uuid(event.getEventId()).build();
                    eventDedupEntity = eventDedupRepository.saveAndFlush(eventDedupEntity);
                    return eventDedupEntity;
            });
        } catch (DataIntegrityViolationException e) {
            isDuplicate.set(true);
        }

        // 1% case - reprocessing.  Acquire the event dedup lock if the lock is timed out
        AtomicBoolean wasUnlocked = new AtomicBoolean(false);
        if (isDuplicate.get()) {
            try {

                // Update outside of the open event processing tx
                txTemplate.execute(status -> {

                    EventDedupEntity eventDedupEntity = eventDedupRepository.findById(event.getEventId()).orElse(null);

                    // At this point, check to see if not marked as processed
                    if (eventDedupEntity == null) {
                        log.warn("Dedup entity not found immediately after insert failed for {}.  Deleted by cleanup job?", event.getEventId());
                    } else {
                        if (eventDedupEntity.isProcessed()) {
                            log.warn("Dedup entity has already been processed {}", eventDedupEntity);
                        } else {
                            // Not processed so see if the dedup lock has expired
                            Instant now = Instant.now();
                            Instant expireInstant = eventDedupEntity.getLockTimestamp().plus(siProperties.getEvent().getDeduplicationLockDuration());
                            if (expireInstant.isAfter(now)) {
                                log.info("Dedup entity was not unlocked - lock has not expired for UUID {}", eventDedupEntity.getUuid());
                            } else {
                                log.info("Dedup entity lock has expired - attempting to acquire ownership for UUID {}", eventDedupEntity.getUuid());
                                try {
                                    eventDedupEntity.setLockTimestamp(now);
                                    eventDedupEntity.setRetryAttempt(eventDedupEntity.getRetryAttempt() + 1);
                                    // This will fail with an optimistic lock exception if another thread/process has updated this row during our transaction
                                    eventDedupRepository.saveAndFlush(eventDedupEntity);

                                    isDuplicate.set(false);
                                    wasUnlocked.set(true);
                                    log.info("Dedup entity is now owned and event will be processed");
                                } catch (DataIntegrityViolationException e) {
                                    log.error("Error unlocking previously locked dedup row after timeout for UUID " + event.getEventId(), e);
                                }
                            }
                        }
                    }
                    return null;
                });

            } catch (Exception e) {
                log.error("Unhandled error during dedup of existing dedup row for event", e);
                isDuplicate.set(true);
                wasUnlocked.set(false);
            }
        }

        String res = wasUnlocked.get() ? "unlocked" : Boolean.toString(isDuplicate.get());
        log.debug("Duplicate event detected? {}, {}", res, event);

        return isDuplicate.get();
    }

    @Override
    public <T> T pollEventFromQueue(String queueName) {
        T res = redissonQueueService.popMessage(queueName);
        if (res != null ) {
            log.debug("Message popped from queue {}: {}", queueName, res);
        } else {
            log.trace("No message in queue {}", queueName);
        }

        return res;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void markProcessedAndPublishEventProcessedEvent(EventProcessedEvent eventProcessedEvent) {
        log.info("markProcessedAndPublishEventProcessedEvent with: {} " , eventProcessedEvent);

        // Mark the dedup record as processed in the event processing transaction - this thread already owns the lock
        eventDedupRepository.markDedupAsProcessed(eventProcessedEvent.getEventId(), Instant.now());

        // Send the message after the transaction is successfully completed
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                pushProcessedEventChannel.send(new GenericMessage<>(eventProcessedEvent));
            }
        });

        log.debug("Event dedup updated and processed event sent: {}", eventProcessedEvent);
    }
}
