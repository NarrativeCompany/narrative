package org.narrative.network.core.user

import org.narrative.common.util.UnexpectedError
import org.narrative.network.core.rating.AgeRating
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate

class UserKycSpec extends Specification {
    @Shared
    now = LocalDate.of(2019, 02, 1)

    def "test calculateAgeInYears not approved"() {
        given:
            UserKyc userKyc = new UserKyc()
            userKyc.setKycStatus(UserKycStatus.IN_REVIEW)
        when:
            def res = userKyc.getAgeInYears()
        then:
            res == null
    }

    def "test calculateAgeInYears non initialized dates"() {
        given:
            UserKyc userKyc = new UserKyc()
            userKyc.setKycStatus(UserKycStatus.APPROVED)
        when:
            userKyc.getAgeInYears()
        then:
            thrown(UnexpectedError)
    }

    def "test calculateAgeInYears"() {
        when:
            Long res = userKyc.getAgeInYears()
        then:
            res == expected
        where:
            userKyc                                  || expected
            buildUserKyc(2001, 01, now) || 18
            buildUserKyc(2001, 02, now) || 17
            buildUserKyc(2001, 03, now) || 17
            buildUserKyc(2200, 03, now) || null
    }

    def "test getPermittedAgeRatings"() {
        when:
            def res = userKyc.getPermittedAgeRatings()
        then:
            res == expected
        where:
            userKyc                                  || expected
            buildUserKyc(2001, 01, now) || EnumSet.of(AgeRating.RESTRICTED, AgeRating.GENERAL)
            buildUserKyc(2001, 02, now) || EnumSet.of(AgeRating.GENERAL)
            buildUserKyc(2001, 03, now) || EnumSet.of(AgeRating.GENERAL)
            buildUserKyc(2200, 03, now) || EnumSet.of(AgeRating.GENERAL)
    }

    def buildUserKyc(int year, int month, LocalDate now) {
        def kyc = new UserKyc(){
            @Override
            def LocalDate getNow() {
                now
            }
        }
        kyc.setKycStatus(UserKycStatus.APPROVED)
        kyc.setBirthYear(year)
        kyc.setBirthMonth(month)
        kyc
    }
}
