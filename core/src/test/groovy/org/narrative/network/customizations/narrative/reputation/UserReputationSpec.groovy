package org.narrative.network.customizations.narrative.reputation

import spock.lang.Specification

class UserReputationSpec extends Specification {
    def "test getVotePointsMultiplier"() {
        given:
            def userReputation = Spy(UserReputation) {
                getTotalScore() >> totalScore
            }
        when:
            BigDecimal multiplier = userReputation.getVotePointsMultiplier()
        then:
            assert multiplier == expectedMultiplier
        where:
            totalScore || expectedMultiplier
            100        || 0.01
            96         || 0.01
            89         || 0.008
            85         || 0.008
            84         || 0.006
            80         || 0.006
            79         || 0.005
            75         || 0.005
            74         || 0.004
            70         || 0.004
            69         || 0.002
            60         || 0.002
            59         || 0.001
            0          || 0.001
    }

    def "test getAdjustedVotePoints"() {
        given:
            def userReputation = Spy(UserReputation) {
                getTotalScore() >> totalScore
            }
        when:
            int adjustedVotePoints = userReputation.getAdjustedVotePoints()
        then:
            assert adjustedVotePoints == expectedResult
        where:
            totalScore || expectedResult
            100        || 100
            96         || 96
            89         || 71
            85         || 68
            84         || 50
            80         || 48
            79         || 40
            75         || 38
            74         || 30
            70         || 28
            69         || 14
            60         || 12
            59         || 6
            54         || 5
            0          || 1

    }


    def "test get reputation level"() {
        given:
            def userReputation = Spy(UserReputation) {
                getTotalScore() >> totalScore
                isConductNegative() >> conductNegative
            }
        when:
            ReputationLevel reputationLevel = userReputation.getLevel()
        then:
            assert reputationLevel == expectedReputationLevel
        where:
            conductNegative | totalScore || expectedReputationLevel
            true            | 100        || ReputationLevel.CONDUCT_NEGATIVE
            false           | 96         || ReputationLevel.HIGH
            false           | 81         || ReputationLevel.MEDIUM
            false           | 49         || ReputationLevel.LOW

    }
}
