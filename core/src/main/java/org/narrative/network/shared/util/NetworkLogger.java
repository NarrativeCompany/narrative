package org.narrative.network.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.config.JacksonConfiguration;
import org.narrative.config.StaticConfig;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.shared.authentication.UserSession;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.security.TransientRole;
import com.opensymphony.xwork2.ActionContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: May 17, 2006
 * Time: 5:20:43 PM
 */
public class NetworkLogger extends NarrativeLogger {

    private static ObjectMapper objectMapper;

    /**
     * NetworkLogger only supports loggers on a per class basis
     *
     * @param clazz to create logger from
     */
    public NetworkLogger(Class clazz) {
        super(clazz);
    }

    @Override
    public void trace(String message) {
        super.trace(messageWithNetworkInfo(message));
    }

    @Override
    public void trace(String message, Object... objs) {
        super.trace(messageWithNetworkInfo(objectsToMessage(message, objs)));
    }

    @Override
    public void trace(String message, Throwable t) {
        super.trace(messageWithNetworkInfo(message), t);
    }

    @Override
    public void trace(String message, Throwable t, Object... objs) {
        super.trace(messageWithNetworkInfo(objectsToMessage(message, objs)), t);
    }

    @Override
    public void debug(String message) {
        super.debug(messageWithNetworkInfo(message));
    }

    @Override
    public void debug(String message, Object... objs) {
        super.debug(messageWithNetworkInfo(objectsToMessage(message, objs)));
    }

    @Override
    public void debug(String message, Throwable t) {
        super.debug(messageWithNetworkInfo(message), t);
    }

    @Override
    public void debug(String message, Throwable t, Object... objs) {
        super.debug(messageWithNetworkInfo(objectsToMessage(message, objs)), t);
    }

    @Override
    public void error(String message) {
        super.error(messageWithNetworkInfo(message));
    }

    @Override
    public void error(String message, Object... objs) {
        super.error(messageWithNetworkInfo(objectsToMessage(message, objs)));
    }

    @Override
    public void error(String message, Throwable t) {
        super.error(messageWithNetworkInfo(message), t);
    }

    @Override
    public void error(String message, Throwable t, Object... objs) {
        super.error(messageWithNetworkInfo(objectsToMessage(message, objs)), t);
    }

    @Override
    public void warn(String message) {
        super.warn(messageWithNetworkInfo(message));
    }

    @Override
    public void warn(String message, Object... objs) {
        super.warn(messageWithNetworkInfo(objectsToMessage(message, objs)));
    }

    @Override
    public void warn(String message, Throwable t) {
        super.warn(messageWithNetworkInfo(message), t);
    }

    @Override
    public void warn(String message, Throwable t, Object... objs) {
        super.warn(messageWithNetworkInfo(objectsToMessage(message, objs)), t);
    }

    @Override
    public void info(String message) {
        super.info(messageWithNetworkInfo(message));
    }

    @Override
    public void info(String message, Object... objs) {
        super.info(messageWithNetworkInfo(objectsToMessage(message, objs)));
    }

    @Override
    public void info(String message, Throwable t) {
        super.info(messageWithNetworkInfo(message), t);
    }

    @Override
    public void info(String message, Throwable t, Object... objs) {
        super.info(messageWithNetworkInfo(objectsToMessage(message, objs)), t);
    }

    private String messageWithNetworkInfo(String message) {
        StringBuilder stringBuilder = new StringBuilder(message);
        // bl: changed to output all headers when debug is enabled.
        stringBuilder.append(getCurrentContextInfo(null, isDebugEnabled()));

        return stringBuilder.toString();
    }

    /**
     * get extra logging information from the current context.
     *
     * @param reqResp            the RequestResponseHandler to use for logging information, if any.  use null to use the current context.
     * @param includeHttpHeaders true if http headers should be included in the message
     * @return the current context info for logging purposes
     */
    public static String getCurrentContextInfo(RequestResponseHandler reqResp, boolean includeHttpHeaders) {
        StringBuilder stringBuilder = new StringBuilder();
        if (reqResp != null) {
            appendReqRespInfo(stringBuilder, reqResp, includeHttpHeaders);
        } else {
            if (!isNetworkContextSet()) {
                return "";
            }
            NetworkContext networkContext = networkContext();

            reqResp = networkContext.getReqResp();
            if (networkContext.isHasPrimaryRole()) {
                PrimaryRole role = networkContext.getPrimaryRole();
                // bl: workaround some issues where we get a nested exception when logging errors.
                // do a safety check to only get the uniqueName if there actually is a session set.
                // otherwise, we may get an exception when trying to get the uniqueName which will cause
                // a cascading error and hide the original exception.
                // bl: likewise if there isn't a current GLOBAL partition set.
                // bl: note that this only applies for non-TransientRoles (i.e. User)
                if (!(role instanceof TransientRole) && (!PartitionType.GLOBAL.hasCurrentSession() || !PartitionType.GLOBAL.hasCurrentPartitionOid())) {
                    stringBuilder.append(" User/Unknown-NoSessionOrPartition");
                } else {
                    stringBuilder.append(" ").append(role.getRoleStringForLogging());
                }
                UserSession userSession = UserSession.getUserSession();
                if (userSession != null) {
                    stringBuilder.append(" Visit/").append(userSession.getUniqueVisitOid());
                }
            }

            if (reqResp != null) {
                appendReqRespInfo(stringBuilder, reqResp, includeHttpHeaders);
            } else {
                stringBuilder.append(" NetworkContext: ");
                stringBuilder.append(" Request Type ==> [").append(networkContext.getRequestType()).append("]");
            }
        }
        return stringBuilder.toString();
    }

    private static void appendReqRespInfo(StringBuilder stringBuilder, RequestResponseHandler reqResp, boolean includeHttpHeaders) {
        stringBuilder.append(" Request: ");
        stringBuilder.append(" URL ==> [").append(reqResp.getUrl()).append("]");
        stringBuilder.append(" Method ==> ").append(reqResp.getMethod());

        // bl: if it's JSON, then let's log the JSON RequestBody instead of parameters
        if(MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(reqResp.getContentType())) {
            stringBuilder.append(" RequestBody ==> [");
            Object requestBodyObj = reqResp.getRequestBodyObject();
            if(requestBodyObj==null) {
                stringBuilder.append(requestBodyObj);
            } else {
                stringBuilder.append("\"");
                stringBuilder.append(getRequestBodyString(requestBodyObj));
                stringBuilder.append("\"");
            }
            stringBuilder.append("]");
        } else {
            Map params = null;
            {
                Map<String, List<String>> multipartFiles = reqResp.getMultipartFiles();
                HttpServletRequest httpServletRequest = ActionContext.getContext() != null ? ServletActionContext.getRequest() : null;
                if (httpServletRequest instanceof MultiPartRequestWrapper) {
                    MultiPartRequestWrapper multiPartRequestWrapper = (MultiPartRequestWrapper) httpServletRequest;
                    params = multiPartRequestWrapper.getParameterMap();
                    Map<String, List<String>> paramNameToFilenames = new LinkedHashMap<>();
                    Enumeration<String> enumeration = multiPartRequestWrapper.getFileParameterNames();
                    if (enumeration != null) {
                        for (String paramName : Collections.list(enumeration)) {
                            paramNameToFilenames.put(paramName, Arrays.asList(multiPartRequestWrapper.getFileNames(paramName)));
                        }
                    }
                    logMultipartFiles(stringBuilder, paramNameToFilenames);
                } else if (!isEmptyOrNull(multipartFiles)) {
                    logMultipartFiles(stringBuilder, multipartFiles);
                }
                if(params==null) {
                    params = reqResp.isDontAllowParamFetching() ? null : reqResp.getParams();
                }
            }

            stringBuilder.append(" Parameters ==> [");
            if (params == null) {
                stringBuilder.append("skipped");
            } else {
                stringBuilder.append(RequestResponseHandler.getParametersString(params));
            }
            stringBuilder.append("]");
        }

        // bl: always include the request body now (when specified).  will help with debugging recurly push commands.
        if (reqResp.isRawRequestBody()) {
            stringBuilder.append(" RequestBody ==> [").append(reqResp.getRequestBody().replace("\n", "").replace("\r", "")).append("]");
        }

        stringBuilder.append(" Referer ==> [").append(reqResp.getReferrer()).append("]");
        // bl: in order to prevent an infinite loop when needing to massage an IP (due to the logging done
        // when an IP is malformed), we only want to get the unmassaged IP here.
        stringBuilder.append(" IP ==> [").append(reqResp.getUnmassagedRemoteHostIp()).append("]");
        String visitOid;
        if (UserSession.hasSession()) {
            visitOid = UserSession.getUserSession().getUniqueVisitOid().toString();
        } else {
            visitOid = null;
        }
        stringBuilder.append(" VisitOid ==> [").append(visitOid).append("]");
        if (includeHttpHeaders) {
            stringBuilder.append(" Headers ==> [");
            Enumeration headerNames = reqResp.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                stringBuilder.append("{").append(headerName).append(": ").append(reqResp.getHeader(headerName)).append("}");
            }
            //stringBuilder.append(" Client key ==> [").append(reqResp.getKeyForClientRequest()).append("]");
            stringBuilder.append("]");
        } else {
            stringBuilder.append(" User-Agent ==> [").append(reqResp.getHeader("user-agent")).append("]");
            stringBuilder.append(" X-Requested-With ==> [").append(reqResp.getHeader("X-Requested-With")).append("]");
            // bl: start logging the range header so we can tell what type of file download request this might be.
            String range = reqResp.getHeader("range");
            if (!isEmpty(range)) {
                stringBuilder.append(" Range ==> [").append(range).append("]");
            }
        }
    }

    private static void logMultipartFiles(StringBuilder stringBuilder, Map<String, List<String>> paramNameToFilesnames) {
        stringBuilder.append(" MultiPart Files ==> [");
        for (Map.Entry<String, List<String>> entry : paramNameToFilesnames.entrySet()) {
            String paramName = entry.getKey();
            List<String> filenames = entry.getValue();
            stringBuilder.append(paramName).append("=").append(Arrays.toString(filenames.toArray()));
        }
        stringBuilder.append("]");
    }

    private static String getRequestBodyString(Object requestBodyObj) {
        try {
            return objectMapper.writeValueAsString(requestBodyObj);
        } catch (JsonProcessingException e) {
            // ignore JSON processing issue and just return empty
            // bl: we can't log this exception through standard logging since it happens during a separate log command.
            // i don't believe there are any JSON processing exceptions that should happen here considering
            // we were able to deserialize the request body object. it should be pretty safe to assume we can
            // serialize the object back into JSON, but if you don't see the request body in the logs,
            // this is the first place to look.
            // nevertheless, we'll log via stderr directly to help identify potential bugs, as dirty as this feels :)
            e.printStackTrace();
            return StringUtils.EMPTY;
        }
    }

    public static void init() {
        NarrativeLogger.setCurrentContextProvider(() -> getCurrentContextInfo(null, true));

        initObjectMapper();
    }

    private static void initObjectMapper() {
        JacksonConfiguration jacksonConfiguration = StaticConfig.getBean(JacksonConfiguration.class);
        // bl: start with the default configuration from Spring
        Jackson2ObjectMapperBuilderCustomizer customizer = jacksonConfiguration.jackson2ObjectMapperBuilderCustomizer();
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        customizer.customize(builder);

        // and add our filter to customize output values, such as sanitizing private values like passwords
        // and truncating long values
        builder.filters(new SimpleFilterProvider().addFilter(NetworkLoggerPropertyFilterMixIn.FILTER_NAME, new NetworkLoggerPropertyFilter()));
        // bl: use the mix-in on all Objects so that the filter is applied universally
        builder.mixIn(Object.class, NetworkLoggerPropertyFilterMixIn.class);

        objectMapper = builder.build();
    }
}
