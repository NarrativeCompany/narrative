package org.narrative.network.shared.security;

import org.narrative.network.customizations.narrative.controller.advice.ExceptionHandlingControllerAdvice;
import org.narrative.network.customizations.narrative.service.api.model.ErrorType;
import org.narrative.network.shared.services.NetworkException;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 12, 2005
 * Time: 10:36:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class AccessViolation extends NetworkException {

    private final Securable securable;

    public AccessViolation() {
        this(wordlet("error.accessViolation.noPermToPerformAction"));
    }

    public AccessViolation(String message) {
        super(message);
        securable = null;
    }

    public AccessViolation(String title, String message) {
        super(title, message);
        securable = null;
    }

    public AccessViolation(Securable securable) {
        super(wordlet("error.accessViolation.noPermToPerformAction"));
        this.securable = securable;
    }

    public Securable getSecurable() {
        return securable;
    }

    /**
     * {@link ErrorType} overrides for this method are only applied if the user is logged in. Otherwise
     * {@link ExceptionHandlingControllerAdvice} will use LOGIN_REQUIRED to force the user to sign in.
     * @return null
     */
    @Override
    public ErrorType getErrorType() {
        return super.getErrorType();
    }
}
