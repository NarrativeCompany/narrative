package org.narrative.network.shared.servlet;

import org.narrative.common.util.IPHttpUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.common.util.processes.TraceProcessHistory;
import org.narrative.common.util.trace.TraceItem;
import org.narrative.common.util.trace.TraceManager;
import org.narrative.common.web.HttpServletRequestResponseHandler;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.context.NetworkContextInternal;
import org.narrative.network.shared.context.RequestType;
import org.narrative.network.shared.processes.ServletRequestProcess;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 8, 2005
 * Time: 11:22:23 PM
 */
@Component
public class NetworkFilter implements Filter {
    private static final NetworkLogger logger = new NetworkLogger(NetworkFilter.class);

    private final SpringEndpointWhitelist springEndpointWhitelist;
    private final NarrativeProperties narrativeProperties;

    public NetworkFilter(SpringEndpointWhitelist springEndpointWhitelist, NarrativeProperties narrativeProperties) {
        this.springEndpointWhitelist = springEndpointWhitelist;
        this.narrativeProperties = narrativeProperties;
    }

    public static void setStandardHttpHeadersForAllResponses(HttpServletResponse response) {
        // set X-UA-Compatible header to try to prevent IE's compatibility mode
        response.setHeader("X-UA-Compatible", "IE=edge");
        //Set Compact Privacy Policy aka P3P
        //Corresponds to p3p.xml
        response.setHeader("P3P", "CAO CUR ADM DEV TAI PSA PSD IVA IVD CON TEL OTP OUR DEL SAM UNR PUB OTR IND PHY ONL UNI COM NAV INT DEM CNT STA PRE LOC");
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("NetworkFilter.init");
    }

    public void destroy() {

    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
        // bl: need this so that POST parameters will be decoded properly by Tomcat.  need to set the character
        // encoding prior to actually getting at any of the request parameters.
        // for GET requests, we have to use the URIEncoding attribute on the Connector element.
        // refer: http://tomcat.apache.org/tomcat-5.5-doc/config/http.html
        // for general information on encoding issues, refer:
        // http://java.sun.com/developer/technicalArticles/Intl/HTTPCharset/
        // http://ppewww.ph.gla.ac.uk/~flavell/charset/form-i18n.html
        request.setCharacterEncoding(IPUtil.IANA_UTF8_ENCODING_NAME);

        // bl: include the networkRegistry on every request
        request.setAttribute(NetworkRegistry.NETWORK_REGISTRY, NetworkRegistry.getInstance());
        boolean tracing = false;
        try {

            // if not http then just delegate
            if ((!(request instanceof GHttpServletRequest) || !(response instanceof GHttpServletResponse))) {
                throw UnexpectedError.getRuntimeException("Unsupported request! must be GHttpServletRequest and GHttpServletResponse as set up in NetworkResponseWrapperFilter!");
            }

            final GHttpServletResponse httpServletResponse = (GHttpServletResponse) response;
            final GHttpServletRequest httpServletRequest = (GHttpServletRequest) request;

            boolean isDontAllowParamFetching = httpServletRequest.isRawRequestBody();

            final HttpServletRequestResponseHandler reqResp = new HttpServletRequestResponseHandler(httpServletRequest, httpServletResponse, isDontAllowParamFetching);

            //see if this is a tracing request
            if (!reqResp.isDontAllowParamFetching()) {
                String trace = request.getParameter("trace");
                tracing = trace != null && trace.equalsIgnoreCase("true");
                if (tracing && NetworkRegistry.getInstance().isProductionServer()) {
                    String password = request.getParameter("tracePw");
                    tracing = password != null && password.equals("dsBo8ECnmXsNVE7wzuty4q6o");
                }
                TraceManager.setTracing(tracing);
                if (tracing) {
                    TraceItem traceItem = new TraceProcessHistory("Root");
                    TraceManager.startTrace(traceItem);
                }
            }

            setStandardHttpHeadersForAllResponses(httpServletResponse);

            boolean isSpringApi = springEndpointWhitelist.isWhitelisted(httpServletRequest.getRequestURI());

            ServletRequestProcess process = new ServletRequestProcess("NetworkFilter", httpServletRequest, httpServletResponse);
            ProcessManager.getInstance().pushProcess(process);

            // bl: append the ServletRequestProcess OID to the thread name
            String oldName = Thread.currentThread().getName();
            Thread.currentThread().setName(oldName + "-" + process.getProcessOid());

            try {
                //put the global partition in scope
                TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                    protected Object doMonitoredTask() {
                        NetworkContextInternal ctx = (NetworkContextInternal) getNetworkContext();

                        //create the request response and set it on the action
                        ctx.setRequestResponse(reqResp);

                        String serverName = request.getServerName();

                        // if the request is on the cluster cp port, execute a cluster request
                        if (NetworkRegistry.getInstance().getClusterCpPort() == request.getLocalPort()) {
                            ctx.setRequestType(RequestType.CLUSTER_CP);
                            executeRequest(filterChain, request, response);

                        } else if (narrativeProperties.getKycQueue().getPort() == request.getLocalPort()) {
                            ctx.setRequestType(RequestType.KYC_QUEUE);
                            executeRequest(filterChain, request, response);

                            //otherwise find and push to the current area, and execute the request
                        } else {
                            // bl: the Narrative platform Area is a singleton, so just look it up.
                            Area area = Area.dao().getNarrativePlatformArea();

                            // bl: if the domain doesn't match, then do a redirect to the proper domain. this is necessary in order
                            // to redirect narrative.org to www.narrative.org
                            // bl: don't want to do any redirects for the Spring REST API
                            if (!isSpringApi && !serverName.equalsIgnoreCase(area.getPrimaryAreaDomainName())) {
                                String redirect = reqResp.getUrl().replace(reqResp.getScheme() + "://" + serverName, area.getPrimaryAreaUrl());
                                return sendResponseRedirect(httpServletResponse, redirect, true);
                            }

                            ctx.setRequestType(RequestType.NARRATIVE);

                            AreaTaskImpl<Object> task = new AreaTaskImpl<Object>() {
                                protected Object doMonitoredTask() {
                                    executeRequest(filterChain, request, response);
                                    return null;
                                }
                            };

                            // bl: register this task so that we flush once the AreaTask has completed while the realm session is still in
                            // scope to make sure the flush (and any event listeners) have access to the current realm partition, if necessary.
                            PartitionGroup.getCurrentPartitionGroup().registerTaskForFlushingOnSuccess(task);

                            try {
                                getNetworkContext().doAreaTask(area, task);
                            } finally {
                                // bl: log every request through the Spring API here
                                if(isSpringApi) {
                                    if(logger.isInfoEnabled()) logger.info("\"" + (process.isOutlier() ? "SLOW " : "") + "REQUEST_COMPLETED" + (PartitionGroup.isCurrentPartitionGroupInError() ? " IN ERROR!" : ".") + " Duration: " + (long) process.getTotalRunningTime() + "ms");
                                }
                            }
                        }
                        return null;
                    }

                }, true /* bypass error logging since our struts interceptors should handle it */);
            } finally {
                // bl: make sure we ALWAYS set the thread name back when we're done.
                // the thread name is typically changed in SpringProcessInterceptor, but we
                // don't want to reset it until after we've logged this final request log.
                Thread.currentThread().setName(oldName);
                ProcessManager.getInstance().popProcess();
            }
        } catch (Throwable t) {
            // all exceptions not caught by our Struts interceptor should be caught and recorded here.
            StatisticManager.recordException(t, true /* filter exception */, new HttpServletRequestResponseHandler((HttpServletRequest) request, (HttpServletResponse) response, false));
            throw UnexpectedError.getRuntimeException("Failed executing request.  Exception bubbled up to NetworkFilter.", t, true);
        } finally {
            if (tracing) {
                TraceManager.endAllTraces();
            }
            TraceManager.setTracing(false);
        }
    }

    private Object sendResponseRedirect(HttpServletResponse httpServletResponse, String redirect, boolean isPermanent) {
        try {
            if (isPermanent) {
                IPHttpUtil.sendPermanentRedirect(httpServletResponse, redirect);
            } else {
                httpServletResponse.sendRedirect(redirect);
            }
            return null;
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Error sending redirect to " + redirect, e);
        }
    }

    private void executeRequest(FilterChain filterChain, ServletRequest request, ServletResponse response) {
        try {
            filterChain.doFilter(request, response);
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Failed executing request-IOException", e, true);
        } catch (ServletException e) {
            throw UnexpectedError.getRuntimeException("Failed executing request-ServletException", e, true);
        }
    }
}

