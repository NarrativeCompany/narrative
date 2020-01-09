package org.narrative.network.core.security.jwt;

import org.narrative.common.util.ApplicationError;
import org.narrative.network.customizations.narrative.service.api.model.ErrorType;
import org.springframework.http.HttpStatus;

/**
 * Date: 9/29/18
 * Time: 11:05 AM
 *
 * @author brian
 */
public class UserMustAgreeToTosException extends ApplicationError {
    private static final long serialVersionUID = 8908909573187592647L;

    public UserMustAgreeToTosException() {
        super(null);
    }

    @Override
    public Integer getStatusCodeOverride() {
        return HttpStatus.FORBIDDEN.value();
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.TOS_AGREEMENT_REQUIRED;
    }
}
