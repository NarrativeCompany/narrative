package org.narrative.network.core.cluster.actions.server;

import org.narrative.common.util.processes.GenericHistory;
import org.narrative.common.util.processes.GenericProcess;
import org.narrative.common.util.processes.HistoryManager;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.network.core.cluster.actions.ServerStatusTask;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: May 16, 2006
 * Time: 4:15:19 PM
 */
public class ProcessStatusAction extends SystemMonitoringAction {

    private Collection<GenericProcess> processList;
    private Collection<GenericHistory> allHistory;
    private final ServerStatusTask serverStatus = new ServerStatusTask();

    public String input() throws Exception {

        processList = ProcessManager.getInstance().getProcessSet();
        allHistory = HistoryManager.getInstance().getAllHistory();

        getNetworkContext().doGlobalTask(serverStatus);

        return INPUT;
    }

    public Collection<GenericProcess> getProcessList() {
        return processList;
    }

    public Collection<GenericHistory> getAllHistory() {
        return allHistory;
    }

    public ServerStatusTask getServerStatus() {
        return serverStatus;
    }
}
