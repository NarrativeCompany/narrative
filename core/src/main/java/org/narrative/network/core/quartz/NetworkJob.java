package org.narrative.network.core.quartz;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.common.util.processes.GenericProcess;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.context.NetworkContextHolder;
import org.narrative.network.shared.context.NetworkContextInternal;
import org.narrative.network.shared.processes.GlobalTaskProcess;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

/**
 * User: barry
 * Date: Mar 3, 2010
 * Time: 3:08:37 PM
 */
public abstract class NetworkJob implements Job, NetworkContextHolder {
    private static final NetworkLogger logger = new NetworkLogger(NetworkJob.class);
    private final boolean isForceWritable;
    private volatile boolean interrupt = false;
    private JobExecutionContext context = null;

    private GenericProcess process;

    protected NetworkContext networkContext;
    public static final String JOB_STATUS_MESSAGE = "jobStatusMessage";

    protected NetworkJob() {
        this(true);
    }

    protected NetworkJob(boolean isForceWritable) {
        this.isForceWritable = isForceWritable;
    }

    protected abstract void executeJob(JobExecutionContext context) throws JobExecutionException;

    protected GenericProcess createProcess() {
        return new GlobalTaskProcess(networkContext, getMonitoredClassName());
    }

    public GenericProcess getProcess() {
        return process;
    }

    protected Class getMonitoredClass() {
        return this.getClass();
    }

    protected String getMonitoredClassName() {
        // bl: for anonymous classes, let's display the $1 after the name.  thus, can't use any
        // standard method on Class.  instead, just strip everything off before the last '.' in the class name.
        return IPStringUtil.getStringAfterLastIndexOf(getMonitoredClass().getName(), ".");
    }

    protected final boolean isForceWritable() {
        return isForceWritable;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.context = context;
        long start = System.currentTimeMillis();
        String debugJobTrigger = "[" + context.getJobDetail().getKey() + " | " + context.getTrigger().getKey() + "]";
        setStatusMessage("Started job " + this.getClass().getSimpleName() + " " + debugJobTrigger);
        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>(isForceWritable()) {
            protected Object doMonitoredTask() {
                try {
                    NetworkJob.this.networkContext = getNetworkContext();
                    ((NetworkContextInternal) getNetworkContext()).setupSystemRole();
                    NetworkJob.this.process = NetworkJob.this.createProcess();
                    if (NetworkJob.this.process != null) {
                        ProcessManager.getInstance().pushProcess(NetworkJob.this.process);
                    }
                    try {
                        executeJob(NetworkJob.this.context);
                    } finally {
                        if (NetworkJob.this.process != null) {
                            ProcessManager.getInstance().popProcess();
                        }
                    }
                    return null;
                } catch (JobExecutionException jee) {
                    logger.warn("Quartz Network job: " + debugJobTrigger + " Canceled.", jee);
                    return null;
                } catch (Throwable e) {
                    StatisticManager.recordException(e, false, null);
                    throw UnexpectedError.getRuntimeException("Failed executing network quartz job: " + debugJobTrigger, e, true);
                }
            }
        });
        setStatusMessage(logger, "Finished job " + this.getClass().getSimpleName() + " " + debugJobTrigger + " in " + (System.currentTimeMillis() - start) + "ms");
    }

    public NetworkContext getNetworkContext() {
        return networkContext;
    }

    public static OID getOidFromContext(JobExecutionContext context, String key) {
        return getOidFromJobDataMap(context.getMergedJobDataMap(), key);
    }

    protected static OID getOidFromJobDataMap(JobDataMap jobDataMap, String key) {
        Object obj = jobDataMap.get(key);
        if (obj == null) {
            return null;
        }

        return OID.valueOf(jobDataMap.getLongValue(key));
    }

    public static Integer getIntegerValueJobDataMap(JobDataMap jobDataMap, String key) {
        Object obj = jobDataMap.get(key);
        if (obj == null) {
            return null;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        return jobDataMap.getIntValue(key);
    }

    public static <T extends IntegerEnum> T getIntegerEnumFromContext(JobExecutionContext context, Class<T> enumClass, String key) {
        return getIntegerEnumFromJobDataMap(context.getMergedJobDataMap(), enumClass, key);
    }

    public static <T extends IntegerEnum> T getIntegerEnumFromJobDataMap(JobDataMap jobDataMap, Class<T> enumClass, String key) {
        Integer id = getIntegerValueJobDataMap(jobDataMap, key);
        if (id == null) {
            return null;
        }

        return EnumRegistry.getForId(enumClass, id);
    }

    protected static Long getLongFromJobDataMap(JobDataMap jobDataMap, String key) {
        Object obj = jobDataMap.get(key);
        if (obj == null) {
            return null;
        }

        return jobDataMap.getLongValue(key);
    }

    protected static boolean isSettingEnabled(JobExecutionContext context, String settingName) {
        Object obj = context.getMergedJobDataMap().get(settingName);
        return obj != null && ((Boolean) obj);
    }

    //This is used by subclasses that are interruptable
    public void interrupt() throws UnableToInterruptJobException {
        interrupt = true;
    }

    public final void checkIsInterrupted() throws JobExecutionException {
        if (interrupt) {
            throw new JobExecutionException("Job Interrupted, will run again later.");
        }
    }

    public final boolean isInterrupted() {
        return interrupt;
    }

    private void setStatusMessage(String message) {
        if (context != null) {
            context.put(JOB_STATUS_MESSAGE, message);
        }
    }

    protected void setStatusMessage(NetworkLogger logger, String message) {
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
        setStatusMessage(message);
    }
}
