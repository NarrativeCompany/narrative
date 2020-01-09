package org.narrative.network.shared.interceptors;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.shared.authentication.UserSession;
import org.narrative.network.shared.baseactions.NetworkAction;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.PreResultListener;

/**
 * Date: Jan 13, 2006
 * Time: 9:28:00 AM
 *
 * @author Brian
 */
public class TransactionAndSessionInterceptor extends NetworkStrutsInterceptorBase {
    protected String networkIntercept(ActionInvocation actionInvocation) throws Exception {
        actionInvocation.addPreResultListener(ActiveSessionFlusher.INSTANCE);

        boolean threadInError = false;
        String ret;
        try {
            ret = actionInvocation.invoke();
            // if the action has any validation errors, then we need to rollback the transactions.
            if (getNetworkAction().hasErrors()) {
                threadInError = true;
            }
        } catch (Throwable e) {
            // rollback the open transactions in the event of an exception
            threadInError = true;
            if (e instanceof Exception) {
                throw (Exception) e;
            } else {
                throw UnexpectedError.getRuntimeException("ERROR", e, true);
            }
        } finally {
            // if we're rolling back transactions, then mark this thread as being in error.
            // by setting the thread in error, we can/will prevent various things from happening
            // such as end of thread runnables (e.g. jgroups messages), temporary end of thread runnables,
            // and also rollback any active sessions in PartitionType.doTask().
            if (threadInError) {
                PartitionGroup.setCurrentPartitionGroupInError(true);
            }
        }

        return ret;
    }

    private static class ActiveSessionFlusher implements PreResultListener {
        private static final ActiveSessionFlusher INSTANCE = new ActiveSessionFlusher();

        public void beforeResult(ActionInvocation invocation, String resultCode) {
            // prior to determining the result, we need to flush all of the active sessions
            // in order to realize any errors prior to executing the result for an action.
            // in many cases, this won't do much since the session(s) will be read only.
            // bl: only flush the sessions if the thread isn't in error.  if the thread _is_
            // in error, then the active current sessions will be rolled back automatically
            // in PartitionType.doTask().  all "open" sessions will be rolled back in
            // the interceptor execution above.
            // same logic as we do when flushing sessions in PartitionType.doTask().
            // adding this check will prevent the ugly plain white error pages from
            // loading.
            // bl: if the Action has errors, then we will eventually mark the PartitionGroup
            // as being in error, so in that case, there is no point to flushing the sessions
            // since the transaction is just going to be rolled back ultimately anyway.
            // nb. necessary to do the hasErrors check here due to the order of operation of events
            // in the interceptor above. we only mark the PartitionGroup as being in error after
            // the invocation has completed (and thus the PreResultListeners have already been run.
            if (!PartitionGroup.isCurrentPartitionGroupInError() && !((NetworkAction) invocation.getAction()).hasErrors()) {
                PartitionType.flushAllOpenSessionsForCurrentPartitionGroup();
            }

            // bl: let's also set the UserSession on the HttpSession to make sure that it gets saved/flushed to Redis.
            // Spring Session + Redis was only updating session information on the initial save, not on subsequent request updates otherwise.
            // bl: specifically, the problem is that RedisOperationsSessionRepository.save() only detects the "delta"
            // of changes, which does NOT include inspecting existing session attributes for any changes (which kinda makes sense).
            UserSession userSession = UserSession.getUserSessionFromHttpSession();
            // bl: only do it if the UserSession is currently set on the HttpSession already. this will bypass when
            // there is not yet a session or when we're using a ThreadLocal session
            if(userSession!=null) {
                UserSession.updateCurrentSessionOnHttpSession(userSession);
            }
        }
    }
}
