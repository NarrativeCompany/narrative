package org.narrative.network.shared.services;

import org.narrative.common.util.IPHttpUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.HttpServletRequestResponseHandler;
import org.narrative.common.web.struts.MethodPropertiesUtil;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.shared.authentication.UserSession;
import org.narrative.network.shared.baseactions.NetworkAction;
import org.narrative.network.shared.struts.NetworkResponses;
import org.narrative.network.shared.util.NetworkLogger;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.ServletRedirectResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 7, 2005
 * Time: 8:19:20 PM
 */
public class NetworkServletRedirectResult extends ServletRedirectResult {

    public static final String PERMANENT_REDIRECT_KEY = NetworkServletRedirectResult.class.getName() + "-PermanentRedirect";

    private static final NetworkLogger logger = new NetworkLogger(NetworkServletRedirectResult.class);

    protected void doExecute(String finalLocation, final ActionInvocation invocation) throws Exception {
        final HttpServletRequest request = ServletActionContext.getRequest();
        final HttpServletResponse response = ServletActionContext.getResponse();
        final NetworkAction networkAction = (NetworkAction) invocation.getAction();

        finalLocation = getRedirectUrl(finalLocation, networkAction, request, invocation.getInvocationContext(), prependServletContext);

        if (HttpServletRequestResponseHandler.isRequestedAsAjax(request)) {
            throw UnexpectedError.getRuntimeException("Should never attempt to do an HTTP redirect response on AJAX requests! finalLocation:" + finalLocation);
        }

        final Boolean isPermanent = (Boolean) actionContext().get(PERMANENT_REDIRECT_KEY);
        final String fFinalLocation = finalLocation;
        final UserSession session = UserSession.hasSession() ? UserSession.getUserSession() : null;
        final boolean isReadOnly = PartitionGroup.getCurrentPartitionGroup().isReadOnly();

        // bl: changed the behavior of our redirect results so that we won't actually send the redirect for the user until after
        // the transaction has completed.  this way, we can be sure that the current transaction has closed
        // before the subsequent request is made.  without this, there is a race condition that oftentimes can
        // prevent proper display of the page due to the fact that the new transaction will start while the current
        // transaction is still processing, thus preventing the subsequent request from seeing changes made
        // in the original request.
        // todo: should this be OnSuccessOrError? we were getting issues where the thread was marked in error due to a JoinGroupRedirect
        // exception that was happening at an unexpected time (during creation of CreateContentTask).
        // the exception first marked the thread as being in error and then subsequently was caught and translated to a subscribe redirect.
        // since the thread was in error, however, this end of partition group runnable was not triggered and thus the user
        // was being presented with a blank page (effectively empty response).  this was particularly difficult to troubleshoot
        // due to the lack of exception in the error log and the plain blank page displayed to the user.
        DelayedResultRunnable.process(() -> {
            try {
                // jw: if this was a post request lets get the users session and ensure that the redirect is recorded
                //     in case we have any double submits.
                if (session != null && IPHttpUtil.isPost(request) && !isReadOnly && MethodPropertiesUtil.isPreventDoublePosting(invocation)) {
                    synchronized (session) {
                        HttpPostSessionObject.HttpPostFormData formData = HttpPostSessionObject.getForUserSession(session).getFormData(request.getParameter(NetworkAction.FORM_TYPE_PARAM_NAME), request.getParameterMap(), session, false);
                        if (formData != null) {
                            formData.setRedirect(fFinalLocation);
                        }
                    }
                }

                if (isPermanent != null && isPermanent) {
                    IPHttpUtil.sendPermanentRedirect(response, fFinalLocation);
                } else {
                    response.sendRedirect(fFinalLocation);
                }
            } catch (IOException e) {
                throw UnexpectedError.getRuntimeException("Failed sending a redirect response to a user on end of thread!", e, true);
            }
        }, () -> {
            logger.error("Failed sending redirect response due to PartitionGroup being in error! redirect: " + fFinalLocation, new Throwable());
        });
    }

    public static String getRedirectUrl(NetworkAction networkAction, HttpServletRequest request, ActionContext actionContext) {
        return getRedirectUrl(null, networkAction, request, actionContext, true);
    }

    private static String getRedirectUrl(String finalLocation, NetworkAction networkAction, HttpServletRequest request, ActionContext actionContext, boolean prependServletContext) {
        finalLocation = getFinalLocation(finalLocation, networkAction, request, actionContext);
        finalLocation = networkAction.registerConfirmationMessage(finalLocation);

        // bl: don't allow !execute in redirect URLs
        if (finalLocation.contains(MethodPropertiesUtil.POST_ONLY_BANG_METHOD_NAME)) {
            // just remove "!execute" from the URL altogether.
            String newLocation = MethodPropertiesUtil.POST_ONLY_METHOD_PATTERN.matcher(finalLocation).replaceAll("");
            logger.info("Stripped " + MethodPropertiesUtil.POST_ONLY_BANG_METHOD_NAME + " from URL: " + finalLocation + " - New URL: " + newLocation);
            finalLocation = newLocation;
        }

        if (isUrlPathUrl(finalLocation)) {
            if (!finalLocation.startsWith("/")) {
                finalLocation = "/" + finalLocation;
            }

            // if the URL's are relative to the servlet context, append the servlet context path
            if (prependServletContext && !isEmpty(request.getContextPath())) {
                finalLocation = request.getContextPath() + finalLocation;
            }

            // bl: opting to not encode redirect urls anymore.  we'll handle session sharing
            // purely on the server (via client md5s and the like).
            //finalLocation = response.encodeRedirectURL(finalLocation);
        }

        return finalLocation;
    }

    private static String getFinalLocation(String finalLocation, NetworkAction networkAction, HttpServletRequest request, ActionContext context) {
        if (!isEmpty(finalLocation)) {
            return finalLocation;
        }
        // bl: always using the ActionContext redirect param ahead of the NetworkAction redirect param.
        Object oFinalLocation = context.get(NetworkResponses.REDIRECT_ACTION_CONTEXT_PARAM);
        if (oFinalLocation != null && oFinalLocation instanceof String && !isEmpty((String) oFinalLocation)) {
            return (String) oFinalLocation;
        }
        finalLocation = networkAction.getRedirect();
        if (!isEmpty(finalLocation)) {
            return finalLocation;
        }
        // bl: referrer comes last
        finalLocation = IPHttpUtil.getReferrerHeader(request);
        if (!isEmpty(finalLocation)) {
            return finalLocation;
        }
        // jw: we may want to consider using the authRealm baseUrl here instead of empty string.
        return "";
    }

    /**
     * need a static version of ServletRedirectResult.isPathUrl, so copying that method here.
     */
    private static boolean isUrlPathUrl(String url) {
        try {
            URI uri = URI.create(url);
            if (uri.isAbsolute()) {
                URL validUrl = uri.toURL();
                if (logger.isTraceEnabled()) {
                    logger.trace("[#0] is full url, not a path", url);
                }
                return validUrl.getProtocol() == null;
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("[#0] isn't absolute URI, assuming it's a path", url);
                }
                return true;
            }
        } catch (IllegalArgumentException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("[#0] isn't a valid URL, assuming it's a path", e, url);
            }
            return true;
        } catch (MalformedURLException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("[#0] isn't a valid URL, assuming it's a path", e, url);
            }
            return true;
        }
    }
}
