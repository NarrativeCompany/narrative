package org.narrative.network.core.cluster.actions.server;

import org.narrative.common.util.processes.RequestProcessHistory;
import org.narrative.network.core.cluster.actions.ClusterAction;

import java.util.List;

/**
 * Date: Dec 2, 2008
 * Time: 5:29:39 PM
 *
 * @author brian
 */
public class RecentProcessOutliersAjaxAction extends ClusterAction {

    private String processName;
    private List<RequestProcessHistory.SlowRequestDetails> recentSlowActionInfo;

    @Override
    public String input() throws Exception {
        recentSlowActionInfo = RequestProcessHistory.getRecentSlowActionInfo(processName);
        return INPUT;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public List<RequestProcessHistory.SlowRequestDetails> getRecentSlowActionInfo() {
        return recentSlowActionInfo;
    }
}
