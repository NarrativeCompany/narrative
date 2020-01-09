package org.narrative.network.customizations.narrative.service.impl.kyc

import org.narrative.base.BaseI18NAndValidatorSpec
import org.narrative.common.persistence.OID
import org.narrative.common.util.UnexpectedError
import org.narrative.network.FakeAreaTaskExecutor
import org.narrative.network.core.user.User
import org.narrative.network.core.user.UserKyc
import org.narrative.network.core.user.UserKycEventType
import org.narrative.network.core.user.dao.UserKycDAO
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor
import org.narrative.network.customizations.narrative.service.api.model.UserKycDTO
import org.narrative.network.customizations.narrative.service.api.model.kyc.KycIdentificationType
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper
import org.narrative.network.customizations.narrative.service.impl.kyc.email.SendKycCertAttemptRejectedEmailTask
import org.narrative.network.customizations.narrative.service.impl.kyc.email.SendKycCertRevokedChargebackEmailTask
import org.narrative.network.customizations.narrative.service.impl.kyc.email.SendKycCertifiedEmailTask
import org.narrative.network.customizations.narrative.service.mapper.UserKycMapper
import org.narrative.network.customizations.narrative.service.mapper.UserKycMapperImpl
import spock.lang.Unroll

import java.time.Instant

class KycServiceImplSpec extends BaseI18NAndValidatorSpec {

    AreaTaskExecutor areaTaskExec = Spy(FakeAreaTaskExecutor)
    UserKycDAO userKycDAO = Mock(UserKycDAO)
    UserKycMapper userKycMapper = new UserKycMapperImpl()
    StaticMethodWrapper wrapper = Mock(StaticMethodWrapper)

    KycServiceImpl tested

    def setupSpec() {
        logSuppressor.suppressLogs(KycServiceImpl)
    }

    def setup() {
        tested = (KycServiceImpl) Spy(KycServiceImpl, constructorArgs: [areaTaskExec, userKycMapper, messageSourceAccessor, wrapper])
    }

    def cleanupSpec() {
        logSuppressor.resumeLogs(KycServiceImpl)
    }

    def "getKycStateForCurrentUser"() {
        given:
            def oid = new OID(-1)
            def user = new User()
            user.setLastPaymentChargebackDatetime(Instant.now())
            def userKyc = new UserKyc(user)
            userKyc.setOid(oid)
        when:
            UserKycDTO res = tested.getKycStateForUser(oid)
        then:
            1 * tested.getUserKycDAO() >> userKycDAO
            1 * userKycDAO.get(oid) >> userKyc
            res.oid == oid
    }

    def "submitKYCApplicant"() {
        given:
            def idType = KycIdentificationType.PASSPORT
            def user = new User()
            user.setUsername('JoeBlow')
            user.setOid(new OID(-1))
            def userKyc = new UserKyc(user)
            userKyc.setKycStatus(UserKycStatus.READY_FOR_VERIFICATION)
            File picFile = Mock(File)
            File idFile1 = Mock(File)
        when:
            UserKycDTO res = tested.submitKycApplicant(user, idType, picFile, idFile1, null)
        then:
            1 * wrapper.checkRegisteredUser() >> {}
            1 * tested.getUserKycDAO() >> userKycDAO
            1 * tested.getUserKycForUser(user) >> userKyc
            1 * areaTaskExec.executeAreaTask(_) >> {}
            1 * tested.sendCertificationNotificationEmail() >> {}
            res.oid == user.getOid()
            res.kycStatus == UserKycStatus.AWAITING_METADATA
            userKyc.events.size() == 1
            userKyc.events.get(0).type == UserKycEventType.SUBMITTED
    }

    def buildUserKyc() {
        UserKyc userKyc = new UserKyc()
        userKyc.setOid(new OID(-1))
        userKyc.user = new User()
        userKyc.user.oid = userKyc.oid
        userKyc.user.lastPaymentChargebackDatetime = Instant.now()
        userKyc
    }

    def "Test updateKycUserStatus bad status"() {
        given:
            UserKyc userKyc = buildUserKyc()
        when:
            tested.updateKycUserStatus(userKyc, UserKycStatus.IN_REVIEW, UserKycEventType.SELFIE_NOT_VALID, null, null)
        then:
            thrown(UnexpectedError)
    }

    def "Test updateKycUserStatus userKyc null"() {
        given:
            UserKyc userKyc = null
        when:
            tested.updateKycUserStatus(userKyc, UserKycStatus.REJECTED, UserKycEventType.SELFIE_NOT_VALID, null, null)
        then:
            thrown(UnexpectedError)
    }

    @Unroll
    def "Test updateKycUserStatus APPROVED REVOKED: #eventType"() {
        given:
            UserKyc userKyc = buildUserKyc()
            if ( UserKycStatus.REVOKED.equals(status)) {
                userKyc.setKycStatus(UserKycStatus.APPROVED)
            }
        when:
            tested.updateKycUserStatus(userKyc, status, eventType, user, note)
        then:
            1 * areaTaskExec.executeNarrativePlatformAreaTask(_)
            1 * tested.getUserKycDAO() >> userKycDAO
            1 * userKycDAO.lock(userKyc)
            1 * tested.appendKycEventAndSendEmailIfNecessary(_, _, _, _) >> {args ->
                assert args[0] == userKyc
                assert args[1] == eventTypeRes
                assert args[2] == user
                assert args[3] == note
                callRealMethod()
            }
            1 * areaTaskExec.executeGlobalTask(_) >> { args ->
                if (UserKycStatus.APPROVED.equals(status)) {
                    assert ((SaveLedgerEntryTask) args[0]).entry.type == LedgerEntryType.KYC_CERTIFICATION_APPROVED
                } else if (UserKycStatus.REVOKED.equals(status)) {
                    assert ((SaveLedgerEntryTask) args[0]).entry.type == LedgerEntryType.KYC_CERTIFICATION_REVOKED
                }
            }
            1 * areaTaskExec.executeNarrativePlatformAreaTask(_) >> {args->
                assert taskType.name == args[0].getClass().name
            }
            userKyc.kycStatus == status
        where:
            status                  | user          | note        | eventType                 || eventTypeRes               | taskType
            UserKycStatus.APPROVED  | 'someUser'    | 'someNote'  | UserKycEventType.APPROVED || UserKycEventType.APPROVED  | SendKycCertifiedEmailTask
            UserKycStatus.APPROVED  | null          | null        | UserKycEventType.APPROVED || UserKycEventType.APPROVED  | SendKycCertifiedEmailTask
            UserKycStatus.REVOKED   | 'someUser'    | 'someNote'  | UserKycEventType.REVOKED  || UserKycEventType.REVOKED   | SendKycCertRevokedChargebackEmailTask
    }

    @Unroll
    def "Test updateKycUserStatus SELFIE_NOT_VALID #eventType"() {
        given:
            UserKyc userKyc = buildUserKyc()
        when:
            def res = tested.updateKycUserStatus(userKyc, status, eventType, user, note)
        then:
            1 * areaTaskExec.executeNarrativePlatformAreaTask(_)
            1 * tested.getUserKycDAO() >> userKycDAO
            1 * userKycDAO.lock(userKyc)
            1 * tested.appendKycEventAndSendEmailIfNecessary(_, _, _, _) >> {args ->
                assert args[0] == userKyc
                assert args[1] == eventTypeRes
                assert args[2] == user
                assert args[3] == note
                callRealMethod()
            }
            0 * areaTaskExec.executeGlobalTask(_)
            1 * areaTaskExec.executeNarrativePlatformAreaTask(_) >> {args->
                assert taskType.name == args[0].getClass().name
            }
            userKyc.kycStatus == status
        where:
            status                  | user          | note        | eventType                           || eventTypeRes                      | taskType
            UserKycStatus.REJECTED  | 'someUser'    | 'someNote'  | UserKycEventType.SELFIE_NOT_VALID   || UserKycEventType.SELFIE_NOT_VALID | SendKycCertAttemptRejectedEmailTask
            UserKycStatus.REJECTED  | null          | null        | UserKycEventType.SELFIE_NOT_VALID   || UserKycEventType.SELFIE_NOT_VALID | SendKycCertAttemptRejectedEmailTask
    }

}
