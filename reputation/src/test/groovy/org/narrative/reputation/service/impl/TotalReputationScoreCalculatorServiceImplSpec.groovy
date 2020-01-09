package org.narrative.reputation.service.impl

import org.narrative.reputation.config.ReputationProperties
import org.narrative.reputation.model.entity.CurrentReputationEntity
import org.narrative.reputation.repository.CurrentReputationRepository
import org.narrative.reputation.repository.FollowerQualityRepository
import org.narrative.reputation.service.ContentQualityCalculatorService
import org.narrative.reputation.service.RatingCorrelationService
import org.narrative.reputation.service.VoteCorrelationService
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
class TotalReputationScoreCalculatorServiceImplSpec extends Specification {


    // Mocks
    CurrentReputationRepository currentReputationRepository = Mock()
    ContentQualityCalculatorService contentQualityCalculatorService = Mock()
    VoteCorrelationService voteCorrelationService = Mock()
    RatingCorrelationService ratingCorrelationService = Mock()
    FollowerQualityRepository followerQualityRepository = Mock()

    ReputationProperties reputationProperties = new ReputationProperties()

    def "test calculateTotalScoreAndUpdateCurrentReputationEntityForUsers"() {
        given:
            def totalReputationScoreCalculatorService =
                    Spy(TotalReputationScoreCalculatorServiceImpl, constructorArgs: [reputationProperties,
                                                                                     currentReputationRepository,
                                                                                     contentQualityCalculatorService,
                                                                                     voteCorrelationService,
                                                                                     ratingCorrelationService,
                                                                                     followerQualityRepository])

            currentReputationRepository.findById(_ as Long) >> Optional.empty()

            totalReputationScoreCalculatorService.getTotalScore(_ as CurrentReputationEntity) >> 0

        when:
            totalReputationScoreCalculatorService.calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(oids.toSet())
        then:
            1 * currentReputationRepository.save(*_) >> { CurrentReputationEntity savedCurrentReputationEntity ->
                assert savedCurrentReputationEntity.getUserOid() == oids[0]
            }
            1 * currentReputationRepository.save(*_) >> { CurrentReputationEntity savedCurrentReputationEntity ->
                assert savedCurrentReputationEntity.getUserOid() == oids[1]
            }
            1 * currentReputationRepository.save(*_) >> { CurrentReputationEntity savedCurrentReputationEntity ->
                assert savedCurrentReputationEntity.getUserOid() == oids[2]
            }
        where:
            oids << [[1L, 2L, 3L]]

    }

    def "test getTotalScore"() {
        given:


            def totalReputationScoreCalculatorService =
                    Spy(TotalReputationScoreCalculatorServiceImpl, constructorArgs: [reputationProperties,
                                                                                     currentReputationRepository,
                                                                                     contentQualityCalculatorService,
                                                                                     voteCorrelationService,
                                                                                     ratingCorrelationService,
                                                                                     followerQualityRepository])

            CurrentReputationEntity currentReputationEntity = CurrentReputationEntity.builder()
                    .userOid(1)
                    .kycVerified(kycVerified)
                    .negativeConductExpirationTimestamp(negativeConductExpirationTimestamp)
                    .build()

            totalReputationScoreCalculatorService.qualityAnalysisComponent(_) >> qualityAnalysisComponent
        when:
            def score = totalReputationScoreCalculatorService.getTotalScore(currentReputationEntity)

        then:
            score == totalScore

        where:
            qualityAnalysisComponent | kycVerified | negativeConductExpirationTimestamp      || totalScore
            50.4                     | false       | Instant.now().minus(1, ChronoUnit.DAYS) || 40
            100                      | false       | Instant.now().minus(1, ChronoUnit.DAYS) || 70
            99.7566545               | false       | Instant.now().minus(1, ChronoUnit.DAYS) || 70
            66.9                     | false       | Instant.now().minus(1, ChronoUnit.DAYS) || 50
            50                       | true        | Instant.now().minus(1, ChronoUnit.DAYS) || 70
            100                      | true        | Instant.now().minus(1, ChronoUnit.DAYS) || 100
            67                       | true        | Instant.now().minus(1, ChronoUnit.DAYS) || 80
            50.4                     | false       | Instant.now().plus(1, ChronoUnit.DAYS) || 30
            100                      | false       | Instant.now().plus(1, ChronoUnit.DAYS) || 60
            99.7566545               | false       | Instant.now().plus(1, ChronoUnit.DAYS) || 60
            66.9                     | false       | Instant.now().plus(1, ChronoUnit.DAYS) || 40
            50                       | true        | Instant.now().plus(1, ChronoUnit.DAYS) || 60
            100                      | true        | Instant.now().plus(1, ChronoUnit.DAYS) || 90
            67                       | true        | Instant.now().plus(1, ChronoUnit.DAYS) || 70
    }

    def "test qualityAnalysisComponent"() {
        given:
            def totalReputationScoreCalculatorService =
                    Spy(TotalReputationScoreCalculatorServiceImpl, constructorArgs: [reputationProperties,
                                                                                     currentReputationRepository,
                                                                                     contentQualityCalculatorService,
                                                                                     voteCorrelationService,
                                                                                     ratingCorrelationService,
                                                                                     followerQualityRepository])

            totalReputationScoreCalculatorService.getFollowerQuality(_) >> followerQuality
            contentQualityCalculatorService.getContentQualityScoreForUser(_) >> contentQualityScoreForUser
            voteCorrelationService.getVoteCorrelationScoreForUser(_) >> voteCorrelationScoreForUser
            ratingCorrelationService.getRatingCorrelationScoreForUser(_) >> ratingCorrelationScoreForUser
        when:
            def score = totalReputationScoreCalculatorService.qualityAnalysisComponent(1)

        then:
            score == qualityAnalysisComponent

        where:
            followerQuality | contentQualityScoreForUser | voteCorrelationScoreForUser | ratingCorrelationScoreForUser || qualityAnalysisComponent
            50              | 50                         | 50                          | 50                            || 50
            10              | 10                         | 10                          | 10                            || 10
            100             | 100                        | 100                         | 100                           || 100
            75              | 67                         | 75                          | 67                            || 69.8

    }

}
