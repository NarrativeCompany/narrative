package org.narrative.reputation.service.impl


import org.narrative.reputation.model.entity.VoteCorrelationEntity
import org.narrative.reputation.repository.VoteCorrelationRepository
import org.narrative.reputation.service.VoteCorrelationService
import org.narrative.shared.event.reputation.DecisionEnum
import org.narrative.shared.event.reputation.NegativeQualityEvent
import org.narrative.shared.event.reputation.NegativeQualityEventType
import org.narrative.shared.event.reputation.VoteEndedEvent
import spock.lang.Specification
import spock.lang.Unroll

class VoteCorrelationServiceImplSpec extends Specification {
    def "test updateVoteCorrelationWithEvent for 2 users"() {
        given:
            VoteCorrelationRepository voteCorrelationRepository = Mock()
            VoteCorrelationService voteCorrelationService = new VoteCorrelationServiceImpl(voteCorrelationRepository)

            voteCorrelationRepository.findById(_) >> Optional.empty()

        when:
            voteCorrelationService.updateVoteCorrelationWithEvent(voteEndedEvent)
        then:
            1 * voteCorrelationRepository.save(*_) >> { VoteCorrelationEntity voteCorrelationEntity ->
                assert voteCorrelationEntity.userOid == 1L
                assert voteCorrelationEntity.majorityVoteCount == user1MajorityVoteCount
                assert voteCorrelationEntity.totalVoteCount == totalVoteCount

            }
            1 * voteCorrelationRepository.save(*_) >> { VoteCorrelationEntity voteCorrelationEntity ->
                assert voteCorrelationEntity.userOid == 2L
                assert voteCorrelationEntity.majorityVoteCount == user2MajorityVoteCount
                assert voteCorrelationEntity.totalVoteCount == totalVoteCount

            }
        where:
            voteEndedEvent                                                                                     || user1MajorityVoteCount | user2MajorityVoteCount | totalVoteCount
            buildVoteEndedEvent(DecisionEnum.ACCEPTED, [1L: DecisionEnum.ACCEPTED, 2L: DecisionEnum.REJECTED]) || 1                      | 0                      | 1
            buildVoteEndedEvent(DecisionEnum.ACCEPTED, [1L: DecisionEnum.REJECTED, 2L: DecisionEnum.ACCEPTED]) || 0                      | 1                      | 1
            buildVoteEndedEvent(DecisionEnum.ACCEPTED, [1L: DecisionEnum.REJECTED, 2L: DecisionEnum.REJECTED]) || 0                      | 0                      | 1
            buildVoteEndedEvent(DecisionEnum.REJECTED, [1L: DecisionEnum.ACCEPTED, 2L: DecisionEnum.REJECTED]) || 0                      | 1                      | 1
            buildVoteEndedEvent(DecisionEnum.REJECTED, [1L: DecisionEnum.REJECTED, 2L: DecisionEnum.REJECTED]) || 1                      | 1                      | 1
            buildVoteEndedEvent(DecisionEnum.REJECTED, [1L: DecisionEnum.ACCEPTED, 2L: DecisionEnum.ACCEPTED]) || 0                      | 0                      | 1
    }

    @Unroll
    def "test updateVoteCorrelationForNegativeEvent #expectedTotal #expectedMajority #entity" () {
        given:
            VoteCorrelationRepository voteCorrelationRepository = Mock()
            VoteCorrelationService voteCorrelationService = new VoteCorrelationServiceImpl(voteCorrelationRepository)
        when:
            voteCorrelationService.updateVoteCorrelationForNegativeEvent(
                    NegativeQualityEvent.builder().negativeQualityEventType(NegativeQualityEventType.CHANGE_REQUEST_DENIED_BY_TRIBUNAL).build()
            )
        then:
            1 * voteCorrelationRepository.findById(_) >>> [Optional.of(entity)]

            entity.totalVoteCount == expectedTotal
            entity.majorityVoteCount == expectedMajority
        where:
            entity || expectedMajority | expectedTotal
            VoteCorrelationEntity.builder().userOid(-1).majorityVoteCount(0).totalVoteCount(0).build() | 0 | 10
            VoteCorrelationEntity.builder().userOid(-1).majorityVoteCount(5).totalVoteCount(10).build() | 0 | 15
            VoteCorrelationEntity.builder().userOid(-1).majorityVoteCount(10).totalVoteCount(10).build() | 0 | 10
            VoteCorrelationEntity.builder().userOid(-1).majorityVoteCount(15).totalVoteCount(10).build() | 5 | 10
            VoteCorrelationEntity.builder().userOid(-1).majorityVoteCount(20).totalVoteCount(10).build() | 10 | 10
            VoteCorrelationEntity.builder().userOid(-1).majorityVoteCount(25).totalVoteCount(10).build() | 15 | 10
    }

    VoteEndedEvent buildVoteEndedEvent(DecisionEnum decision, Map<Long, DecisionEnum> userVotesMap) {
        return VoteEndedEvent.builder()
                .referendumId(1L)
                .decision(decision)
                .userVotesMap(userVotesMap)
                .build()
    }

    @Unroll
    def "test getVoteCorrelationScoreForUser"() {

        given:
            VoteCorrelationRepository voteCorrelationRepository = Mock()
            VoteCorrelationService voteCorrelationServicee = new VoteCorrelationServiceImpl(voteCorrelationRepository)

            voteCorrelationRepository.findById(_) >> Optional.of(VoteCorrelationEntity.builder()
                    .userOid(1)
                    .majorityVoteCount(majorityVoteCount)
                    .totalVoteCount(totalVoteCount)
                    .build()
            )

        when:
            def score = voteCorrelationServicee.getVoteCorrelationScoreForUser(1)
        then:
            score == voteCorrelationScoreForUser
        where:
            majorityVoteCount | totalVoteCount || voteCorrelationScoreForUser
            1                 | 1              || 0
            6                 | 10             || 15
            52                | 100            || 52
            100               | 400            || 25
            100               | 1000           || 10
            50                | 1000           || 5
            1000              | 1000           || 100

    }
}