package org.narrative.network.shared.interceptors;

import org.narrative.common.web.struts.MethodPropertiesUtil;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * Date: Mar 6, 2006
 * Time: 9:20:19 AM
 *
 * @author Brian
 */
public class ReadOnlyEnforcerInterceptor extends AbstractInterceptor {

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        boolean isReadOnly = MethodPropertiesUtil.isReadOnlyRequest(invocation);

        // set the sessions as read only, if applicable
        if (isReadOnly) {
            PartitionGroup.getCurrentPartitionGroup().setReadOnly(true);
        }
        return invocation.invoke();
    }
}
