package org.narrative.network.customizations.narrative.interceptors;

import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Corollary to the original TransactionAndSessionInterceptor from the old Struts stack.
 *
 * Date: 9/11/18
 * Time: 12:48 PM
 *
 * @author brian
 */
@ControllerAdvice
public class SessionFlushResponseBodyAdvice implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // bl: we want this to run for all requests
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // bl: if there's no error, then let's flush the session to the db to ensure everything will save properly.
        // this can help raise possible flushing issues immediately, before the response body has been written.
        // bl: note that for read-only sessions, this is essentially a no-op.
        if(!PartitionGroup.getCurrentPartitionGroup().isInError()) {
            PartitionType.flushAllOpenSessionsForCurrentPartitionGroup();
        }

        return body;
    }
}
