package org.narrative.common.util.processes;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.IPUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 15, 2005
 * Time: 11:16:52 AM
 */
public class GenericProcess {

    private final OID processOid;
    private final long startTimeNanos;
    protected final long startTimeMS;
    protected final Thread processThread;
    protected final String name;
    private Long ownStartNanos;
    private long durationNanos = 0L;

    private String statusMessage;

    private GenericProcess parentProcess;
    private GenericProcess childProcess;

    public GenericProcess() {
        this("");
    }

    public GenericProcess(String name) {
        this(name, null);
    }

    public GenericProcess(String name, Thread thread) {
        processOid = OIDGenerator.isInitialized() ? OIDGenerator.getNextOID() : CoreUtils.seqOid();
        this.name = name;
        startTimeNanos = System.nanoTime();
        ownStartNanos = startTimeNanos;
        startTimeMS = System.currentTimeMillis();
        processThread = thread == null ? Thread.currentThread() : thread;
    }

    public OID getProcessOid() {
        return processOid;
    }

    public final double getTotalRunningTime() {
        return (System.nanoTime() - startTimeNanos) / 1000000;
    }

    public final double getOwnRunningTime() {
        if (ownStartNanos != null) {
            addOwnDuration();
        }
        return durationNanos / 1000000;
    }

    public long getStartTimeMS() {
        return startTimeMS;
    }

    public Long getThreadId() {
        return processThread.getId();
    }

    public String getStatus() {
        return processThread.getState().toString();
    }

    public Thread getProcessThread() {
        return processThread;
    }

    public String getType() {
        return IPUtil.getClassSimpleName(this.getClass());
    }

    public String getName() {
        return name;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void updateStatusMessageAndLog(NarrativeLogger logger, String statusMessage) {
        setStatusMessage(statusMessage);
        if (logger.isInfoEnabled()) {
            logger.info(statusMessage);
        }
    }

    public String getAreaName() {
        return "{none}";
    }

    public String getOwner() {
        return "{system}";
    }

    public String getInfo() {
        return null;
    }

    public GenericHistory getHistory() {
        return new GenericHistory(this);
    }

    public boolean isOutlier() {
        return getHistory().isOutlier(getTotalRunningTime());
    }

    public boolean isRootProcess() {
        return parentProcess == null;
    }

    public GenericProcess getParentProcess() {
        return parentProcess;
    }

    void setParentProcess(GenericProcess parentProcess) {
        this.parentProcess = parentProcess;
    }

    public GenericProcess getRootProcess() {
        if (parentProcess != null) {
            return parentProcess.getRootProcess();
        }
        return this;
    }

    public GenericProcess getChildProcess() {
        return childProcess;
    }

    void setChildProcess(GenericProcess childProcess) {
        if (childProcess != null && this.childProcess == null) {
            // setting a childProcess? then make sure we stop counting
            addOwnDuration();
        } else if (childProcess == null && this.childProcess != null) {
            // setting the childProcess to null?  then make sure we start counting again!
            assert ownStartNanos == null : "If childProcess is not null, then runStartNanos must be null!";
            ownStartNanos = System.nanoTime();
        }
        this.childProcess = childProcess;
    }

    private void addOwnDuration() {
        assert ownStartNanos != null : "runStartNanos must not be null when adding duration and resetting!";
        durationNanos += (System.nanoTime() - ownStartNanos);
        ownStartNanos = null;
    }

    public List<GenericProcess> getProcessList() {
        List<GenericProcess> ret = new LinkedList<GenericProcess>();
        ret.add(this);
        GenericProcess childProcess = getChildProcess();
        while (childProcess != null) {
            ret.add(childProcess);
            childProcess = childProcess.getChildProcess();
        }
        return ret;
    }

}
