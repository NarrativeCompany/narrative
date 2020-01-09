package org.narrative.network.shared.tasktypes;

import org.narrative.network.shared.context.NetworkContext;

/**
 * Date: May 16, 2006
 * Time: 2:47:42 PM
 *
 * @author Brian
 */
public class TaskOptions {

    private TaskIsolationLevel taskIsolationLevel = TaskIsolationLevel.NOT_ISOLATED;
    private boolean isBypassErrorStatisticRecording = false;
    private NetworkContext networkContextToUse;

    public TaskOptions() {}

    public TaskOptions(TaskIsolationLevel taskIsolationLevel) {
        this.taskIsolationLevel = taskIsolationLevel;
    }

    TaskOptions(NetworkContext networkContextToUse) {
        this.networkContextToUse = networkContextToUse;
    }

    public TaskIsolationLevel getTaskIsolationLevel() {
        return taskIsolationLevel;
    }

    public boolean isBypassErrorStatisticRecording() {
        return isBypassErrorStatisticRecording;
    }

    void setBypassErrorStatisticRecording(boolean bypassErrorStatisticRecording) {
        isBypassErrorStatisticRecording = bypassErrorStatisticRecording;
    }

    public NetworkContext getNetworkContextToUse() {
        return networkContextToUse;
    }

    void setNetworkContextToUse(NetworkContext networkContextToUse) {
        this.networkContextToUse = networkContextToUse;
    }
}
