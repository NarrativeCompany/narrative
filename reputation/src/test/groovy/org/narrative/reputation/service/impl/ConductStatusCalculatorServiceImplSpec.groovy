package org.narrative.reputation.service.impl

import org.narrative.reputation.model.entity.ConductStatusEntity
import org.narrative.reputation.model.entity.CurrentReputationEntity
import org.narrative.reputation.repository.ConductStatusRepository
import org.narrative.reputation.repository.CurrentReputationRepository
import org.narrative.reputation.service.ConductStatusCalculatorService
import org.narrative.shared.event.reputation.ConductEventType
import org.narrative.shared.event.reputation.ConductStatusEvent
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

class ConductStatusCalculatorServiceImplSpec extends Specification {
    static final Instant now = Instant.now()

    def "test writeConductStatusEventToDB"() {
        given:
            ConductStatusRepository conductStatusRepository = Mock()
            ConductStatusCalculatorService conductStatusCalculatorService = new ConductStatusCalculatorServiceImpl(conductStatusRepository, null, null)

            ConductStatusEvent conductStatusEvent = ConductStatusEvent.builder()
                    .userOid(1)
                    .conductEventType(ConductEventType.PAYMENT_CHARGEBACK)
                    .eventTimestamp(Instant.EPOCH)
                    .build()


        when:
            conductStatusCalculatorService.writeConductStatusEventToDB(conductStatusEvent)

        then:
            1 * conductStatusRepository.save(*_) >> {
                ConductStatusEntity conductStatusEntity ->
                    assert conductStatusEntity.getUserOid() == 1
                    assert conductStatusEntity.getConductEventType() == ConductEventType.PAYMENT_CHARGEBACK
                    assert conductStatusEntity.getEventTimestamp() == Instant.EPOCH
            }
    }

    def "test getConductStatusEntitiesForUser for user with no current reputation entity"() {
        given:
            ConductStatusRepository conductStatusRepository = Mock()
            CurrentReputationRepository currentReputationRepository = Mock()
            ConductStatusCalculatorService conductStatusCalculatorService = new ConductStatusCalculatorServiceImpl(conductStatusRepository, currentReputationRepository, null)

            currentReputationRepository.findById(_) >> Optional.empty()
        when:
            ConductStatusEvent event = Mock()
            conductStatusCalculatorService.getConductStatusEntitiesForUser(event)


        then:
            1 * currentReputationRepository.save(*_)
            1 * conductStatusRepository.findByUserOid(_)

    }

    def "test getConductStatusEntitiesForUser for user with current reputation entity and KYC"() {
        given:
            ConductStatusRepository conductStatusRepository = Mock()
            CurrentReputationRepository currentReputationRepository = Mock()
            ConductStatusCalculatorService conductStatusCalculatorService = new ConductStatusCalculatorServiceImpl(conductStatusRepository, currentReputationRepository, null)

            CurrentReputationEntity currentReputationEntity = CurrentReputationEntity.builder()
                    .kycVerified(true)
                    .build()
            currentReputationRepository.findById(_) >> Optional.of(currentReputationEntity)
        when:
            ConductStatusEvent event = Mock()
            conductStatusCalculatorService.getConductStatusEntitiesForUser(event)


        then:
            1 * conductStatusRepository.findByUserOidAndEventTimestampAfter(_, _)

    }


    def "test calculateWeightedSeveritySum for empty list"() {
        given:
            ConductStatusCalculatorService conductStatusCalculatorService = new ConductStatusCalculatorServiceImpl(null, null, java.time.Clock.systemUTC())
            def sum
        when:
            sum = conductStatusCalculatorService.calculateWeightedSeveritySum(new ArrayList<ConductStatusEntity>())
        then:
            assert sum == 0
    }

    def "test calculateWeightedSeveritySum"() {
        given:
            ConductStatusCalculatorService conductStatusCalculatorService = new ConductStatusCalculatorServiceImpl(null, null, java.time.Clock.systemUTC())
        when:
            def sum = conductStatusCalculatorService.calculateWeightedSeveritySum(eventList)
        then:
            assert Math.round(sum * 10000) / 10000 == expectedRoundedSum
        where:
            eventList || expectedRoundedSum
            buildEntityListFromEventList([
                    new Tuple2(0, ConductEventType.FAILURE_TO_PAY_FOR_NICHE),
                    new Tuple2(31, ConductEventType.FAILURE_TO_PAY_FOR_NICHE),
                    new Tuple2(61, ConductEventType.FAILURE_TO_PAY_FOR_NICHE),
                    new Tuple2(92, ConductEventType.FAILURE_TO_PAY_FOR_NICHE)
            ])        || 2.2802
            buildEntityListFromEventList([
                    new Tuple2(0, ConductEventType.FAILURE_TO_PAY_FOR_NICHE),
            ])        || 1.000
            buildEntityListFromEventList([
                    new Tuple2(0, ConductEventType.FAILURE_TO_PAY_FOR_NICHE),
                    new Tuple2(3, ConductEventType.FAILURE_TO_PAY_FOR_NICHE),
                    new Tuple2(5, ConductEventType.FAILURE_TO_PAY_FOR_NICHE),
                    new Tuple2(7, ConductEventType.FAILURE_TO_PAY_FOR_NICHE),
            ])        || 3.7841
    }

    List<ConductStatusEntity> buildEntityListFromEventList(List<Tuple2<Long, ConductEventType>> tuples) {
        ArrayList<ConductStatusEntity> entityList = new ArrayList<>(tuples.size())

        // With each Tuple, build a ConductStatusEntity
        for (Tuple2<Long, ConductEventType> tuple in tuples) {
            entityList.add(
                    (ConductStatusEntity) ConductStatusEntity.builder()
                            .eventTimestamp(Instant.now().minus(tuple.getFirst(), ChronoUnit.DAYS))
                            .conductEventType(tuple.getSecond())
                            .build())
        }

        return entityList
    }

    @Unroll
    def "test calculatePenaltyTime"() {
        given:
            ConductStatusCalculatorService conductStatusCalculatorService = new ConductStatusCalculatorServiceImpl(null, null, null)
        when:
            def penaltyTime = conductStatusCalculatorService.calculatePenaltyTime(weightedSeveritySum)
        then:
            assert Math.round(penaltyTime * 10000) / 10000 == expectedRoundedPenaltyTime
        where:
            weightedSeveritySum || expectedRoundedPenaltyTime
            1.992               || 1.8462
            1.000               || 1.028
            3.716               || 8.6669
            99999               || 180.0
    }

    @Unroll
    def "test applyPenaltyTimeToUser"() {

        given:
            ConductStatusRepository conductStatusRepository = Mock()
            CurrentReputationRepository currentReputationRepository = Mock()
            Clock clock = Mock()

            clock.instant() >> now

            ConductStatusCalculatorService conductStatusCalculatorService = new ConductStatusCalculatorServiceImpl(conductStatusRepository, currentReputationRepository, clock)


            CurrentReputationEntity currentReputationEntity = CurrentReputationEntity.builder()
                    .kycVerified(kycVerified)
                    .negativeConductExpirationTimestamp(existingNegativeConductExpirationDate)
                    .build()
            currentReputationRepository.findById(_) >> Optional.of(currentReputationEntity)
        when:
            conductStatusCalculatorService.applyPenaltyTimeToUser(1, penaltyTime, now)
        then:
            1 * currentReputationRepository.save(*_) >> {
                CurrentReputationEntity entity -> assert entity.getNegativeConductExpirationTimestamp() == expectedNegativeConductExpirationDate
            }
        where:
            kycVerified | penaltyTime | existingNegativeConductExpirationDate || expectedNegativeConductExpirationDate
            false       | 1           | null                                  || now.plus(1, ChronoUnit.DAYS)
            false       | 0.5         | null                                  || now.plus(12, ChronoUnit.HOURS)
            true        | 1           | null                                  || now.plus(12, ChronoUnit.HOURS)
            false       | 1           | now.minus(7, ChronoUnit.DAYS)         || now.plus(1, ChronoUnit.DAYS)
            true        | 1           | now.minus(7, ChronoUnit.DAYS)         || now.plus(12, ChronoUnit.HOURS)
            false       | 1           | now.plus(7, ChronoUnit.DAYS)          || now.plus(8, ChronoUnit.DAYS)
            true        | 2           | now.plus(7, ChronoUnit.DAYS)          || now.plus(8, ChronoUnit.DAYS)
            false       | 200         | now.plus(7, ChronoUnit.DAYS)          || now.plus(180, ChronoUnit.DAYS)
    }
}
