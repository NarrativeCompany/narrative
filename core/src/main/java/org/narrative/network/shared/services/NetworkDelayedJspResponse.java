package org.narrative.network.shared.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.StrutsResultSupport;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import java.io.IOException;

/**
 * Modeled after ServletDispatcherResult, this result handles sending responses after the transaction
 * for the current request has completed.  this is most useful for things like AJAX reload responses
 * where you need to be able to guarantee that the previous request had committed its transactions
 * prior to reloading the page.
 * <p>
 * Very similar to NetworkServletRedirectResult, which sends traditional 302 redirects at the end of PartitionGroup.
 * <p>
 * NOTE: Since this result runs AFTER the transactions have committed, the Hibernate sessions have also been closed
 * so this result should only be used with actions and JSPs that will not require access to Hibernate objects.
 * <p>
 * Date: Jan 26, 2010
 * Time: 9:47:40 AM
 *
 * @author brian
 */
public class NetworkDelayedJspResponse extends StrutsResultSupport {
    @Override
    protected void doExecute(final String finalLocation, ActionInvocation invocation) throws Exception {
        final PageContext pageContext = ServletActionContext.getPageContext();

        if (pageContext != null) {
            pageContext.include(finalLocation);
        } else {
            final HttpServletRequest request = ServletActionContext.getRequest();
            final HttpServletResponse response = ServletActionContext.getResponse();
            PartitionGroup.addEndOfPartitionGroupRunnableForSuccessOrError(new DelayedResultRunnable() {
                @Override
                public void run() {
                    try {
                        RequestDispatcher dispatcher = request.getRequestDispatcher(finalLocation);

                        // if the view doesn't exist, let's do a 404
                        if (dispatcher == null) {
                            response.sendError(404, "result '" + finalLocation + "' not found");

                            return;
                        }

                        // If we're included, then include the view
                        // Otherwise do forward
                        // This allow the page to, for example, set content type
                        if (!response.isCommitted() && (request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH) == null)) {
                            request.setAttribute("struts.view_uri", finalLocation);
                            request.setAttribute("struts.request_uri", request.getRequestURI());

                            dispatcher.forward(request, response);
                        } else {
                            dispatcher.include(request, response);
                        }
                    } catch (ServletException e) {
                        throw UnexpectedError.getRuntimeException("Failed executing delayed JSP response!", e);
                    } catch (IOException e) {
                        throw UnexpectedError.getRuntimeException("Failed executing delayed JSP response!", e);
                    }
                }
            });
        }
    }
}
