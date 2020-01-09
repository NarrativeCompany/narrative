package org.narrative.network.customizations.narrative.interceptors;

import org.narrative.network.core.security.jwt.JwtTokenInvalidException;
import org.narrative.network.core.security.jwt.UserMustAgreeToTosException;
import org.narrative.network.core.security.jwt.UserMustVerifyEmailException;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.security.PrimaryRole;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Interceptor to enforce users whose accounts are disabled do not have access
 * Date: 9/29/18
 * Time: 2:57 PM
 *
 * @author brian
 */
public class UserStatusInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod)handler;

            PrimaryRole primaryRole = networkContext().getPrimaryRole();
            if(primaryRole.isRegisteredUser()) {
                User user = primaryRole.getUser();
                checkUserDisabled(user, handlerMethod);
                checkUserEmailVerification(user, handlerMethod);
            }
        }

        return true;
    }

    private void checkUserDisabled(User user, HandlerMethod handlerMethod) {
        BypassUserDisabledCheck bypassUserDisabledCheckAnn = handlerMethod.getMethodAnnotation(BypassUserDisabledCheck.class);
        // bl: bypass the user disabled check if the handler method is annotated with @BypassUserDisabledCheck
        if(bypassUserDisabledCheckAnn!=null) {
            return;
        }


        // bl: have to make sure that the user is an active user, otherwise the JWT is invalid!
        if(!user.isActive()) {
            if(user.isDeactivated()) {
                throw new UserMustAgreeToTosException();
            }
            throw new JwtTokenInvalidException("User isn't active, so JWT invalid! This probably should never happen. status/" + user.getUserStatus() + " oid/" + user.getOid());
        }
    }

    private void checkUserEmailVerification(User user, HandlerMethod handlerMethod) {
        BypassUserEmailVerificationCheck bypassUserEmailVerificationCheckAnn = handlerMethod.getMethodAnnotation(BypassUserEmailVerificationCheck.class);
        // bl: bypass the user email verification check if the handler method is annotated with @BypassUserEmailVerificationCheck
        if(bypassUserEmailVerificationCheckAnn!=null) {
            return;
        }

        // bl: check if the user is pending email verification
        if(user.isPendingEmailVerification()) {
            throw new UserMustVerifyEmailException();
        }
    }
}
