package org.narrative.reputation.service.impl

import org.narrative.reputation.model.entity.CurrentReputationEntity
import org.narrative.reputation.repository.CurrentReputationRepository
import org.narrative.shared.event.reputation.KYCVerificationEvent
import spock.lang.Shared
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

class KYCVerificationServiceImplSpec extends Specification {
    @Shared
    Instant now = Instant.now()

    def "test updateCurrentReputationWithKYCVerificationEvent"() {

        given:
            CurrentReputationRepository currentReputationRepository = Mock()
            Clock clock = Mock()
            clock.instant() >> now

            KYCVerificationServiceImpl kycVerificationService = new KYCVerificationServiceImpl(currentReputationRepository, clock)

            CurrentReputationEntity entity = CurrentReputationEntity.builder()
                    .negativeConductExpirationTimestamp(negativeConductExpirationTimestamp)
                    .userOid(1)
                    .build()

            currentReputationRepository.findById(_) >> Optional.of(entity)

            KYCVerificationEvent kycVerificationEvent = KYCVerificationEvent.builder()
                    .userOid(1)
                    .isVerified(isVerified)
                    .build()

        when:
            kycVerificationService.updateCurrentReputationWithKYCVerificationEvent(kycVerificationEvent)
        then:
            1 * currentReputationRepository.save(*_) >> { CurrentReputationEntity currentReputationEntity ->
                assert currentReputationEntity.isKycVerified() == isKycVerified
                assert currentReputationEntity.userOid == 1

                // negativeConductExpirationTimestamp should be now
                assert currentReputationEntity.negativeConductExpirationTimestamp == newNegativeConductExpirationTimestamp
            }
        where:
            isVerified | negativeConductExpirationTimestamp             | isKycVerified || newNegativeConductExpirationTimestamp
            true       | now.plus(2, ChronoUnit.HOURS)     | true          || now
            false      | now.minus(2, ChronoUnit.HOURS) | false         || now.minus(2, ChronoUnit.HOURS)
    }

    def "test updateCurrentReputationWithKYCVerificationEvent with null entity"() {

        given:
            CurrentReputationRepository currentReputationRepository = Mock()
            Clock clock = Mock()
            clock.instant() >> now

            KYCVerificationServiceImpl kycVerificationService = new KYCVerificationServiceImpl(currentReputationRepository, clock)

            currentReputationRepository.findById(_) >> Optional.empty()

            KYCVerificationEvent kycVerificationEvent = KYCVerificationEvent.builder()
                    .userOid(1)
                    .isVerified(isVerified)
                    .build()

        when:
            kycVerificationService.updateCurrentReputationWithKYCVerificationEvent(kycVerificationEvent)
        then:
            1 * currentReputationRepository.save(*_) >> { CurrentReputationEntity currentReputationEntity ->
                assert currentReputationEntity.isKycVerified() == isKycVerified
                assert currentReputationEntity.userOid == 1

                // negativeConductExpirationTimestamp should be now
                assert currentReputationEntity.negativeConductExpirationTimestamp == newNegativeConductExpirationTimestamp
            }
        where:
            isVerified | negativeConductExpirationTimestamp | isKycVerified || newNegativeConductExpirationTimestamp
            true       | null                               | true          || null
            false      | null                               | false         || null
    }

}
