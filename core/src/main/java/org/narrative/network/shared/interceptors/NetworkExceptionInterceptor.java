package org.narrative.network.shared.interceptors;

import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.Debug;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.PageNotFoundError;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.common.web.HttpServletRequestResponseHandler;
import org.narrative.common.web.struts.MethodPropertiesUtil;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.context.NetworkContextInternal;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.security.Guest;
import org.narrative.network.shared.struts.NetworkResponses;
import org.narrative.network.shared.util.NetworkLogger;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.config.entities.ExceptionMappingConfig;
import com.opensymphony.xwork2.interceptor.Interceptor;
import org.apache.jasper.JasperException;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletResponse;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: May 18, 2006
 * Time: 2:18:12 PM
 * To change this template use File | Settings | File Templates.
 * <p>
 * Our NetworkExceptionInterceptor is a copy of Struts's ExceptionMappingInterceptor.  We needed
 * to modify its behavior in order to properly handle exceptions for ajax requests.
 */
public class NetworkExceptionInterceptor implements Interceptor {
    private static final NetworkLogger logger = new NetworkLogger(NetworkExceptionInterceptor.class);

    public void destroy() {
    }

    public void init() {
    }

    public static final String HAVE_PARAMETERS_BEEN_SET_FOR_REQUEST_CONTEXT_ATTRIBUTE = NetworkExceptionInterceptor.class.getSimpleName() + ".haveParamsBeenSet";

    public String intercept(ActionInvocation invocation) throws Exception {
        String result;

        try {
            result = invocation.invoke();
        } catch (Throwable e) {
            if (logger.isErrorEnabled()) {
                logger.error("Exception thrown in action: " + invocation.getProxy().getActionName(), e);
            }
            StatisticManager.recordException(e, false, null);

            // bl: once we've logged the main exception, detect character collation issues here and give a generic message
            Throwable rootCause = Debug.getRootCause(e);
            if (rootCause instanceof SQLException) {
                if (rootCause.getMessage().contains("Illegal mix of collations") || rootCause.getMessage().contains("Incorrect string value")) {
                    // bl: give the generic error message in this case instead.
                    e = new ApplicationError(wordlet("sqlException.invalidCharactersInRequest"));
                }
            }

            // if the response is already committed, then we can't do anything.  we already recorded the exception
            // above, so just let the call stack finish itself off.  this should avoid the ugly IllegalStateException
            // errors that seem to crop up whenever a user navigates away from a page while it is still loading
            // (which triggers a broken pipe exception that ultimately gets caught here, but since we've already
            // written to the response, we can't write any more data to it).
            {
                boolean canReturnJspError = false;
                HttpServletResponse response = ServletActionContext.getResponse();
                /*try {
                    // bl: try getting the writer.  if getting the writer succeeds, then that means we may have already
                    // written a JSP response (or we have not written any response yet) and thus we can return the usual
                    // JSP error response.
                    if(response.isCommitted()) {
                        response.getWriter();
                    }
                } catch(IllegalStateException ise) {
                    canReturnJspError = false;
                }*/
                // bl: if it's a JasperException, then we know the error happened while rendering JSP, and we don't want to
                // nest JSP evaluations anymore
                if (!response.isCommitted() && !(e instanceof JasperException) && !(e.getCause() instanceof JasperException)) {
                    // if we got here, then it means that the response is not yet committed, in which case we can
                    // use the writer to write out the new s_exception.jsp JSP response.
                    // if the page has already been committed, then it means that we already tried to output content
                    // so let's just leave it at that and avoid all kinds of crazy errors when we nest JSP evaluations together.
                    canReturnJspError = true;
                    // bl: we used to try response.getWriter() if the response was already committed so that we could
                    // nest the s_exception.jsp error page inside of the current page. that causes all kinds of
                    // funky JSP evaluation issues, so we aren't going to do that anymore. only evaluate JSP
                    // if the response hasn't written out status code and headers yet (not committed)
                }
                // if we've already called getOutputStream (per the IllegalStateException when calling response.getWriter()),
                // then let's just do nothing else to avoid further IllegalStateExceptions when trying to write out
                // the s_exception.jsp response.
                if (!canReturnJspError) {
                    return NetworkResponses.doNothingResponse();
                }

                int statusCode;
                if (e instanceof ApplicationError) {
                    ApplicationError appError = (ApplicationError) e;
                    // bl: allow ApplicationErrors to define their own custom status code, if desired.
                    if (appError.getStatusCodeOverride() != null) {
                        statusCode = appError.getStatusCodeOverride();
                    } else if (e instanceof AccessViolation) {
                        statusCode = HttpServletResponse.SC_UNAUTHORIZED;
                    } else if (e instanceof PageNotFoundError) {
                        statusCode = HttpServletResponse.SC_NOT_FOUND;
                    } else {
                        // bl: 400 / Bad Request is the default for all other ApplicationErrors.
                        statusCode = HttpServletResponse.SC_BAD_REQUEST;
                    }
                } else {
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                }
                response.setStatus(statusCode);
            }

            // bl: some errors happen before a role is set. if that's the case, let's set the PrimaryRole as a guest
            // so that we can at least render an error page.
            if (!networkContext().isHasPrimaryRole()) {
                ((NetworkContextInternal) networkContext()).setPrimaryRole(new Guest(networkContext().getAuthZone()));
            }

            // we are able to write out a JSP response, so proceed with our standard error handling.
            // bl: only return the AJAX error message result if the request was actually made as AJAX (regardless
            // of the @MethodDetails(requestType) annotation. if the request was not made as AJAX, then even
            // if it's mapped to only be AJAX, we may as well return a full error page vs. an AJAX JSON
            // result that will not be very useful when displayed in the browser.
            if (HttpServletRequestResponseHandler.isRequestedAsAjax(ServletActionContext.getRequest())) {
                publishException(invocation, e);
                return NetworkResponses.legacyAjaxErrorMessageResult();
            }

            // bl: only want to use the specialized result-type for exceptions if all of the parameters were
            // actually set on the action.  if not, then we very well may get errors (e.g. NPEs) when attempting
            // to render the error page.
            Boolean haveParamsBeenSet = (Boolean) invocation.getInvocationContext().get(HAVE_PARAMETERS_BEEN_SET_FOR_REQUEST_CONTEXT_ATTRIBUTE);
            if (haveParamsBeenSet != null && haveParamsBeenSet) {
                result = MethodPropertiesUtil.getResultTypeToUseForExceptions(invocation);
            } else {
                // params haven't been set yet?  in that case, we just want to use the default error page since
                // rendering the original form probably won't give the user much help anyway.
                result = null;
            }
            if (IPStringUtil.isEmpty(result)) {
                List exceptionMappings = invocation.getProxy().getConfig().getExceptionMappings();
                String mappedResult = this.findResultFromExceptions(exceptionMappings, e);
                if (mappedResult != null) {
                    result = mappedResult;
                } else {
                    throw UnexpectedError.getRuntimeException("Unmapped exception found", e);
                }
            }
            publishException(invocation, e);
        }

        return result;
    }

    private String findResultFromExceptions(List exceptionMappings, Throwable t) {
        String result = null;

        // Check for specific exception mappings.
        if (exceptionMappings != null) {
            int deepest = Integer.MAX_VALUE;
            for (Iterator iter = exceptionMappings.iterator(); iter.hasNext(); ) {
                ExceptionMappingConfig exceptionMappingConfig = (ExceptionMappingConfig) iter.next();
                int depth = getDepth(exceptionMappingConfig.getExceptionClassName(), t);
                if (depth >= 0 && depth < deepest) {
                    deepest = depth;
                    result = exceptionMappingConfig.getResult();
                }
            }
        }

        return result;
    }

    /**
     * Return the depth to the superclass matching. 0 means ex matches exactly. Returns -1 if there's no match.
     * Otherwise, returns depth. Lowest depth wins.
     */
    public int getDepth(String exceptionMapping, Throwable t) {
        return getDepth(exceptionMapping, t.getClass(), 0);
    }

    private int getDepth(String exceptionMapping, Class exceptionClass, int depth) {
        if (exceptionClass.getName().indexOf(exceptionMapping) != -1) {
            // Found it!
            return depth;
        }
        // If we've gone as far as we can go and haven't found it...
        if (exceptionClass.equals(Throwable.class)) {
            return -1;
        }
        return getDepth(exceptionMapping, exceptionClass.getSuperclass(), depth + 1);
    }

    public static final String THROWABLE_CONTEXT_PARAM = "throwable";

    /**
     * Default implementation to handle ExceptionHolder publishing. Pushes given ExceptionHolder on the stack.
     * Subclasses may override this to customize publishing.
     *
     * @param invocation The invocation to publish Exception for.
     * @param throwable  The exceptionHolder wrapping the Exception to publish.
     */
    protected void publishException(ActionInvocation invocation, Throwable throwable) {
        invocation.getInvocationContext().put(THROWABLE_CONTEXT_PARAM, throwable);
        if (NetworkRegistry.getInstance().isLocalOrDevServer()) {
            if (throwable instanceof ApplicationError) {
                invocation.getInvocationContext().put("extraExceptionSystemLogInfo", HtmlTextMassager.convertCrAndLfToHtml(HtmlTextMassager.disableHtml(((ApplicationError) throwable).getSystemLogInfo())));
            }
            invocation.getInvocationContext().put("stackTraceHtml", HtmlTextMassager.stackTraceAsHtml(throwable));
        }
    }
}
