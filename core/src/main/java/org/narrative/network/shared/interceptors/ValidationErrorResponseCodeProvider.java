package org.narrative.network.shared.interceptors;

/**
 * This interface allows an action to specify different response codes (other than input) for validation error handling.
 * Date: Jan 17, 2008
 * Time: 12:30:28 PM
 *
 * @author brian
 */
public interface ValidationErrorResponseCodeProvider {
    public String getErrorResponseCode();
}
