package org.narrative.base;

import org.narrative.common.util.InvalidParamError;

/**
 * Mocked {@link org.narrative.common.util.InvalidParamError} so we can use in unit tests without the stack running
 */
public class MockInvalidParamError extends InvalidParamError {
    public MockInvalidParamError(String message) {
        super(message);
    }
}
