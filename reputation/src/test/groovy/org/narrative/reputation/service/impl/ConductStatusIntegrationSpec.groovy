package org.narrative.reputation.service.impl

import org.narrative.reputation.BaseIntegTestSpec
import org.narrative.reputation.model.entity.ConductStatusEntity
import org.narrative.reputation.model.entity.CurrentReputationEntity
import org.narrative.reputation.repository.ConductStatusRepository
import org.narrative.reputation.repository.CurrentReputationRepository
import org.narrative.reputation.service.ConductStatusCalculatorService
import org.narrative.shared.event.reputation.ConductEventType
import org.narrative.shared.event.reputation.ConductStatusEvent
import org.narrative.shared.reputation.config.redisson.ReputationRedissonQueueService
import org.redisson.api.RedissonClient
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

import java.time.Instant
import java.time.temporal.ChronoUnit

class ConductStatusIntegrationSpec extends BaseIntegTestSpec {

    @SpringBean
    RedissonClient redissonClient = Mock()

    @SpringBean
    ReputationRedissonQueueService redissonQueueService = Mock()

    @Autowired
    ConductStatusRepository conductStatusRepository

    @Autowired
    CurrentReputationRepository currentReputationRepository

    @Autowired
    ConductStatusCalculatorService conductStatusCalculatorService

    static final Instant now = Instant.now()

    @Transactional
    def "test conduct status for 1 bad thing just now"() {
        given:
            ConductStatusEvent conductStatusEvent = ConductStatusEvent.builder()
                    .conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE)
                    .userOid(1)
                    .eventTimestamp(now)
                    .build()
        when:
            conductStatusCalculatorService.calculateNegativeConductExpirationDate(conductStatusEvent)

        then:
            CurrentReputationEntity entity = currentReputationRepository.findById(1L).get()
            assert entity.getNegativeConductExpirationTimestamp() != null

            // Should be around 24 hours
            assert entity.getNegativeConductExpirationTimestamp().isAfter(now.plus(23, ChronoUnit.HOURS))
            assert entity.getNegativeConductExpirationTimestamp().isBefore(now.plus(25, ChronoUnit.HOURS))

    }

    @Transactional
    def "test conduct status for 1 bad thing a month"() {
        given:
            def entities = new ArrayList()

            3.times {
                ConductStatusEntity conductStatusEntity = ConductStatusEntity.builder()
                        .conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE)
                        .userOid(1)
                        .eventId(UUID.randomUUID())
                        .eventTimestamp(now.minus((it + 1L) * 30, ChronoUnit.DAYS))
                        .build()

                entities.add(conductStatusEntity)
            }

            conductStatusRepository.saveAll(entities)

            ConductStatusEvent conductStatusEvent = ConductStatusEvent.builder()
                    .conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE)
                    .userOid(1)
                    .eventTimestamp(now)
                    .build()
        when:
            conductStatusCalculatorService.calculateNegativeConductExpirationDate(conductStatusEvent)

        then:
            CurrentReputationEntity entity = currentReputationRepository.findById(1L).get()
            assert entity.getNegativeConductExpirationTimestamp() != null

            // Should be around 2.5 days
            assert entity.getNegativeConductExpirationTimestamp().isAfter(now.plus(61, ChronoUnit.HOURS))
            assert entity.getNegativeConductExpirationTimestamp().isBefore(now.plus(62, ChronoUnit.HOURS))

    }

    @Transactional
    def "test conduct status for 1 bad thing a day"() {
        given:

            3.times {
                ConductStatusEntity conductStatusEntity = ConductStatusEntity.builder()
                        .conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE)
                        .userOid(1)
                        .eventTimestamp(now.minus((it + 1L), ChronoUnit.DAYS))
                        .eventId(UUID.randomUUID())
                        .build()

                conductStatusRepository.save(conductStatusEntity)
            }

            ConductStatusEvent conductStatusEvent = ConductStatusEvent.builder()
                    .conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE)
                    .userOid(1)
                    .eventTimestamp(now)
                    .build()
        when:
            conductStatusCalculatorService.calculateNegativeConductExpirationDate(conductStatusEvent)

        then:
            CurrentReputationEntity entity = currentReputationRepository.findById(1L).get()
            assert entity.getNegativeConductExpirationTimestamp() != null

            // Should be around 10 days week
            assert entity.getNegativeConductExpirationTimestamp().isAfter(now.plus(9, ChronoUnit.DAYS))
            assert entity.getNegativeConductExpirationTimestamp().isBefore(now.plus(10, ChronoUnit.DAYS))

    }

    @Transactional
    def "test conduct status for 1 bad thing a day then KYC then another bad event"() {
        given:

            3.times {
                ConductStatusEntity conductStatusEntity = ConductStatusEntity.builder()
                        .conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE)
                        .userOid(1)
                        .eventTimestamp(now.minus((it + 1L), ChronoUnit.DAYS))
                        .eventId(UUID.randomUUID())
                        .build()

                conductStatusRepository.save(conductStatusEntity)

                // Throw in a bunch of events for another user
                conductStatusEntity = ConductStatusEntity.builder()
                        .conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE)
                        .userOid(2)
                        .eventTimestamp(now.minus((it + 1L), ChronoUnit.DAYS))
                        .eventId(UUID.randomUUID())
                        .build()

                conductStatusRepository.save(conductStatusEntity)

            }

            CurrentReputationEntity currentReputationEntity = CurrentReputationEntity.builder()
                    .userOid(1)
                    .kycVerified(true)
                    .kycVerifiedTimestamp(now.minus(1L, ChronoUnit.HOURS))
                    .build()

            currentReputationRepository.save(currentReputationEntity)

            ConductStatusEvent conductStatusEvent = ConductStatusEvent.builder()
                    .conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE)
                    .userOid(1)
                    .eventTimestamp(now)
                    .build()
        when:
            conductStatusCalculatorService.calculateNegativeConductExpirationDate(conductStatusEvent)

        then:
            CurrentReputationEntity entity = currentReputationRepository.findById(1L).get()
            assert entity.getNegativeConductExpirationTimestamp() != null

            // Should be around 1/2 day
            assert entity.getNegativeConductExpirationTimestamp().isAfter(now.plus(12, ChronoUnit.HOURS))
            assert entity.getNegativeConductExpirationTimestamp().isBefore(now.plus(13, ChronoUnit.HOURS))

    }

    @Transactional
    def "test conduct status for 1 bad thing for user one when user 2 also has bad things"() {
        given:
            // User 2's bad events
            3.times {
                ConductStatusEntity conductStatusEntity = ConductStatusEntity.builder()
                        .conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE)
                        .userOid(2)
                        .eventTimestamp(now.minus((it + 1L), ChronoUnit.DAYS))
                        .eventId(UUID.randomUUID())
                        .build()

                conductStatusRepository.save(conductStatusEntity)
            }

            ConductStatusEvent conductStatusEvent = ConductStatusEvent.builder()
                    .conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE)
                    .userOid(1)
                    .eventTimestamp(now)
                    .build()
        when:
            conductStatusCalculatorService.calculateNegativeConductExpirationDate(conductStatusEvent)

        then:
            CurrentReputationEntity entity = currentReputationRepository.findById(1L).get()
            assert entity.getNegativeConductExpirationTimestamp() != null

            // Should be around 1 day
            assert entity.getNegativeConductExpirationTimestamp().isAfter(now.plus(23, ChronoUnit.HOURS))
            assert entity.getNegativeConductExpirationTimestamp().isBefore(now.plus(25, ChronoUnit.HOURS))

    }
}