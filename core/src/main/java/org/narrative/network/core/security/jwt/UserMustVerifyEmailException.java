package org.narrative.network.core.security.jwt;

import org.narrative.common.util.ApplicationError;
import org.narrative.network.customizations.narrative.service.api.model.ErrorType;
import org.springframework.http.HttpStatus;

/**
 * Date: 10/1/18
 * Time: 8:28 AM
 *
 * @author brian
 */
public class UserMustVerifyEmailException extends ApplicationError {
    private static final long serialVersionUID = 3922864629514936612L;
    
    public UserMustVerifyEmailException() {
        super(null);
    }

    @Override
    public Integer getStatusCodeOverride() {
        return HttpStatus.FORBIDDEN.value();
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.EMAIL_VERIFICATION_REQUIRED;
    }
}
