package org.narrative.network.customizations.narrative.interceptors;

import org.narrative.common.util.IPHttpUtil;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Date: 8/17/18
 * Time: 8:45 AM
 *
 * @author brian
 */
public class ReadOnlyInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ReadOnly readOnlyAnn = handler instanceof HandlerMethod ? ((HandlerMethod)handler).getMethodAnnotation(ReadOnly.class) : null;
        boolean isReadOnly;
        if(IPHttpUtil.isGet(request)) {
            // bl: for get requests, default to read-only. only set it to read-only false if set as such via annotation
            isReadOnly = readOnlyAnn==null || readOnlyAnn.value();
        } else {
            // bl: for all other requests (POST/PUT/DELETE), only set to read-only if explicitly set via annotation.
            isReadOnly = readOnlyAnn!=null && readOnlyAnn.value();
        }

        // set the sessions as read only, if applicable
        if (isReadOnly) {
            PartitionGroup.getCurrentPartitionGroup().setReadOnly(true);
        }
        return super.preHandle(request, response, handler);
    }
}
