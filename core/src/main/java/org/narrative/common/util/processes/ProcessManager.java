/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 15, 2005
 * Time: 3:15:31 PM
 */
package org.narrative.common.util.processes;

import org.narrative.common.persistence.OID;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessManager {

    private static ProcessManager instance;
    private Map<OID, GenericProcess> processMap = new ConcurrentHashMap<OID, GenericProcess>();

    private static final ThreadLocal<GenericProcess> ACTIVE_PROCESS = new ThreadLocal<GenericProcess>();

    private static final Comparator<GenericProcess> GENERIC_PROCESS_COMPARATOR = new Comparator<GenericProcess>() {
        public int compare(GenericProcess o1, GenericProcess o2) {
            int ret = Long.valueOf(o1.getStartTimeMS()).compareTo(o2.getStartTimeMS());
            if (ret != 0) {
                return ret;
            }
            return OID.compareOids(o1.getProcessOid(), o2.getProcessOid());
        }
    };

    private ProcessManager() {}

    public static ProcessManager getInstance() {
        if (instance == null) {
            instance = new ProcessManager();
        }
        return instance;
    }

    public void pushProcess(GenericProcess process) {
        if (process == null) {
            return;
        }
        GenericProcess currentProcess = ACTIVE_PROCESS.get();
        process.setParentProcess(currentProcess);
        if (currentProcess == null) {
            processMap.put(process.getProcessOid(), process);
        } else {
            currentProcess.setChildProcess(process);
        }
        ACTIVE_PROCESS.set(process);
    }

    public void popProcess() {
        GenericProcess currentProcess = ACTIVE_PROCESS.get();
        assert currentProcess != null : "Attempted to pop a process off the stack, but there wasn't a current active process!";
        HistoryManager.getInstance().recordProcess(currentProcess);
        GenericProcess parentProcess = currentProcess.getParentProcess();
        if (parentProcess != null) {
            parentProcess.setChildProcess(null);
        } else {
            // remove the root process from the map since we're done
            processMap.remove(currentProcess.getProcessOid());
        }
        ACTIVE_PROCESS.set(parentProcess);
    }

    public GenericProcess getCurrentProcess() {
        return ACTIVE_PROCESS.get();
    }

    public Set<GenericProcess> getProcessSet() {
        Set<GenericProcess> ret = new TreeSet<GenericProcess>(GENERIC_PROCESS_COMPARATOR);
        ret.addAll(processMap.values());

        Iterator<GenericProcess> iter = ret.iterator();
        // bl: clean up any terminated process threads
        while (iter.hasNext()) {
            GenericProcess process = iter.next();
            if (process.getProcessThread().getState() == Thread.State.TERMINATED) {
                processMap.remove(process.getProcessOid());
                iter.remove();
            }
        }
        return ret;
    }

    public GenericProcess getProcess(OID id) {
        return processMap.get(id);
    }
}