package org.narrative.network.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.security.jwt.JwtAuthenticationTokenFilter;
import org.narrative.network.core.security.jwt.JwtUtil;
import org.narrative.network.customizations.narrative.controller.postbody.user.TwoFactoryVerifyInputDTO;
import org.narrative.network.customizations.narrative.service.api.TwoFactorAuthenticationService;
import org.narrative.network.customizations.narrative.service.api.model.TokenDTO;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.impl.common.ValidationExceptionFactory;
import org.narrative.network.shared.security.AuthZoneLoginRequired;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filter that will accept a JWT and 2FA code and validate the 2FA code for the principal found in the current JWT.  If
 * the code is valid, write a new JWT into the response with an updated 2FA expiry.
 *
 * ###
 * ### Renewal requests must be submitted to the URI supported by this filter so they are processed BEFORE
 * ### normal JWT processing by {@link JwtAuthenticationTokenFilter}
 * ###
 */
public class TwoFactorCodeAuthenticationFilter extends OncePerRequestFilter {
    private static final String TWO_FACTOR_PATH = "check-2fa";

    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver resolver;
    private final RequestMatcher requestMatcher;
    private final ValidationExceptionFactory validationExceptionFactory;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;
    private final ObjectMapper objectMapper;
    private final StaticMethodWrapper staticMethodWrapper;

    public TwoFactorCodeAuthenticationFilter(JwtUtil jwtUtil, NarrativeProperties narrativeProperties,
                                             HandlerExceptionResolver resolver,
                                             ValidationExceptionFactory validationExceptionFactory,
                                             TwoFactorAuthenticationService twoFactorAuthenticationService,
                                             ObjectMapper objectMapper,
                                             StaticMethodWrapper staticMethodWrapper) {
        this.jwtUtil = jwtUtil;
        this.resolver = resolver;
        this.validationExceptionFactory = validationExceptionFactory;
        this.twoFactorAuthenticationService = twoFactorAuthenticationService;
        this.objectMapper = objectMapper;
        this.staticMethodWrapper = staticMethodWrapper;

        this.requestMatcher = new AntPathRequestMatcher(narrativeProperties.getSecurity().getLoginURI() + "/" + TWO_FACTOR_PATH, HttpMethod.POST.name());
    }

    /**
     * Can be overridden in subclasses for custom filtering control,
     * returning {@code true} to avoid filtering of the given request.
     * <p>The default implementation always returns {@code false}.
     *
     * @param request current HTTP request
     * @return whether the given request should <i>not</i> be filtered
     * @throws ServletException in case of errors
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !requestMatcher.matches(request);
    }

    /**
     * Handle a JWT 2FA code validation
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Get credentials from request JSON

        TwoFactoryVerifyInputDTO twoFactorInput = parse2FAInput(request);
        TokenDTO tokenDTO;
        try {
            //Get the current JWT from the header
            String currentJWTString = jwtUtil.extractJwtStringFromRequest(request);
            if (StringUtils.isEmpty(currentJWTString)) {
                throw new AuthZoneLoginRequired();
            }

            //Make sure a code is provided and valid
            if (!isValidInteger(twoFactorInput.getVerificationCode())) {
                throw validationExceptionFactory.forInvalidFieldError("Invalid 2FA code", TwoFactoryVerifyInputDTO.Fields.verificationCode);
            }
            int verificationCode = Integer.parseInt(twoFactorInput.getVerificationCode());

            tokenDTO = twoFactorAuthenticationService.renewTwoFactorAuthForCurrentJWT(currentJWTString, verificationCode, twoFactorInput.isRememberMe());
        } catch (Exception e) {
            resolver.resolveException(request, response, null, e);
            return;
        }

        //Write the response
        jwtUtil.writeTokenDTOAsJSONToResponse(response, tokenDTO);

        //The request is over.  No need to continue chaining
    }

    @VisibleForTesting
    TwoFactoryVerifyInputDTO parse2FAInput(HttpServletRequest request) {
        if (!MediaType.APPLICATION_JSON_VALUE.equals(request.getHeader(HttpHeaders.CONTENT_TYPE))) {
            return null;
        }

        try {
            /*
             * HttpServletRequest can be read only once
             */
            String json = IOUtils.toString(request.getReader());

            //json transformation
            TwoFactoryVerifyInputDTO twoFactorInput = objectMapper.readValue(json, TwoFactoryVerifyInputDTO.class);

            // set the request body object on the RequestResponseHandler for logging purposes.
            staticMethodWrapper.networkContext().getReqResp().setRequestBodyObject(twoFactorInput);

            return twoFactorInput;
        } catch (Exception e) {
            // log & ignore the exception so we fall through and throw BadCredentialsException below.
            if(logger.isWarnEnabled()) logger.warn("Failed processing JSON 2FA input", e);
        }

        return null;
    }

    @VisibleForTesting
    boolean isValidInteger(String code) {
        if (StringUtils.isEmpty(code)) {
            return false;
        }

        try {
            Integer.parseInt(code);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
