package org.narrative.network.shared.interceptors;

import com.opensymphony.xwork2.ActionContext;

import java.util.Collections;
import java.util.Map;

/**
 * Date: Feb 8, 2006
 * Time: 9:48:49 AM
 *
 * @author Brian
 */
public class NetworkPostParametersInterceptor extends NetworkBaseParametersInterceptor {

    @Override
    protected Map<String, Object> retrieveParameters(ActionContext context) {
        Map<String, Object> postPrepareParamMap = NetworkPreParametersInterceptor.getPostParametersMap(context);

        if (postPrepareParamMap != null && !postPrepareParamMap.isEmpty()) {
            return postPrepareParamMap;
        }
        return Collections.emptyMap();
    }
}
