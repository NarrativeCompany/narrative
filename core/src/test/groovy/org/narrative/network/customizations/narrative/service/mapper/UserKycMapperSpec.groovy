package org.narrative.network.customizations.narrative.service.mapper

import org.narrative.network.core.user.UserKyc
import org.narrative.network.core.user.UserKycEvent
import org.narrative.network.core.user.UserKycEventType
import org.narrative.network.customizations.narrative.service.api.model.UserKycDTO
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus
import spock.lang.Shared
import spock.lang.Specification

class UserKycMapperSpec extends Specification {
    @Shared
    UserKycMapper mapper = new UserKycMapperImpl()

    def "test mapUserKycEntityToUserKycDTO happy path"() {
        given:
            def eventList = Arrays.asList(
                    UserKycEvent.builder().type(UserKycEventType.APPROVED).build(),
                    UserKycEvent.builder().type(UserKycEventType.REJECTED).build(),
                    UserKycEvent.builder().type(eventType as UserKycEventType).build(),
                    UserKycEvent.builder().type(UserKycEventType.NOTE).build(),
                    UserKycEvent.builder().type(UserKycEventType.SUBMITTED).build(),
            )
            def ukyc = Spy(UserKyc) {
                getInvoiceType() >> null
            }
            ukyc.setKycStatus(UserKycStatus.REJECTED)
            ukyc.setEvents(eventList)
        when:
            UserKycDTO res1 = mapper.mapUserKycEntityToUserKycDTO(ukyc)
            Collections.shuffle(eventList)
            UserKycDTO res2 = mapper.mapUserKycEntityToUserKycDTO(ukyc)
            Collections.shuffle(eventList)
            UserKycDTO res3 = mapper.mapUserKycEntityToUserKycDTO(ukyc)
        then:
            res1.rejectedReasonEventType == eventType
            res2.rejectedReasonEventType == eventType
            res3.rejectedReasonEventType == eventType
        where:
            eventType << [UserKycEventType.DOCUMENT_SUSPICIOUS, UserKycEventType.SELFIE_LOW_QUALITY, UserKycEventType.SELFIE_MISMATCH]
    }

    def "test mapUserKycEntityToUserKycDTO rejected not found"() {
        given:
            def eventList = Arrays.asList(
                    UserKycEvent.builder().type(UserKycEventType.APPROVED).build(),
                    UserKycEvent.builder().type(UserKycEventType.REJECTED).build(),
                    UserKycEvent.builder().type(UserKycEventType.NOTE).build(),
                    UserKycEvent.builder().type(UserKycEventType.NOTE).build(),
                    UserKycEvent.builder().type(UserKycEventType.SUBMITTED).build(),
            )
            def ukyc = Spy(UserKyc) {
                getInvoiceType() >> null
            }
            ukyc.setKycStatus(UserKycStatus.REJECTED)
            ukyc.setEvents(eventList)
        when:
            UserKycDTO res = mapper.mapUserKycEntityToUserKycDTO(ukyc)
        then:
            res.rejectedReasonEventType == null
    }

    def "test mapUserKycEntityToUserKycDTO not rejected"() {
        given:
            def eventList = Arrays.asList(
                    UserKycEvent.builder().type(UserKycEventType.APPROVED).build(),
                    UserKycEvent.builder().type(UserKycEventType.REJECTED).build(),
                    UserKycEvent.builder().type(eventType as UserKycEventType).build(),
                    UserKycEvent.builder().type(UserKycEventType.NOTE).build(),
            )
            def ukyc = new UserKyc();
            ukyc.setKycStatus(UserKycStatus.IN_REVIEW)
            ukyc.setEvents(eventList)
        when:
            UserKycDTO res = mapper.mapUserKycEntityToUserKycDTO(ukyc)
        then:
            res.rejectedReasonEventType == null
        where:
            eventType << [
                    UserKycEventType.USER_INFO_MISSING_FROM_DOCUMENT,
                    UserKycEventType.SELFIE_NOT_VALID ]
    }
}
