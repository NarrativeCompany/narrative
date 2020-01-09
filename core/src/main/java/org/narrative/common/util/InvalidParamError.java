package org.narrative.common.util;

import com.google.common.annotations.VisibleForTesting;
import org.narrative.network.customizations.narrative.service.api.model.ErrorType;

import javax.servlet.http.HttpServletResponse;

import java.util.Collection;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Jun 23, 2010
 * Time: 2:11:56 PM
 *
 * @author brian
 */
public class InvalidParamError extends ApplicationError {
    /**
     * Protected constructor so we can extend this class for unit tests and use without having to stand up the stack
     */
    @VisibleForTesting
    protected InvalidParamError(String message) {
        super(message);
    }

    public InvalidParamError(String paramName, String paramValue) {
        super(wordlet("invalidParam.error", paramName, paramValue));
    }

    public static void recordError(Collection<String> errors, String paramName, String paramValue) {
        errors.add(wordlet("invalidParam.error", paramName, paramValue));
    }

    @Override
    public Integer getStatusCodeOverride() {
        return HttpServletResponse.SC_NOT_FOUND;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.NOT_FOUND;
    }
}
