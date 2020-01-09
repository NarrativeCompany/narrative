package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.UnexpectedError;
import org.narrative.config.StaticConfig;
import org.narrative.network.core.security.jwt.JwtUtil;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.model.TokenDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateProfileAccountConfirmationInputBase;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Date: 9/28/18
 * Time: 10:28 AM
 *
 * @author brian
 */
public class UpdateProfileAndJwtBaseTask extends UpdateProfileAccountConfirmationBaseTask<TokenDTO> {
    public UpdateProfileAndJwtBaseTask(User user, UpdateProfileAccountConfirmationInputBase updateProfilePasswordConfirmation) {
        super(user, updateProfilePasswordConfirmation);
    }

    @Override
    protected TokenDTO doMonitoredTask() {
        return generateTokenDTO(getUser());
    }

    public static TokenDTO generateTokenDTO(User user) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        JwtUtil jwtUtil = StaticConfig.getBean(JwtUtil.class);

        // load the existing JWT passed in via header. guaranteed to exist by virtue of the fact that we know
        // the current PrimaryRole is a signed in user.
        Jws<Claims> jws;
        try {
            jws = jwtUtil.parseAndValidateJWTFromString(jwtUtil.extractJwtStringFromRequest(request));
        } catch (ServletException e) {
            throw UnexpectedError.getRuntimeException("Should never get an exception parsing JWT now. Should have failed previously in a filter.", e);
        }

        // generate a new subject based on the email address and password, one of which should have been updated.
        String jwtSubject = jwtUtil.getJwtSubject(user.getEmailAddress(), user.getInternalCredentials().getPasswordFields().getHashedPassword());

        // include any non-standard JWT claims
        Map<String,Object> claimMap = jws.getBody().entrySet().stream()
                .filter(entry -> !JwtUtil.STANDARD_JWT_CLAIMS.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        //Build a clone of the token passed in, but change the subject since the email address or password has changed.
        String jwt = jwtUtil.buildJWTToken(
                jws.getBody().getId(),
                jwtSubject,
                //Issued at date
                new Date(System.currentTimeMillis()),
                jws.getBody().getExpiration(),
                //Additional claims
                claimMap
        );

        return jwtUtil.buildTokenDTO(jwt, false);
    }
}
