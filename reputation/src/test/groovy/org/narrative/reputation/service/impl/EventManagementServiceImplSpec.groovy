package org.narrative.reputation.service.impl

import org.narrative.reputation.BaseContainerizedReputationApplicationSpec
import org.narrative.reputation.repository.EventDedupRepository
import org.narrative.reputation.service.EventManagementService
import org.narrative.shared.event.EventProcessedEvent
import org.narrative.shared.event.reputation.ConductEventType
import org.narrative.shared.event.reputation.ConductStatusEvent
import org.redisson.api.RedissonClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.IgnoreIf
import test.util.TestCategory

import java.time.Instant
import java.time.temporal.ChronoUnit

@IgnoreIf({TestCategory.isIntegContainerizedTestsDisabled()})
class EventManagementServiceImplSpec extends BaseContainerizedReputationApplicationSpec {
    @Autowired
    EventManagementService eventManagementService
    @Autowired
    EventDedupRepository eventDedupRepository
    @Autowired
    RedissonClient redissonClient
    @Autowired
    TransactionTemplate txTemplate

    @Transactional(propagation = Propagation.NEVER)
    def "Test deDuplicateEvent"() {
        given:
            EventProcessedEvent event = EventProcessedEvent.builder().eventId(UUID.randomUUID()).build()
        when:
            def res1 = eventManagementService.isDuplicateEvent(event)
            def res1Exists = eventDedupRepository.getOne(event.eventId) != null
            def res2 = eventManagementService.isDuplicateEvent(event)
        then:
            !res1
            res1Exists
            res2
    }

    @Transactional(propagation = Propagation.NEVER)
    def "Test deDuplicateEvent lock expired"() {
        given:
            EventProcessedEvent event = EventProcessedEvent.builder().eventId(UUID.randomUUID()).build()
        when:
            def res1 = eventManagementService.isDuplicateEvent(event)
            def res1Exists = eventDedupRepository.getOne(event.eventId) != null
            txTemplate.execute({ TransactionStatus status ->
                def dedup = eventDedupRepository.getOne(event.eventId)
                dedup.setLockTimestamp(Instant.now().minus(1, ChronoUnit.DAYS))
                eventDedupRepository.saveAndFlush(dedup)
                return null;
            });
            def res2 = eventManagementService.isDuplicateEvent(event)
        then:
            !res1
            res1Exists
            !res2
    }

    @Transactional(propagation = Propagation.NEVER)
    def "Test deDuplicateEvent processed"() {
        given:
            EventProcessedEvent event = EventProcessedEvent.builder().eventId(UUID.randomUUID()).build()
        when:
            def res1 = eventManagementService.isDuplicateEvent(event)
            def res1Exists = eventDedupRepository.getOne(event.eventId) != null
            txTemplate.execute({ TransactionStatus status ->
                def dedup = eventDedupRepository.getOne(event.eventId)
                dedup.setProcessed(true)
                eventDedupRepository.saveAndFlush(dedup)
                return null;
            });
            def res2 = eventManagementService.isDuplicateEvent(event)
        then:
            !res1
            res1Exists
            res2
    }

    def "Test pollEventFromQueue"() {
        given:
            def queue1 = 'queue1'
            def queue2 = 'queue2'
            ConductStatusEvent sre1 = ConductStatusEvent.builder().conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE).build()
            ConductStatusEvent sre2 = ConductStatusEvent.builder().conductEventType(ConductEventType.CONTENT_REMOVED_FOR_AUP_VIOLATION).build()
            EventProcessedEvent epe1 = EventProcessedEvent.builder().eventId(UUID.randomUUID()).build()
            EventProcessedEvent epe2 = EventProcessedEvent.builder().eventId(UUID.randomUUID()).build()
            redissonClient.getBlockingQueue(queue1).put(sre1)
            redissonClient.getBlockingQueue(queue1).put(sre2)
            redissonClient.getBlockingQueue(queue2).put(epe1)
            redissonClient.getBlockingQueue(queue2).put(epe2)
        when:
            def res1 = eventManagementService.pollEventFromQueue(queue1)
            def res2 = eventManagementService.pollEventFromQueue(queue1)
            def res3 = eventManagementService.pollEventFromQueue(queue2)
            def res4 = eventManagementService.pollEventFromQueue(queue2)
        then:
            res1 == sre1
            res2 == sre2
            res3 == epe1
            res4 == epe2
    }

    @Transactional(propagation = Propagation.NEVER)
    def "test publishEventProcessedEvent"() {
        given:
            EventProcessedEvent epe1 = EventProcessedEvent.builder().eventId(UUID.randomUUID()).build()
            EventProcessedEvent epe2 = EventProcessedEvent.builder().eventId(UUID.randomUUID()).build()
            EventProcessedEvent epe3 = EventProcessedEvent.builder().eventId(UUID.randomUUID()).build()
            EventProcessedEvent epe4 = EventProcessedEvent.builder().eventId(UUID.randomUUID()).build()
            eventDedupRepository
            def queue = EventProcessedEvent.EVENT_TYPE.eventQueueName
        when:
            eventManagementService.isDuplicateEvent(epe1)
            eventManagementService.isDuplicateEvent(epe2)
            eventManagementService.isDuplicateEvent(epe3)
            eventManagementService.isDuplicateEvent(epe4)
            // Execute in a transaction - the event send is bound to the wrapping transaction commit
            txTemplate.execute({ TransactionStatus status ->
                eventManagementService.markProcessedAndPublishEventProcessedEvent(epe1)
                eventManagementService.markProcessedAndPublishEventProcessedEvent(epe2)
                eventManagementService.markProcessedAndPublishEventProcessedEvent(epe3)
                eventManagementService.markProcessedAndPublishEventProcessedEvent(epe4)
                null
            })
            def res1 = eventManagementService.pollEventFromQueue(queue)
            def res2 = eventManagementService.pollEventFromQueue(queue)
            def res3 = eventManagementService.pollEventFromQueue(queue)
            def res4 = eventManagementService.pollEventFromQueue(queue)
        then:
            res1 == epe1
            res2 == epe2
            res3 == epe3
            res4 == epe4
    }
}
