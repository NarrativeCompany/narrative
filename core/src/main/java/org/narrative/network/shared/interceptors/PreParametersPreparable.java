package org.narrative.network.shared.interceptors;

/**
 * This interface can be used to handle what typically might happen in an action constructor.  Since action
 * constructors are invoked prior to any of the interceptors being run, we are very limited in terms of what
 * dependencies our action constructors can have on other application constructs.  Thus, I've added this
 * interface (similar to the Preparable interface) that allows you to "do stuff" prior to the initial
 * setting of parameters on the action.  Essentially, this gives us a two-tiered "prepare" mechanism as
 * opposed to the current single-tier "prepare".  Note, however, that as of the call to preParametersPrepare,
 * no parameters will have been set on the action.  Thus, if your preParametersPrepare method needs to rely
 * on one or more parameters, it will have to grab them directly from the HttpServletRequest
 * (or HttpServletRequestResponseHandler) and convert the values from strings as appropriate.
 * <p>
 * Date: Feb 13, 2007
 * Time: 8:53:01 AM
 *
 * @author Brian
 */
public interface PreParametersPreparable {
    public void preParametersPrepare();
}
