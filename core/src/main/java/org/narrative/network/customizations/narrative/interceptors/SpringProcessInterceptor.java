package org.narrative.network.customizations.narrative.interceptors;

import org.narrative.common.util.processes.ProcessManager;
import org.narrative.common.util.processes.SpringProcess;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 9/18/18
 * Time: 10:07 AM
 *
 * @author brian
 */
public class SpringProcessInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestName;
        if(handler instanceof HandlerMethod) {
            Method method = ((HandlerMethod)handler).getMethod();
            requestName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        } else {
            requestName = request.getServletPath() + "[" + request.getMethod() + "]";
        }

        SpringProcess springProcess = new SpringProcess(requestName, request);

        // bl: changing so that we always now include the original thread name as part of the name
        // so that we can associate the debug lines after changing the name
        String threadName = requestName + "-" + ProcessManager.getInstance().getCurrentProcess().getProcessOid();
        Thread.currentThread().setName(threadName);
        ProcessManager.getInstance().pushProcess(springProcess);

        // bl: set the Spring multipart request so we have a copy for multipart logging purposes
        if(request instanceof MultipartHttpServletRequest) {
            networkContext().getReqResp().setSpringMultipartRequest((MultipartHttpServletRequest)request);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ProcessManager.getInstance().popProcess();
    }
}
