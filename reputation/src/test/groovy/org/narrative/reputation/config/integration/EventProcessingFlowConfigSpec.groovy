package org.narrative.reputation.config.integration

import org.narrative.reputation.BaseIntegTestSpec
import org.narrative.reputation.service.*
import org.narrative.reputation.service.impl.KYCVerificationServiceImpl
import org.narrative.reputation.service.impl.TotalReputationScoreCalculatorServiceImpl
import org.narrative.shared.event.EventProcessedEvent
import org.narrative.shared.event.reputation.*
import org.narrative.shared.reputation.config.redisson.ReputationRedissonQueueService
import org.redisson.api.RedissonClient
import org.spockframework.spring.SpringBean
import org.spockframework.spring.SpringSpy
import org.spockframework.spring.UnwrapAopProxy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationContext
import org.springframework.integration.endpoint.SourcePollingChannelAdapter
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.GenericMessage
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.shaded.org.apache.commons.lang.math.RandomUtils

@Transactional
class EventProcessingFlowConfigSpec extends BaseIntegTestSpec {
    @Autowired
    ApplicationContext context

    @SpringBean
    RedissonClient redissonClient = Mock()
    @SpringBean
    ReputationRedissonQueueService redissonQueueService = Mock()
    @SpringSpy
    @UnwrapAopProxy
    EventManagementService eventManagementService
    @SpringSpy
    @UnwrapAopProxy
    TotalReputationScoreCalculatorServiceImpl reputationService
    @SpringSpy
    @UnwrapAopProxy
    ContentQualityCalculatorService contentQualityCalculatorService
    @SpringSpy
    @UnwrapAopProxy
    ConductStatusCalculatorService conductStatusCalculatorService;
    @SpringSpy
    @UnwrapAopProxy
    VoteCorrelationService voteCorrelationService
    @SpringSpy
    @UnwrapAopProxy
    RatingCorrelationService ratingCorrelationService
    @SpringSpy
    @UnwrapAopProxy
    KYCVerificationServiceImpl kycVerificationService
    @SpringSpy
    @UnwrapAopProxy
    UserOIDLockManager userOIDLockManager

    @Autowired
    @Qualifier("conductStatusEventFlow.org.springframework.integration.config.SourcePollingChannelAdapterFactoryBean#0")
    SourcePollingChannelAdapter sourcePollingChannelAdapter;

    def getFlowInputChannel(String queueName) {
        return context.getBean(queueName + "_input", MessageChannel.class);
    }

    def "Test duplicate event flow"() {
        given:
            ConductStatusEvent event = ConductStatusEvent.builder().userOid(423L).conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE).build();
            def queueName = event.getEventType().eventQueueName
            def message = new GenericMessage(event)
        when:
            getFlowInputChannel(queueName).send(message)
            getFlowInputChannel(queueName).send(message)
        then:
            2 * userOIDLockManager.createAndAcquireLock(event) >> event
            2 * eventManagementService.isDuplicateEvent(event)
    }

    //TODO: figure out a way to test this - must run as the poller for the errorChannel to be utilized
//    def "Test error flow" () {
//        given:
//            ConductStatusEvent event = ConductStatusEvent.builder().userOid(53L).severity(210).build();
//            def queueName = event.getEventType().eventQueueName
//            def message = new GenericMessage(event)
//        when:
//            sourcePollingChannelAdapter.createPoller().doPoll(message)
//        then:
//            1 * eventManagementService.isDuplicateEvent(event)
//            1 * reputationService.processConductStatusEvent(_) >> {
//                throw new RuntimeException()
//            }
//    }

    def "Test ConductStatusEvent flow"() {
        given:
            ConductStatusEvent event = ConductStatusEvent.builder().userOid(RandomUtils.nextLong()).conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE).build();
            def queueName = event.getEventType().eventQueueName
            def message = new GenericMessage(event)
        when:
            getFlowInputChannel(queueName).send(message)
        then:
            1 * userOIDLockManager.createAndAcquireLock(event) >> event
            1 * eventManagementService.isDuplicateEvent(event) >> false
            1 * conductStatusCalculatorService.calculateNegativeConductExpirationDate(event) >> event
            1 * reputationService.calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(_) >> { args ->
                def idSet = args[0] as Set<Long>
                assert idSet.size() == 1
                assert idSet.contains(event.userOid)
                null
            }
            1 * eventManagementService.markProcessedAndPublishEventProcessedEvent(_) >> { args ->
                callRealMethod()
                assert ((EventProcessedEvent) args[0]).eventId == event.eventId
            }
    }

    def "Test ContentLikeEvent flow"() {
        given:
            ContentLikeEvent event = ContentLikeEvent.builder().userOid(RandomUtils.nextLong()).likePoints(10).likeEventType(LikeEventType.DISLIKE).build();
            def queueName = event.getEventType().eventQueueName
            def message = new GenericMessage(event)
        when:
            getFlowInputChannel(queueName).send(message)
        then:
            1 * userOIDLockManager.createAndAcquireLock(event) >> event
            1 * eventManagementService.isDuplicateEvent(event) >> false
            1 * contentQualityCalculatorService.updateContentQualityWithEvent(event) >> event
            1 * reputationService.calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(_) >> { args ->
                def idSet = args[0] as Set<Long>
                assert idSet.size() == 1
                assert idSet.contains(event.userOid)
                null
            }
            1 * eventManagementService.markProcessedAndPublishEventProcessedEvent(_) >> { args ->
                callRealMethod()
                assert ((EventProcessedEvent) args[0]).eventId == event.eventId
            }
    }

    def "Test CommentLikeEvent flow"() {
        given:
            CommentLikeEvent event = CommentLikeEvent.builder().userOid(RandomUtils.nextLong()).likePoints(10).likeEventType(LikeEventType.DISLIKE).build();
            def queueName = event.getEventType().eventQueueName
            def message = new GenericMessage(event)
        when:
            getFlowInputChannel(queueName).send(message)
        then:
            1 * userOIDLockManager.createAndAcquireLock(event) >> event
            1 * eventManagementService.isDuplicateEvent(event) >> false
            1 * contentQualityCalculatorService.updateContentQualityWithEvent(event) >> event
            1 * reputationService.calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(_) >> { args ->
                def idSet = args[0] as Set<Long>
                assert idSet.size() == 1
                assert idSet.contains(event.userOid)
                null
            }
            1 * eventManagementService.markProcessedAndPublishEventProcessedEvent(_) >> { args ->
                callRealMethod()
                assert ((EventProcessedEvent) args[0]).eventId == event.eventId
            }
    }

    def "Test KYCVerificationEvent flow"() {
        given:
            KYCVerificationEvent event = KYCVerificationEvent.builder().userOid(RandomUtils.nextLong()).isVerified(true).build();
            def queueName = event.getEventType().eventQueueName
            def message = new GenericMessage(event)
        when:
            getFlowInputChannel(queueName).send(message)
        then:
            1 * userOIDLockManager.createAndAcquireLock(event) >> event
            1 * eventManagementService.isDuplicateEvent(event) >> false
            1 * kycVerificationService.updateCurrentReputationWithKYCVerificationEvent(event) >> event
            1 * reputationService.calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(_) >> { args ->
                def idSet = args[0] as Set<Long>
                assert idSet.size() == 1
                assert idSet.contains(event.userOid)
                null
            }
            1 * eventManagementService.markProcessedAndPublishEventProcessedEvent(_) >> { args ->
                callRealMethod()
                assert ((EventProcessedEvent) args[0]).eventId == event.eventId
            }
    }

    def "Test RatingEvent flow"() {
        given:
            RatingEvent event = RatingEvent.builder().userOid(RandomUtils.nextLong()).ratedWithConsensus(false).build();
            def queueName = event.getEventType().eventQueueName
            def message = new GenericMessage(event)
        when:
            getFlowInputChannel(queueName).send(message)
        then:
            1 * userOIDLockManager.createAndAcquireLock(event) >> event
            1 * eventManagementService.isDuplicateEvent(event) >> false
            1 * ratingCorrelationService.updateRatingCorrelationWithRatingEvent(event) >> event
            1 * reputationService.calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(_) >> { args ->
                def idSet = args[0] as Set<Long>
                assert idSet.size() == 1
                assert idSet.contains(event.userOid)
                null
            }
            1 * eventManagementService.markProcessedAndPublishEventProcessedEvent(_) >> { args ->
                callRealMethod()
                assert ((EventProcessedEvent) args[0]).eventId == event.eventId
            }
    }

    def "Test RatingConsensusChangedEvent flow"() {
        given:
            def pairMap = [1234: true, 4567: false, 5678: true]
            ConsensusChangedEvent event = ConsensusChangedEvent.builder().usersConsensusChangedMap(pairMap).build();
            def queueName = event.getEventType().eventQueueName
            def message = new GenericMessage(event)
        when:
            getFlowInputChannel(queueName).send(message)
        then:
            1 * userOIDLockManager.createAndAcquireLock(event) >> event
            1 * eventManagementService.isDuplicateEvent(event) >> false
            1 * ratingCorrelationService.updateRatingCorrelationWithRatingConsensusChangedEvent(event) >> event
            1 * reputationService.calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(_) >> { args ->
                def idSet = args[0] as Set<Long>
                assert idSet.size() == pairMap.size()
                assert idSet.containsAll(pairMap.keySet())
                null
            }
            1 * eventManagementService.markProcessedAndPublishEventProcessedEvent(_) >> { args ->
                callRealMethod()
                assert ((EventProcessedEvent) args[0]).eventId == event.eventId
            }
    }

    def "Test VoteEndedEvent flow"() {
        given:
            def pairMap = [1234: true, 4567: false, 5678: true]
            VoteEndedEvent event = VoteEndedEvent.builder().userVotesMap(pairMap).build();
            def queueName = event.getEventType().eventQueueName
            def message = new GenericMessage(event)
        when:
            getFlowInputChannel(queueName).send(message)
        then:
            1 * userOIDLockManager.createAndAcquireLock(event) >> event
            1 * eventManagementService.isDuplicateEvent(event) >> false
            1 * voteCorrelationService.updateVoteCorrelationWithEvent(event) >> event
            1 * reputationService.calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(_) >> { args ->
                def idSet = args[0] as Set<Long>
                assert idSet.size() == pairMap.size()
                assert idSet.containsAll(pairMap.keySet())
                null
            }
            1 * eventManagementService.markProcessedAndPublishEventProcessedEvent(_) >> { args ->
                callRealMethod()
                assert ((EventProcessedEvent) args[0]).eventId == event.eventId
            }
    }

    def "Test NegativeQuality flow"() {
        given:
            NegativeQualityEvent event = NegativeQualityEvent.builder().userOid(22L).negativeQualityEventType(NegativeQualityEventType.CHANGE_REQUEST_DENIED_BY_TRIBUNAL).build();
            def queueName = event.getEventType().eventQueueName
            def message = new GenericMessage(event)
        when:
            getFlowInputChannel(queueName).send(message)
        then:
            1 * userOIDLockManager.createAndAcquireLock(event) >> event
            1 * eventManagementService.isDuplicateEvent(event) >> false
            1 * voteCorrelationService.updateVoteCorrelationForNegativeEvent(event) >> event
            1 * reputationService.calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(_) >> { args ->
                def idSet = args[0] as Set<Long>
                assert idSet.size() == 1
                assert idSet.contains(22L)
                null
            }
            1 * eventManagementService.markProcessedAndPublishEventProcessedEvent(_) >> { args ->
                callRealMethod()
                assert ((EventProcessedEvent) args[0]).eventId == event.eventId
            }
    }
}
