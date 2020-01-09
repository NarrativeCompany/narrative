package org.narrative.network.core.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.narrative.base.LogSuppressor
import org.narrative.config.properties.NarrativeProperties
import org.narrative.network.core.security.jwt.JwtUtil
import org.narrative.network.core.settings.global.services.translations.NetworkResourceBundle
import org.narrative.network.customizations.narrative.controller.postbody.user.TwoFactoryVerifyInputDTO
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor
import org.narrative.network.customizations.narrative.service.api.TwoFactorAuthenticationService
import org.narrative.network.customizations.narrative.service.api.ValidationException
import org.narrative.network.customizations.narrative.service.api.model.TokenDTO
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper
import org.narrative.network.customizations.narrative.service.impl.common.ValidationExceptionFactory
import org.narrative.network.shared.context.NetworkContextImplBase
import org.narrative.network.shared.security.AuthZoneLoginRequired
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.web.servlet.HandlerExceptionResolver
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest

class TwoFactorCodeAuthenticationFilterSpec extends Specification {
    static def TOKEN_SECRET = 'superSecret'
    static def TOKEN_STRING = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJTb21lU3ViamVjdCIsImp0aSI6IlNvbWVJZCIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNjE2MjM5MDIyLCJ0ZmFFeHBpcnkiOjE1MTYyNTkwMjIsImF1dGhvcml0aWVzIjoiVVNFUl9ST0xFIn0.R_Hsw9cA4n4l7QZUPzR4z2JYT1ahMK0BnBce88s6DGc'
    @Shared narrativeProperties = new NarrativeProperties()
    @Shared logSuppressor = new LogSuppressor();
    def expectedErrorMsg = 'Some expected error message'
    def jwtUtil = Mock(JwtUtil)
    def areaTaskExec = Mock(AreaTaskExecutor)
    def messageSource = Mock(MessageSourceAccessor)
    def resolver = Mock(HandlerExceptionResolver)
    def tfaService = Mock(TwoFactorAuthenticationService)
    def request = Mock(HttpServletRequest)
    def objectMapper = Mock(ObjectMapper)
    def staticMethodWrapper = Mock(StaticMethodWrapper)
    def validationExceptionFactory = Mock(ValidationExceptionFactory)

    TwoFactorCodeAuthenticationFilter spied = Spy(TwoFactorCodeAuthenticationFilter, constructorArgs: [jwtUtil, narrativeProperties, resolver, validationExceptionFactory, tfaService, objectMapper, staticMethodWrapper]) as TwoFactorCodeAuthenticationFilter

    def setupSpec() {
        narrativeProperties.getSecurity().setLoginURI("bogusURI")
        logSuppressor.suppressLogs(TwoFactorCodeAuthenticationFilter)
    }

    def setup() {
        //Make the resolver just re-throw
        resolver.resolveException(_, _, _, _) >> { args ->
            throw args[3]
        }
    }

    def cleanupSpec() {
        logSuppressor.resumeLogs(TwoFactorCodeAuthenticationFilter)
    }

    def "test doFilterInternal no code provided"() {
        given:
            TwoFactoryVerifyInputDTO verifyDTO = TwoFactoryVerifyInputDTO.builder().verificationCode("").build()
        when:
            spied.doFilterInternal(request, null, null)
        then:
            1 * jwtUtil.extractJwtStringFromRequest(request) >> 'someJWT'
            1 * spied.parse2FAInput(request) >> verifyDTO
            1 * validationExceptionFactory.forInvalidFieldError(_, _) >> {throw new ValidationException(expectedErrorMsg, null)}
            ValidationException ex = thrown()
            ex.message == expectedErrorMsg
    }

    @Ignore
    def "test doFilterInternal no jwt in header"() {
        given:
            def networkContextImplBase = Mock(NetworkContextImplBase)
            NetworkContextImplBase.setCurrentContext(networkContextImplBase)
            def networkResourceBundle = Mock(NetworkResourceBundle)
        when:
            spied.doFilterInternal(request, null, null)
        then:
            1 * jwtUtil.extractJwtStringFromRequest(request) >> ''
            1 * networkContextImplBase.getResourceBundle() >> networkResourceBundle
            1 * networkResourceBundle.getString(_) >> 'some string'
            AuthZoneLoginRequired ex = thrown()
    }

    def "test doFilterInternal success"() {
        given:
            def jwt = 'jwt'
            def code = 123
            def rememberMe = true
            TokenDTO tokenDTO = TokenDTO.builder().build()
            TwoFactoryVerifyInputDTO verifyDTO = TwoFactoryVerifyInputDTO.builder().verificationCode(Integer.toString(code)).rememberMe(rememberMe).build()
        when:
            spied.doFilterInternal(request, null, null)
        then:
            1 * jwtUtil.extractJwtStringFromRequest(request) >> jwt
            1 * spied.parse2FAInput(request) >> verifyDTO
            1 * tfaService.renewTwoFactorAuthForCurrentJWT(jwt, code, rememberMe) >> tokenDTO
            1 * jwtUtil.writeTokenDTOAsJSONToResponse(null, tokenDTO) >> {}
    }

    @Unroll
    def "test isValidInteger value:#value expected:#expected"() {
        when:
            def res = spied.isValidInteger(value)
        then:
            res == expected
        where:
            value  | expected
            '1234' | true
            'xxx'  | false
            '12xx' | false
            ''     | false
            null   | false
    }
}
