package org.narrative.reputation.model.entity

import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

class CurrentReputationEntityTest extends Specification {
    def "Test isConductNegative logic"() {
        def response

        given:
            CurrentReputationEntity currentReputationEntity = CurrentReputationEntity.builder()
                    .negativeConductExpirationTimestamp(negativeConductExpirationTimestamp)
                    .build()
        when:
            response = currentReputationEntity.isConductNegative()
        then:
            assert response == expectedResult
        where:
            negativeConductExpirationTimestamp       | expectedResult
            null                                     | false
            Instant.now().minus(1, ChronoUnit.HOURS) | false
            Instant.now().plus(1, ChronoUnit.HOURS) || true

    }
}
