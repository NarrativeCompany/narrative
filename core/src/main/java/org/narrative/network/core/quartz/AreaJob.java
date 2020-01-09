package org.narrative.network.core.quartz;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.processes.GenericProcess;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.shared.context.AreaContext;
import org.narrative.network.shared.context.AreaContextHolder;
import org.narrative.network.shared.processes.AreaTaskProcess;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

/**
 * User: barry
 * Date: Mar 4, 2010
 * Time: 3:45:55 PM
 */
public abstract class AreaJob extends NetworkJob implements AreaContextHolder {
    public static final String AREA_OID = "areaOid";

    private static final NetworkLogger logger = new NetworkLogger(AreaJob.class);

    private AreaTaskProcess process;

    protected AreaJob() {}

    protected AreaJob(boolean isForceWritable) {
        super(isForceWritable);
    }

    @Override
    protected GenericProcess createProcess() {
        // bl: return null here so that we can create the process directly in this class instead of in NetworkJob.
        // this will prevent needlessly adding a secondary layer to the job stack.
        return null;
    }

    protected abstract void executeAreaJob(JobExecutionContext context) throws JobExecutionException;

    protected Area getArea(JobExecutionContext context) {
        return Area.dao().get(getOidFromContext(context, AREA_OID));
    }

    @Override
    protected final void executeJob(final JobExecutionContext context) throws JobExecutionException {
        Area area = getArea(context);
        AreaTaskImpl<Object> task = new AreaTaskImpl<Object>(isForceWritable()) {
            @Override
            protected Object doMonitoredTask() {
                try {
                    AreaJob.this.networkContext = getAreaContext();
                    AreaJob.this.process = new AreaTaskProcess(getAreaContext(), AreaJob.this.getNetworkContext(), AreaJob.this.getMonitoredClassName());
                    ProcessManager.getInstance().pushProcess(AreaJob.this.process);
                    try {
                        executeAreaJob(context);
                    } finally {
                        ProcessManager.getInstance().popProcess();
                    }
                } catch (JobExecutionException jee) {
                    logger.warn("Quartz area job: " + context.getJobDetail().getKey().getName() + " Canceled.", jee);
                    return null;
                }
                return null;
            }
        };
        // bl: register this task so that we flush once the AreaTask has completed while the realm session is still in
        // scope to make sure the flush (and any event listeners) have access to the current realm partition, if necessary.
        PartitionGroup.getCurrentPartitionGroup().registerTaskForFlushingOnSuccess(task);
        getNetworkContext().doAreaTask(area, task);
    }

    public static void addAreaToJobDataMap(Area area, JobBuilder jobBuilder) {
        jobBuilder.usingJobData(AREA_OID, area.getOid().getValue());
    }

    public static void addAreaToJobDataMap(Area area, TriggerBuilder triggerBuilder) {
        triggerBuilder.usingJobData(AREA_OID, area.getOid().getValue());
    }

    public static OID getAreaOidFromTrigger(Trigger trigger) {
        return getOidFromJobDataMap(trigger.getJobDataMap(), AREA_OID);
    }

    public static OID getAreaOidFromJobDetails(JobDetail jobDetails) {
        return getOidFromJobDataMap(jobDetails.getJobDataMap(), AREA_OID);
    }

    public AreaContext getAreaContext() {
        return (AreaContext) getNetworkContext();
    }
}
