package org.narrative.reputation.service.impl

import org.narrative.reputation.model.entity.RatingCorrelationEntity
import org.narrative.reputation.repository.RatingCorrelationRepository
import org.narrative.reputation.service.RatingCorrelationService
import org.narrative.shared.event.reputation.ConsensusChangedEvent
import org.narrative.shared.event.reputation.RatingEvent
import org.narrative.shared.event.reputation.RatingType
import spock.lang.Specification
import spock.lang.Unroll

class RatingCorrelationServiceImplSpec extends Specification {
    def "test updateRatingCorrelationWithRatingEvent"() {
        given:
            RatingCorrelationRepository ratingCorrelationRepository = Mock()
            RatingCorrelationService ratingCorrelationService = new RatingCorrelationServiceImpl(ratingCorrelationRepository)

            ratingCorrelationRepository.findById(_) >> Optional.empty()

            RatingEvent ratingEvent = RatingEvent.builder()
                    .userOid(1)
                    .revote(revote)
                    .ratedWithConsensus(ratedWithConsensus)
                    .build()

        when:
            ratingCorrelationService.updateRatingCorrelationWithRatingEvent(ratingEvent)
        then:
            1 * ratingCorrelationRepository.save(*_) >> { RatingCorrelationEntity ratingCorrelationEntity ->
                assert ratingCorrelationEntity.userOid == 1
                assert ratingCorrelationEntity.getTotalVoteCount() == totalVoteCount
                assert ratingCorrelationEntity.getMajorityVoteCount() == majorityVoteCount
            }
        where:
            revote | ratedWithConsensus || totalVoteCount | majorityVoteCount
            false  | true               || 1              | 1
            false  | false              || 1              | 0
            true   | true               || 0              | 1
            true   | false              || 0              | 0
    }

    def "test updateRatingCorrelationWithRatingConsensusChangedEvent"() {
        given:
            RatingCorrelationRepository ratingCorrelationRepository = Mock()
            RatingCorrelationService ratingCorrelationService = new RatingCorrelationServiceImpl(ratingCorrelationRepository)

            ratingCorrelationRepository.findById(_) >> Optional.empty()

        when:
            ratingCorrelationService.updateRatingCorrelationWithRatingConsensusChangedEvent(ratingConsensusChangedEvent)
        then:
            1 * ratingCorrelationRepository.save(*_) >> { RatingCorrelationEntity ratingCorrelationEntity ->
                assert ratingCorrelationEntity.userOid == 1L
                assert ratingCorrelationEntity.majorityVoteCount == user1MajorityVoteCount
                assert ratingCorrelationEntity.totalVoteCount == totalVoteCount

            }
            1 * ratingCorrelationRepository.save(*_) >> { RatingCorrelationEntity ratingCorrelationEntity ->
                assert ratingCorrelationEntity.userOid == 2L
                assert ratingCorrelationEntity.majorityVoteCount == user2MajorityVoteCount
                assert ratingCorrelationEntity.totalVoteCount == totalVoteCount

            }
        where:
            ratingConsensusChangedEvent                                                  || user1MajorityVoteCount | user2MajorityVoteCount | totalVoteCount
            buildRatingConsensusChangedEvent(RatingType.QUALITY, [1L: true, 2L: true])   || 1                      | 1                      | 0
            buildRatingConsensusChangedEvent(RatingType.QUALITY, [1L: true, 2L: false])  || 1                      | -1                     | 0
            buildRatingConsensusChangedEvent(RatingType.QUALITY, [1L: false, 2L: true])  || -1                     | 1                      | 0
            buildRatingConsensusChangedEvent(RatingType.QUALITY, [1L: false, 2L: false]) || -1                     | -1                     | 0
    }

    ConsensusChangedEvent buildRatingConsensusChangedEvent(RatingType ratingType, Map<Long, Boolean> usersConsensusChangedMap) {
        return ConsensusChangedEvent.builder()
                .usersConsensusChangedMap(usersConsensusChangedMap)
                .build()
    }

    @Unroll
    def "test getRatingCorrelationScoreForUser"() {

        given:
            RatingCorrelationRepository ratingCorrelationRepository = Mock()
            RatingCorrelationService ratingCorrelationService = new RatingCorrelationServiceImpl(ratingCorrelationRepository)

            ratingCorrelationRepository.findById(_) >> Optional.of(RatingCorrelationEntity.builder()
                    .userOid(1)
                    .majorityVoteCount(majorityVoteCount)
                    .totalVoteCount(totalVoteCount)
                    .build()
            )

        when:
            def score = ratingCorrelationService.getRatingCorrelationScoreForUser(1)
        then:
            score == ratingCorrelationScoreForUser
        where:
            majorityVoteCount | totalVoteCount || ratingCorrelationScoreForUser
            1                 | 1              || 0
            6                 | 10             || 15
            52                | 100            || 52
            100               | 400            || 25
            100               | 1000           || 10
            50                | 1000           || 5
            1000              | 1000           || 100

    }
}
