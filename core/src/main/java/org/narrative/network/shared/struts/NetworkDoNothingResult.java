package org.narrative.network.shared.struts;

import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.dispatcher.StrutsResultSupport;

/**
 * This result type is needed for actions that manually handle writing out the servlet response.  An example of this
 * is the ServerStatusAction which can proxy AJAX requests to other servlets.
 * ServerStatusAction previously used an emptyResponse(), which would subsequently invoke an empty response after
 * writing out its own response, which could sometimes result in strange behavior since the JSP processing may
 * necessitate a session lookup, which could break things since the response has already been written.
 * <p>
 * Date: Oct 25, 2007
 * Time: 9:03:06 AM
 *
 * @author brian
 */
public class NetworkDoNothingResult extends StrutsResultSupport {
    public void doExecute(String string, ActionInvocation actionInvocation) throws Exception {
    }
}
