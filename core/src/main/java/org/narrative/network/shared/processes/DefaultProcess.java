package org.narrative.network.shared.processes;

import org.narrative.common.util.IPUtil;
import org.narrative.common.util.processes.GenericProcess;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.shared.util.NetworkLogger;

/**
 * Date: 5/26/11
 * Time: 7:40 AM
 *
 * @author brian
 */
public class DefaultProcess {

    private static final NetworkLogger logger = new NetworkLogger(DefaultProcess.class);

    public static void runProcess(String processName, Runnable runnable) {
        try {
            ProcessManager.getInstance().pushProcess(new GenericProcess(processName));
            try {
                runnable.run();
            } finally {
                ProcessManager.getInstance().popProcess();
            }
        } catch (Throwable t) {
            StatisticManager.recordException(t, false, null);
            logger.error("Failed running default process " + processName + " for " + IPUtil.getClassSimpleName(runnable.getClass()), t);
        }
    }
}
