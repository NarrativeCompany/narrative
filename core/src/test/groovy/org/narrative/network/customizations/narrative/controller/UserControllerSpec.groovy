package org.narrative.network.customizations.narrative.controller

import org.narrative.base.WebMvcBaseSpec
import org.narrative.config.properties.NarrativeProperties
import org.narrative.network.core.security.jwt.JwtUtil
import org.narrative.network.core.user.TwoFactorAuthenticationBackupCode
import org.narrative.network.core.user.User
import org.narrative.network.customizations.narrative.controller.advice.ExceptionHandlingControllerAdvice
import org.narrative.network.customizations.narrative.controller.postbody.user.DisableTwoFactorAuthInputDTO
import org.narrative.network.customizations.narrative.controller.postbody.user.EnableTwoFactorAuthInputDTO
import org.narrative.network.customizations.narrative.controller.postbody.user.UserNotificationSettingsInputDTO
import org.narrative.network.customizations.narrative.service.api.KycService
import org.narrative.network.customizations.narrative.service.api.RedemptionService
import org.narrative.network.customizations.narrative.service.api.TwoFactorAuthenticationService
import org.narrative.network.customizations.narrative.service.api.UserService
import org.narrative.network.customizations.narrative.service.api.model.*
import org.narrative.network.customizations.narrative.service.api.model.input.KycApplicantInput
import org.narrative.network.customizations.narrative.service.api.model.kyc.KycIdentificationType
import org.narrative.network.customizations.narrative.service.mapper.CurrentUserMapper
import org.narrative.network.customizations.narrative.service.mapper.UserMapper
import org.narrative.network.shared.context.NetworkContextImplBase
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.core.convert.converter.Converter
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import spock.lang.Shared
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

import javax.servlet.http.Cookie
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicLong

class UserControllerSpec extends WebMvcBaseSpec {
    @Shared UserService userService
    @Shared RedemptionService redemptionsService
    @Shared UserMapper userMapper
    @Shared TwoFactorAuthenticationService tfas
    @Shared JwtUtil jwtUtil
    @Shared CurrentUserMapper currentUserMapper
    @Shared AcceptHeaderLocaleResolver acceptHeaderLocaleResolver
    @Shared KycService kycService
    UserController tested;
    static AtomicLong oidGen = new AtomicLong(0);

    @Shared NarrativeProperties narrativeProperties = new NarrativeProperties()

    @Override
    def buildController(DetachedMockFactory detachedMockFactory) {
        userService = detachedMockFactory.Mock(UserService)
        redemptionsService = detachedMockFactory.Mock(RedemptionService)
        userMapper = detachedMockFactory.Mock(UserMapper)
        tfas = detachedMockFactory.Mock(TwoFactorAuthenticationService)
        jwtUtil = detachedMockFactory.Mock(JwtUtil)
        currentUserMapper = detachedMockFactory.Mock(CurrentUserMapper)
        acceptHeaderLocaleResolver = detachedMockFactory.Mock(AcceptHeaderLocaleResolver)
        kycService = detachedMockFactory.Mock(KycService)
        tested = new UserController(userService, tfas, redemptionsService, userMapper, currentUserMapper,acceptHeaderLocaleResolver, narrativeProperties, kycService, staticMethodWrapper, validationExceptionFactory) {
            @Override
            def File getUploadedFile(MultipartFile multipartFile) {
                return createFile((MockMultipartFile) multipartFile)
            }
        }
        tested
    }

    @Override
    def getMockList() {
        return [userService, redemptionsService, jwtUtil, userMapper, tfas, currentUserMapper, kycService]
    }

    @Override
    def getConverters() {
        // Dumb fake converter to allow multipart upload to work with MockMVC
        [
        new Converter<MockMultipartFile, KycApplicantInput>() {
            @Override
            KycApplicantInput convert(MockMultipartFile source) {
                return mappingJackson2JsonView.getObjectMapper().readValue(source.bytes, KycApplicantInput)
            }
        }
        ]
    }

    def "Test findUserNotificationSettings"() {
        given:
            def uriString = '/users/current/notification-settings'
            def expected = UserNotificationSettingsDTO.builder()
                    .notifyWhenFollowed(false)
                    .notifyWhenMentioned(true)
                    .suspendAllEmails(true)
                    .build()
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            UserNotificationSettingsDTO res = convertResultToObject(mvcResult, UserNotificationSettingsDTO)
        then:
            1 * userService.findUserNotificationSettings() >> expected
            mvcResult != null
            res == expected
    }

    def "test updateUserNotificationSettings"() {
        given:
            def uriString = '/users/current/notification-settings'
            def arg = UserNotificationSettingsInputDTO.builder()
                    .notifyWhenFollowed(false)
                    .notifyWhenMentioned(true)
                    .suspendAllEmails(true)
                    .build()
            def expected = UserNotificationSettingsDTO.builder()
                    .suspendAllEmails(arg.suspendAllEmails)
                    .notifyWhenMentioned(arg.notifyWhenMentioned)
                    .notifyWhenFollowed(arg.notifyWhenFollowed)
                    .build()
        when:
            def mvcResult = mockMvc.perform(buildPutRequest(uriString, objectMapper.writeValueAsString(arg)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            UserNotificationSettingsDTO res = convertResultToObject(mvcResult, UserNotificationSettingsDTO)
        then:
            1 * userMapper.mapUserNotificationsSettingsReqToDTO(arg) >> expected
            1 * userService.updateUserNotificationSettings(expected) >> expected
            mvcResult != null
            res == expected
    }

    @Unroll
    def "test updateUserNotificationSettings bad data field: #reason"() {
        given:
            def uriString = '/users/current/notification-settings'
            logSuppressor.suppressLogs(ExceptionHandlingControllerAdvice)
        when:
            def mvcResult = mockMvc.perform(buildPutRequest(uriString, postJson as String))
                    //.andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn()
            ValidationErrorDTO err = convertResultToObject(mvcResult, ValidationErrorDTO)
        then:
            1 * exceptionHandlingControllerAdvice.setCurrentPartitionGroupInError() >> {}
            mvcResult != null
            err.fieldErrors.size() == 1
            extractKeyValues(err).contains(reason)
        cleanup:
            logSuppressor.resumeLogs(ExceptionHandlingControllerAdvice)
        where:
            [postJson, reason] << buildBadRequestData()
    }

    def buildBadRequestData() {
        [
                ['{"notifyWhenFollowed": false,  "notifyWhenMentioned": true}', 'suspendAllEmails'],
                ['{"notifyWhenFollowed": false,  "suspendAllEmails": true}', 'notifyWhenMentioned'],
                ['{"notifyWhenMentioned": true, "suspendAllEmails": true}', 'notifyWhenFollowed']
        ]
    }

    def "Test getUserOwnedChannels"() {
        given:
            def uriString = '/users/current/owned-channels'
            def expected = UserOwnedChannelsDTO.builder()
                    .ownedNiches(2)
                    .ownedPublications(1)
                    .build()
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            UserOwnedChannelsDTO res = convertResultToObject(mvcResult, UserOwnedChannelsDTO)
        then:
            1 * userService.getUserOwnedChannels() >> expected
            mvcResult != null
            res == expected
    }

    @Unroll
    def "test enableTwoFactorAuthenticationForCurrentUser bad data secret:#secret verificationCode:#verificationCode"() {
        given:
            def uriString = '/users/current/2fa-secret'
            def jwtString = 'someJWT'
            logSuppressor.suppressLogs(ExceptionHandlingControllerAdvice)
            def postBody = EnableTwoFactorAuthInputDTO.builder()
                    .twoFactorAuthCode(verificationCode)
                    .secret(secret)
                    .rememberMe(false)
                    .currentPassword('superSecret')
                    .build()
        when:
            def mvcResult = mockMvc.perform(
                                        buildPostRequest(uriString, mappingJackson2JsonView.objectMapper.writeValueAsString(postBody))
                                        .cookie(new Cookie(JwtUtil.AUTH_COOKIE_KEY, jwtString))
                    )
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            ValidationErrorDTO err = convertResultToObject(mvcResult, ValidationErrorDTO)
        then:
            //Stub out this static method call wrapper for testing
            1 * exceptionHandlingControllerAdvice.setCurrentPartitionGroupInError() >> {}
            mvcResult != null
            err.fieldErrors.size() == 1
            extractKeyValues(err).contains(reason)
        cleanup:
            logSuppressor.resumeLogs(ExceptionHandlingControllerAdvice)
        where:
            secret   | verificationCode | reason
            null     | 1234             | 'secret'
            'secret' | null             | 'twoFactorAuthCode'
    }

    def "Test enableTwoFactorAuthenticationForCurrentUser"() {
        given:
            def uriString = '/users/current/2fa-secret'
            def secret = "someReallySecretString"
            def verificationCode = 12348765
            def postBody = EnableTwoFactorAuthInputDTO.builder()
                            .twoFactorAuthCode(verificationCode)
                            .secret(secret)
                            .rememberMe(false)
                            .currentPassword('superSecret')
                            .build();
            def rememberMe = true
            def jwtString = 'someJWT'
            def expected = TokenDTO.builder().token(jwtString).twoFactorAuthExpired(false).build()
        when:
            def mvcResult = mockMvc.perform(
                    buildPostRequest(uriString, mappingJackson2JsonView.objectMapper.writeValueAsString(postBody))
                            .cookie(new Cookie(JwtUtil.AUTH_COOKIE_KEY, jwtString))
            )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
            TokenDTO res = convertResultToObject(mvcResult, TokenDTO)
        then:
            1 * tfas.enableTwoFactorAuthForCurrentUser(jwtString, _) >> expected
            mvcResult != null
            res == expected
    }

    def "Test disableTwoFactorAuthenticationForCurrentUser"() {
        given:
            def uriString = '/users/current/2fa-secret'
            def body = DisableTwoFactorAuthInputDTO.builder()
                        .currentPassword('superSecret')
                        .twoFactorAuthCode(123456)
                        .build()
        when:
            def mvcResult = mockMvc.perform(buildPutRequest(uriString, mappingJackson2JsonView.objectMapper.writeValueAsString(body)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
        then:
            1 * tfas.disableTwoFactorAuthForCurrentUser(_)
            mvcResult != null
            StringUtils.isEmpty(mvcResult.response.contentAsString)
    }

    def "Test generateTwoFactorSecretForCurrentUser"() {
        given:
            def uriString = '/users/current/2fa-secret'
            def expectedSecret = 'superSecret'
            def expectedBackupCodes = TwoFactorAuthenticationBackupCode.getAllBackupCodes(expectedSecret)
            byte[] expectedImageBytes = 'thisissomeimage'.getBytes(StandardCharsets.UTF_8)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            TwoFactorSecretDTO res = convertResultToObject(mvcResult, TwoFactorSecretDTO)
        then:
            1 * tfas.generateNewSecret() >> expectedSecret
            1 * tfas.buildQRImageForCurrentUser(expectedSecret, UserController.QR_IMAGE_TYPE) >> expectedImageBytes
            1 * tfas.generateBackupCodes(expectedSecret) >> expectedBackupCodes
            mvcResult != null
            res.secret == expectedSecret
            res.qrCodeImage == 'data:image/' + UserController.QR_IMAGE_TYPE.name().toLowerCase() + ';base64,' + Base64.encodeBase64String(expectedImageBytes)
            res.backupCodes.containsAll(Arrays.asList(472947, 417850, 678612, 961737, 177339, 972480, 680298, 147137, 694119, 739528))
    }

    def "Test getKycStateForCurrentUser"() {
        given:
            def uriString = '/users/current/kyc/status'
            def expected = UserKycDTO.builder().build()
            def networkContext = Mock(NetworkContextImplBase)
            def user = Mock(User)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            UserKycDTO res = convertResultToObject(mvcResult, UserKycDTO)
        then:
            2 * staticMethodWrapper.networkContext() >> networkContext
            1 * networkContext.getPrimaryRole() >> user
            1 * user.checkRegisteredUser() >> null
            1 * networkContext.getUser() >> user
            1 * kycService.getKycStateForUser(_) >> expected
            mvcResult != null
            res == expected
    }

    def createFile(MockMultipartFile mpFile) {
        File f = File.createTempFile(mpFile.originalFilename, 'tmp')
        FileUtils.writeByteArrayToFile(f, mpFile.bytes)
        return f
    }

    def "Test submitKYCApplicant PASSPORT"() {
        given:
            def uriString = '/users/current/kyc/verification'
            def input = new KycApplicantInput(KycIdentificationType.PASSPORT)
            def jsonPart = new MockMultipartFile('kycApplicantInput', '', MediaType.APPLICATION_JSON_UTF8_VALUE, mappingJackson2JsonView.getObjectMapper().writeValueAsString(input).getBytes('UTF-8'))
            def firstFile = new MockMultipartFile(KycService.LIVE_PHOTO_IMAGE, 'filename.jpg', MediaType.IMAGE_JPEG_VALUE,'some image'.getBytes());
            def secondFile = new MockMultipartFile(KycService.DOC_FRONT_IMAGE, 'other-file-name.jpg', MediaType.IMAGE_JPEG_VALUE, 'some other image'.getBytes());
            def expected = UserKycDTO.builder().build()
            def request = MockMvcRequestBuilders.multipart(uriString).file(jsonPart).file(firstFile).file(secondFile)
            def networkContext = Mock(NetworkContextImplBase)
            def user = Mock(User)
        when:
            def mvcResult = mockMvc.perform(request)
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            UserKycDTO res = convertResultToObject(mvcResult, UserKycDTO)
        then:
            1 * staticMethodWrapper.networkContext() >> networkContext
            1 * networkContext.getUser() >> user
            1 * kycService.submitKycApplicant(_ as User, input.kycIdentificationType, _ as File, _ as File, null) >> { args ->
                File file1 = (File) args[2]
                assert FileUtils.readFileToByteArray(file1) == firstFile.bytes
                assert file1.name.contains(firstFile.originalFilename)
                File file2 = args[3] as File
                assert FileUtils.readFileToByteArray(file2) == secondFile.bytes
                assert file2.name.contains(secondFile.originalFilename)
                expected
            }
            mvcResult != null
            res == expected
    }

    def "Test submitKYCApplicant DRIVERS_LICENSE"() {
        given:
            def uriString = '/users/current/kyc/verification'
            def input = new KycApplicantInput(KycIdentificationType.DRIVERS_LICENSE)
            def jsonPart = new MockMultipartFile('kycApplicantInput', '', MediaType.APPLICATION_JSON_UTF8_VALUE, mappingJackson2JsonView.getObjectMapper().writeValueAsString(input).getBytes('UTF-8'))
            def firstFile = new MockMultipartFile(KycService.LIVE_PHOTO_IMAGE, 'filename.jpg', MediaType.IMAGE_JPEG_VALUE,'some image'.getBytes());
            def secondFile = new MockMultipartFile(KycService.DOC_FRONT_IMAGE, 'other-file-name.jpg', MediaType.IMAGE_JPEG_VALUE, 'some other image'.getBytes());
            def thirdFile = new MockMultipartFile(KycService.DOC_BACK_IMAGE, 'other-file-name2.jpg', MediaType.IMAGE_JPEG_VALUE, 'some other image2'.getBytes());
            def expected = UserKycDTO.builder().build()
            def request = MockMvcRequestBuilders.multipart(uriString).file(jsonPart).file(firstFile).file(secondFile).file(thirdFile)
            def networkContext = Mock(NetworkContextImplBase)
        when:
            def mvcResult = mockMvc.perform(request)
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            UserKycDTO res = convertResultToObject(mvcResult, UserKycDTO)
        then:
            1 * staticMethodWrapper.networkContext() >> networkContext
            1 * networkContext.getUser() >> Mock(User)
            1 * kycService.submitKycApplicant(_, input.kycIdentificationType, _ as File, _ as File, _ as File) >> { args ->
                File file1 = (File) args[2]
                assert FileUtils.readFileToByteArray(file1) == firstFile.bytes
                assert file1.name.contains(firstFile.originalFilename)
                File file2 = args[3] as File
                assert FileUtils.readFileToByteArray(file2) == secondFile.bytes
                assert file2.name.contains(secondFile.originalFilename)
                File file3 = args[4] as File
                assert FileUtils.readFileToByteArray(file3) == thirdFile.bytes
                assert file3.name.contains(thirdFile.originalFilename)
                expected
            }
            mvcResult != null
            res == expected
    }

    def "Test submitKYCApplicant GOVERNMENT_ID"() {
        given:
            def uriString = '/users/current/kyc/verification'
            def input = new KycApplicantInput(KycIdentificationType.GOVERNMENT_ID)
            def jsonPart = new MockMultipartFile('kycApplicantInput', '', MediaType.APPLICATION_JSON_UTF8_VALUE, mappingJackson2JsonView.getObjectMapper().writeValueAsString(input).getBytes('UTF-8'))
            def firstFile = new MockMultipartFile(KycService.LIVE_PHOTO_IMAGE, 'filename.jpg', MediaType.IMAGE_JPEG_VALUE,'some image'.getBytes());
            def secondFile = new MockMultipartFile(KycService.DOC_FRONT_IMAGE, 'other-file-name.jpg', MediaType.IMAGE_JPEG_VALUE, 'some other image'.getBytes());
            def thirdFile = new MockMultipartFile(KycService.DOC_BACK_IMAGE, 'other-file-name2.jpg', MediaType.IMAGE_JPEG_VALUE, 'some other image2'.getBytes());
            def expected = UserKycDTO.builder().build()
            def request = MockMvcRequestBuilders.multipart(uriString).file(jsonPart).file(firstFile).file(secondFile).file(thirdFile)
            def networkContext = Mock(NetworkContextImplBase)
        when:
            def mvcResult = mockMvc.perform(request)
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            UserKycDTO res = convertResultToObject(mvcResult, UserKycDTO)
        then:
            1 * staticMethodWrapper.networkContext() >> networkContext
            1 * networkContext.getUser() >> Mock(User)
            1 * kycService.submitKycApplicant(_, input.kycIdentificationType, _ as File, _ as File, _ as File) >> { args ->
                File file1 = (File) args[2]
                assert FileUtils.readFileToByteArray(file1) == firstFile.bytes
                assert file1.name.contains(firstFile.originalFilename)
                File file2 = args[3] as File
                assert FileUtils.readFileToByteArray(file2) == secondFile.bytes
                assert file2.name.contains(secondFile.originalFilename)
                File file3 = args[4] as File
                assert FileUtils.readFileToByteArray(file3) == thirdFile.bytes
                assert file3.name.contains(thirdFile.originalFilename)
                expected
            }
            mvcResult != null
            res == expected
    }

    def "Test submitKYCApplicant PASSPORT missing live photo"() {
        given:
            def uriString = '/users/current/kyc/verification'
            def input = new KycApplicantInput(KycIdentificationType.PASSPORT)
            def jsonPart = new MockMultipartFile('kycApplicantInput', '', MediaType.APPLICATION_JSON_UTF8_VALUE, mappingJackson2JsonView.getObjectMapper().writeValueAsString(input).getBytes('UTF-8'))
            def secondFile = new MockMultipartFile(KycService.DOC_FRONT_IMAGE, '', MediaType.IMAGE_JPEG_VALUE, 'some other image'.getBytes());
            def request = MockMvcRequestBuilders.multipart(uriString).file(jsonPart).file(secondFile)
        when:
            def mvcResult = mockMvc.perform(request)
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            ValidationErrorDTO res = convertResultToObject(mvcResult, ValidationErrorDTO)
        then:
            1 * exceptionHandlingControllerAdvice.setCurrentPartitionGroupInError() >> {}
            mvcResult != null
            res.fieldErrors.get(0).name == KycService.LIVE_PHOTO_IMAGE
    }

    def "Test submitKYCApplicant PASSPORT missing front image"() {
        given:
            def uriString = '/users/current/kyc/verification'
            def input = new KycApplicantInput(KycIdentificationType.PASSPORT)
            def jsonPart = new MockMultipartFile('kycApplicantInput', '', MediaType.APPLICATION_JSON_UTF8_VALUE, mappingJackson2JsonView.getObjectMapper().writeValueAsString(input).getBytes('UTF-8'))
            def firstFile = new MockMultipartFile(KycService.LIVE_PHOTO_IMAGE, '', MediaType.IMAGE_JPEG_VALUE,'some image'.getBytes());
            def request = MockMvcRequestBuilders.multipart(uriString).file(jsonPart).file(firstFile)
        when:
            def mvcResult = mockMvc.perform(request)
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            ValidationErrorDTO res = convertResultToObject(mvcResult, ValidationErrorDTO)
        then:
            1 * exceptionHandlingControllerAdvice.setCurrentPartitionGroupInError() >> {}
            mvcResult != null
            res.fieldErrors.get(0).name == KycService.DOC_FRONT_IMAGE
    }

    def "Test submitKYCApplicant DRIVERS_LICENSE missing back image"() {
        given:
            def uriString = '/users/current/kyc/verification'
            def input = new KycApplicantInput(KycIdentificationType.DRIVERS_LICENSE)
            def jsonPart = new MockMultipartFile('kycApplicantInput', '', MediaType.APPLICATION_JSON_UTF8_VALUE, mappingJackson2JsonView.getObjectMapper().writeValueAsString(input).getBytes('UTF-8'))
            def firstFile = new MockMultipartFile(KycService.LIVE_PHOTO_IMAGE, '', MediaType.IMAGE_JPEG_VALUE,'some image'.getBytes());
            def secondFile = new MockMultipartFile(KycService.DOC_FRONT_IMAGE, '', MediaType.IMAGE_JPEG_VALUE, 'some other image'.getBytes());
            def request = MockMvcRequestBuilders.multipart(uriString).file(jsonPart).file(firstFile).file(secondFile)
        when:
            def mvcResult = mockMvc.perform(request)
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            ValidationErrorDTO res = convertResultToObject(mvcResult, ValidationErrorDTO)
        then:
            1 * exceptionHandlingControllerAdvice.setCurrentPartitionGroupInError() >> {}
            mvcResult != null
            res.fieldErrors.get(0).name == KycService.DOC_BACK_IMAGE
    }
}
