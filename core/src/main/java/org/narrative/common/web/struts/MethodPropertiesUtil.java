package org.narrative.common.web.struts;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.HttpMethodType;
import org.narrative.common.web.HttpRequestType;
import org.narrative.common.web.HttpServletRequestResponseHandler;
import org.narrative.network.core.system.NetworkRegistry;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Date: Jun 22, 2006
 * Time: 10:54:13 AM
 *
 * @author Brian
 */
public class MethodPropertiesUtil {
    private static final NarrativeLogger logger = new NarrativeLogger(MethodPropertiesUtil.class);
    public static final String POST_ONLY_METHOD_NAME = "execute";
    public static final String POST_ONLY_BANG_METHOD_NAME = "!" + POST_ONLY_METHOD_NAME;
    // the pattern is \!execute[^\/\?\#]* 
    public static final Pattern POST_ONLY_METHOD_PATTERN = Pattern.compile(POST_ONLY_BANG_METHOD_NAME + "[^\\/\\?\\#]*");

    private static final Map<Class<?>, Map<String, MethodProperties>> s_actionClassToMethodNameToMethodType = new ConcurrentHashMap<Class<?>, Map<String, MethodProperties>>();

    public static HttpMethodType getHttpMethodType(ActionInvocation actionInvocation) {
        return getMethodProperties(actionInvocation).methodType;
    }

    public static boolean isReadOnlyRequest(ActionInvocation actionInvocation) {
        return getMethodProperties(actionInvocation).isReadOnly;
    }

    public static boolean isAjaxRequest(ActionInvocation actionInvocation) {
        // bl: updated so that it will detect when the request actually was submitted as an AJAX request, not just when
        // the action was configured to be an AJAX request.
        return HttpServletRequestResponseHandler.isRequestedAsAjax(ServletActionContext.getRequest()) || getHttpRequestType(actionInvocation).isAjax();
    }

    public static HttpRequestType getHttpRequestType(ActionInvocation actionInvocation) {
        return getMethodProperties(actionInvocation).requestType;
    }

    public static boolean isBypassValidate(ActionInvocation actionInvocation) {
        return getMethodProperties(actionInvocation).isBypassValidate;
    }

    public static String getResultTypeToUseForExceptions(ActionInvocation actionInvocation) {
        return getMethodProperties(actionInvocation).resultTypeToUseForExceptions;
    }

    public static String getErrorResponseCode(ActionInvocation actionInvocation) {
        return getMethodProperties(actionInvocation).errorResponseCode;
    }

    public static boolean isSSLOnlyRequest(ActionInvocation actionInvocation) {
        return getMethodProperties(actionInvocation).isSSLOnly;
    }

    public static boolean isPreventDoublePosting(ActionInvocation actionInvocation) {
        return getMethodProperties(actionInvocation).isPreventDoublePosting;
    }

    private static MethodProperties getMethodProperties(ActionInvocation actionInvocation) {
        return getMethodProperties(actionInvocation.getAction().getClass(), actionInvocation.getProxy().getMethod());
    }

    private static MethodProperties getMethodProperties(Class<?> actionClass, String actionMethod) {
        Map<String, MethodProperties> methodNameToType = s_actionClassToMethodNameToMethodType.get(actionClass);
        if (methodNameToType == null) {
            s_actionClassToMethodNameToMethodType.put(actionClass, methodNameToType = new ConcurrentHashMap<String, MethodProperties>());
        }
        MethodProperties methodProperties = methodNameToType.get(actionMethod);
        if (methodProperties == null) {
            // mirrors DefaultActionInvocation.invokeAction()
            Method method;
            try {
                method = actionClass.getMethod(actionMethod, new Class[0]);
            } catch (NoSuchMethodException e) {
                // hmm -- OK, try doXxx instead
                try {
                    String altMethodName = "do" + actionMethod.substring(0, 1).toUpperCase() + actionMethod.substring(1);
                    method = actionClass.getMethod(altMethodName, new Class[0]);
                } catch (NoSuchMethodException e1) {
                    // throw the original one

                    if (NetworkRegistry.getInstance().isLocalOrDevServer()) {
                        throw UnexpectedError.getRuntimeException("Failed lookup of method for action: " + actionMethod + " on " + IPUtil.getClassSimpleName(actionClass) + ". Must be a coding error!", e, true);
                    } else {
                        logger.warn("Failed lookup of method for action: " + actionMethod + " on " + IPUtil.getClassSimpleName(actionClass) + ". Must be a coding error!", e, true);
                    }
                    methodNameToType.put(actionMethod, errorDefault);
                    return errorDefault;
                }
            }

            methodProperties = new MethodProperties();
            MethodDetails methodDetails = method.getAnnotation(MethodDetails.class);

            if (methodDetails!=null && methodDetails.httpMethodType() != HttpMethodType.UNSPECIFIED) {
                // if specified, use the annotation
                methodProperties.methodType = methodDetails.httpMethodType();
            } else if (actionMethod.startsWith(Action.INPUT)) {
                // if the method starts with "input", it's a GET
                methodProperties.methodType = HttpMethodType.GET;
            } else {
                // method doesn't start with input, so default the method type to POST
                methodProperties.methodType = HttpMethodType.POST;
            }

            // bl: all "execute" methods must be POST requests now.  this is because we are filtering out "!execute"
            // from redirect URLs in NetworkServletRedirectResult.
            assert !(methodProperties.methodType == HttpMethodType.GET && actionMethod.contains(POST_ONLY_METHOD_NAME)) : "No longer allowing !execute* methods to be GET requests.  If you want to do a non-input get request, then start it with something other than execute.";

            if (methodDetails!=null && methodDetails.readOnly() != ReadOnly.UNSPECIFIED) {
                methodProperties.isReadOnly = methodDetails.readOnly() == ReadOnly.TRUE ? true : false;
            } else {
                // by default, all GET requests are read only
                // by default, POSTs are not read only.
                methodProperties.isReadOnly = IPUtil.isEqual(HttpMethodType.GET, methodProperties.methodType);
            }

            methodProperties.requestType = methodDetails!=null ? methodDetails.requestType() : HttpRequestType.STANDARD;

            ErrorResponseCode errorResponseCode = method.getAnnotation(ErrorResponseCode.class);
            if (errorResponseCode != null && !IPStringUtil.isEmpty(errorResponseCode.value())) {
                methodProperties.errorResponseCode = errorResponseCode.value();
            }

            BypassValidate bypassValidate = method.getAnnotation(BypassValidate.class);
            if (bypassValidate != null) {
                methodProperties.isBypassValidate = true;
            }

            UseInputForExceptions useInputForExceptions = method.getAnnotation(UseInputForExceptions.class);
            if (useInputForExceptions != null) {
                assert methodProperties.methodType.isPost() && !methodProperties.requestType.isAjax() : "If using UseInputForExceptions, the method must be a non-AJAX POST request!";
                methodProperties.resultTypeToUseForExceptions = useInputForExceptions.value();
            }

            if (methodDetails!=null && methodDetails.isSSLOnly()) {
                methodProperties.isSSLOnly = true;
            }

            if (methodDetails!=null && methodDetails.preventDoublePosting()) {
                methodProperties.isPreventDoublePosting = true;
            }

            methodNameToType.put(actionMethod, methodProperties);
        }

        return methodProperties;
    }

    private static final MethodProperties errorDefault = new MethodProperties();

    private static class MethodProperties {
        HttpMethodType methodType = HttpMethodType.UNSPECIFIED;
        HttpRequestType requestType = HttpRequestType.STANDARD;
        boolean isReadOnly = false;
        boolean isBypassValidate = false;
        String resultTypeToUseForExceptions = null;
        String errorResponseCode = null;
        boolean isSSLOnly = false;
        boolean isPreventDoublePosting = false;
    }
}
