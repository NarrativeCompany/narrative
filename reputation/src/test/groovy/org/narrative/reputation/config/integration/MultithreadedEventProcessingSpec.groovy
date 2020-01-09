package org.narrative.reputation.config.integration

import org.apache.commons.lang3.RandomUtils
import org.narrative.reputation.BaseContainerizedReputationApplicationSpec
import org.narrative.reputation.model.entity.EventDedupEntity
import org.narrative.reputation.repository.EventDedupRepository
import org.narrative.shared.event.Event
import org.narrative.shared.event.EventProcessedEvent
import org.narrative.shared.event.reputation.*
import org.narrative.shared.reputation.config.redisson.ReputationRedissonQueueService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.integration.endpoint.SourcePollingChannelAdapter
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.IgnoreIf
import test.util.TestCategory

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@DirtiesContext
@IgnoreIf({ TestCategory.isIntegContainerizedTestsDisabled()})
@ActiveProfiles ( profiles = ['default','local','containerInteg', 'eventStress'] )
class MultithreadedEventProcessingSpec extends BaseContainerizedReputationApplicationSpec {
    @Autowired
    ReputationRedissonQueueService redissonQueueService
    @Autowired
    TransactionTemplate txTemplate
    @Autowired
    EventDedupRepository dedupRepository
    @Autowired
    ApplicationContext applicationContext

    def "Test multi-threaded flow" () {
        given:
            def dupCount = 25

            //Build a list of duplicated events - modify the UUID but duplicate payload/user OIDs.
            //Rotate oids to create lots of contention for locks
            def events = []
            for(int i = 0; i < dupCount; i++) {
                def curList = []
                for (int j = 1; j < 11; j++) {
                    curList.add ConductStatusEvent.builder().userOid(j).conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE).build()
                    curList.add ContentLikeEvent.builder().userOid(j).likePoints(10).likeEventType(LikeEventType.DISLIKE).build();
                    curList.add CommentLikeEvent.builder().userOid(j).likePoints(10).likeEventType(LikeEventType.DISLIKE).build();
                    curList.add KYCVerificationEvent.builder().userOid(j).isVerified(true).build();
                    curList.add RatingEvent.builder().userOid(j).ratedWithConsensus(false).build();
                    curList.add NegativeQualityEvent.builder().userOid(j).negativeQualityEventType(NegativeQualityEventType.CHANGE_REQUEST_DENIED_BY_TRIBUNAL).build()
                }

                curList.add ConsensusChangedEvent.builder().usersConsensusChangedMap(buildBooleanPairMap()).build();
                curList.add VoteEndedEvent.builder().userVotesMap(buildDecisionPairMap()).build();

                //Make sure there are existing dedup rows for 1 in 5 events
                if (i % 5 == 0) {
                    for (ReputationEvent re: curList) {
                        EventDedupEntity ede = EventDedupEntity.builder()
                                .uuid(re.eventId)
                                .lockTimestamp(Instant.now().minus(10, ChronoUnit.HOURS))
                                .build()
                        ede.setRetryAttempt(RandomUtils.nextInt(0, 5))
                        txTemplate.execute{status -> dedupRepository.save(ede) }
                    }
                }

                events.addAll curList
            }
            def eventMap = [:]
            for (Event event: events) {
                eventMap.put(event.eventId, event)
            }
            List<EventProcessedEvent> processedEventList = []
        when:
            //Start the polling adapters
            Map<String, SourcePollingChannelAdapter> beanMap = applicationContext.getBeansOfType(SourcePollingChannelAdapter)
            for(SourcePollingChannelAdapter pollingChannelAdapter: beanMap.values()) {
                pollingChannelAdapter.start()
            }

            //Push the events to Redisson
            for (Event event: events) {
                redissonQueueService.pushMessage(event.getEventType().eventQueueName, event)
            }

            //Wait for all to be processed successfully
            long timer = System.currentTimeMillis()
            def successful = true
            while (redissonQueueService.getQueue(EventProcessedEvent.EVENT_TYPE.eventQueueName).size() < events.size()) {
                Thread.sleep(500)
                if ((System.currentTimeMillis() - timer) > TimeUnit.SECONDS.toMillis(500)) {
                    successful = false
                    break
                }
            }
            EventProcessedEvent message = redissonQueueService.popMessage(EventProcessedEvent.EVENT_TYPE.eventQueueName)
            while (message != null) {
                processedEventList.add(message)
                message = redissonQueueService.popMessage(EventProcessedEvent.EVENT_TYPE.eventQueueName)
            }
            def errorEvents = []
            for (EventProcessedEvent event: processedEventList) {
                if (!event.successful) {
                    errorEvents.add(eventMap.get(event.eventId))
                }
            }

            //Stop all of the pollers so we can shut down cleanly
            for(SourcePollingChannelAdapter pollingChannelAdapter: beanMap.values()) {
                pollingChannelAdapter.stop()
            }
        then:
            successful
            errorEvents.size() == 0
    }

    def buildBooleanPairMap() {
        def res = [:]
        res.put(1, randomBool())
        res.put(2, randomBool())
        res.put(3, randomBool())
        res.put(4, randomBool())
        res.put(5, randomBool())
        res.put(6, randomBool())
        res.put(7, randomBool())
        res.put(8, randomBool())
        res.put(9, randomBool())
        res.put(10, randomBool())
        res
    }

    def buildDecisionPairMap() {
        def res = [:]
        res.put(1, randomDecision())
        res.put(2, randomDecision())
        res.put(3, randomDecision())
        res.put(4, randomDecision())
        res.put(5, randomDecision())
        res.put(6, randomDecision())
        res.put(7, randomDecision())
        res.put(8, randomDecision())
        res.put(9, randomDecision())
        res.put(10, randomDecision())
        res
    }

    def randomBool() {
        return RandomUtils.nextInt() % 2 == 0
    }

    def randomDecision() {
        return RandomUtils.nextInt() % 2 == 0 ? DecisionEnum.REJECTED : DecisionEnum.ACCEPTED;
    }
}