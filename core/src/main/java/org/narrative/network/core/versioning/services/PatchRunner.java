package org.narrative.network.core.versioning.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.persistence.hibernate.DatabaseResources;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.cluster.setup.NetworkSetup;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.versioning.AppVersion;
import org.narrative.network.core.versioning.AppliedPatch;
import org.narrative.network.core.versioning.BootstrapPatch;
import org.narrative.network.core.versioning.DowntimePatch;
import org.narrative.network.core.versioning.NamedPatch;
import org.narrative.network.core.versioning.Patch;
import org.narrative.network.core.versioning.PatchCondition;
import org.narrative.network.core.versioning.PatchRunnerLock;
import org.narrative.network.core.versioning.RunForEveryPatchRunnerPatch;
import org.narrative.network.core.versioning.RunForNewVersionPatch;
import org.narrative.network.core.versioning.RunForRecordOnlyPatch;
import org.narrative.network.core.versioning.StandardPatch;
import org.narrative.network.core.versioning.dao.AppVersionDAO;
import org.narrative.network.core.versioning.impl.AfterServletStartupPatch;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.PartitionTask;
import org.narrative.network.shared.tasktypes.TaskIsolationLevel;
import org.narrative.network.shared.tasktypes.TaskOptions;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 20, 2006
 * Time: 3:17:42 PM
 * This class runs the patches that have not yet been applied
 */
public class PatchRunner extends GlobalTaskImpl<Boolean> {
    private static final NetworkLogger logger = new NetworkLogger(PatchRunner.class);

    private static final List<PatchInfo<BootstrapPatch>> bootstrapPatches = newLinkedList();
    private static final List<PatchInfo<Patch>> schemaChangePatches = newLinkedList();
    private static final List<PatchInfo<Patch>> afterSchemaChangePatches = newLinkedList();
    private static final List<PatchInfo<AfterServletStartupPatch>> afterServletStartupPatches = newLinkedList();
    private static final Set<String> patchesAdded = new HashSet<String>();

    private Map<PartitionType, Set<Partition>> partitionMap;
    private Map<Partition, Map<String, AppliedPatch>> partitionToPatchNameToAppliedPatch;
    private static final String DID_NOT_EXECUTE_REASON = "didNotExecuteReason";

    private static boolean isBootstrapPatching;
    private static final ThreadLocal<Boolean> IS_PATCHING = new ThreadLocal<>();

    public static boolean isBootstrapPatching() {
        return isBootstrapPatching;
    }

    public static boolean isPatching() {
        return IS_PATCHING.get() != null && IS_PATCHING.get();
    }

    public static void setPatching(boolean patching) {
        if (patching) {
            IS_PATCHING.set(true);
        } else {
            IS_PATCHING.remove();
        }
    }

    private static boolean isInitialized() {
        return PatchRegistry.isInitialized();
    }

    private static enum PatchType {
        SCHEMA_CHANGE,
        REGULAR
    }

    private final boolean inDowntime;
    private final boolean recordOnly;

    /**
     * Runs the registered patches
     *
     * @param inDowntime If (true) downtime patches will be run.  If (false) execution will stop when a downtime patch is hit
     */
    public PatchRunner(boolean inDowntime) {
        this(inDowntime, false);
    }

    /**
     * Runs the registered patches
     *
     * @param inDowntime If (true) downtime patches will be run.  If (false) execution will stop when a downtime patch is hit
     * @param recordOnly If (true) the patchs will not be executed, only run.  Good for initial install/
     */
    public PatchRunner(boolean inDowntime, boolean recordOnly) {
        this.inDowntime = inDowntime;
        this.recordOnly = recordOnly;
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("usage: PatchRunner [inDowntime]");
            return;
        }

        boolean inDowntime = args[0].equalsIgnoreCase("inDowntime");
        boolean recordOnly = args[0].equalsIgnoreCase("recordOnly");
        long startTime;
        try {
            NetworkSetup.doSetup();
            startTime = System.currentTimeMillis();

            PatchRegistry.init();

            //first run the bootstrap patches, but only if we aren't just recording (since that would do nothing)
            if (!recordOnly) {
                runBootstrapPatches();
            }

            //now run the normal patches
            TaskRunner.doRootGlobalTask(new PatchRunner(recordOnly || inDowntime, recordOnly));
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Failed running patch runner", e, true);
        } finally {
            IPUtil.onEndOfApp();
        }

        if (logger.isInfoEnabled()) {
            logger.info("PatchRunner complete! Total time: " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    private static <P extends Patch> void add(P patch, PatchType patchType, PatchCondition... patchConditions) {
        if (patchesAdded.contains(patch.getName())) {
            throw UnexpectedError.getRuntimeException("Patch registered twice. " + patch.getName());
        }
        PartitionType partitionType = patch.getPartitionType();
        if (patchType == PatchType.SCHEMA_CHANGE) {
            schemaChangePatches.add(new PatchInfo<>(partitionType, patch, patchConditions));

        } else if (patchType == PatchType.REGULAR) {
            if (patch instanceof AfterServletStartupPatch) {
                afterServletStartupPatches.add(new PatchInfo<>(partitionType, (AfterServletStartupPatch) patch, patchConditions));
            } else {
                afterSchemaChangePatches.add(new PatchInfo<>(partitionType, patch, patchConditions));
            }

        } else {
            throw UnexpectedError.getRuntimeException("Unsupported PatchType: " + patchType);
        }

        patchesAdded.add(patch.getName());
    }

    /**
     * Adds a patch which will be run before any other patches.  These are not recorded in the database as having been
     * run since the point of these patches is to set up parts of the database which require bootstrapping.  Therefore
     * these will be run every time, and must be resilliant to that.
     *
     * @param bootstrapPatch the bootstrap patch to run at the start of the patch runner
     */
    public static void addStartBootstrapPatch(BootstrapPatch bootstrapPatch, PatchCondition... patchConditions) {
        bootstrapPatches.add(new PatchInfo<BootstrapPatch>(null, bootstrapPatch, patchConditions));
    }

    public static void addStartBootstrapPatch(BootstrapPatch bootstrapPatch) {
        addStartBootstrapPatch(bootstrapPatch, PatchCondition.getDefault());
    }

    /**
     * Adds a patch which requires downtime.
     *
     * @param patch
     */
    static void addSchemaChangeDowntimePatch(DowntimePatch patch) {
        add(patch, PatchType.SCHEMA_CHANGE, PatchCondition.getDefault());
    }

    static void addDowntimePatch(DowntimePatch patch) {
        add(patch, PatchType.REGULAR, PatchCondition.getDefault());
    }

    static void addSchemaChangeDowntimePatch(DowntimePatch patch, PatchCondition... patchConditions) {
        add(patch, PatchType.SCHEMA_CHANGE, patchConditions);
    }

    static void addDowntimePatch(DowntimePatch patch, PatchCondition... patchConditions) {
        add(patch, PatchType.REGULAR, patchConditions);
    }

    /**
     * Adds a patch which does not require downtime.
     *
     * @param patch
     */
    static void addSchemaChangePatch(StandardPatch patch) {
        add(patch, PatchType.SCHEMA_CHANGE, PatchCondition.getDefault());
    }

    static void addPatch(StandardPatch patch) {
        add(patch, PatchType.REGULAR, PatchCondition.getDefault());
    }

    static void addSchemaChangePatch(StandardPatch patch, PatchCondition... patchConditions) {
        add(patch, PatchType.SCHEMA_CHANGE, patchConditions);
    }

    static void addPatch(StandardPatch patch, PatchCondition... patchConditions) {
        add(patch, PatchType.REGULAR, patchConditions);
    }

    protected Boolean doMonitoredTask() {
        assert isInitialized() : "Should only run the PatchRunner AFTER initialization!";

        // new version so we need to acquire a cluster wide lock to do the patches
        PatchRunnerLock.dao().acquirePatchRunnerLock();

        // bl: once we have acquired the lock, we need to check the db again to see if there is actually any work
        // to perform.  a separate servlet may have started up concurrently and ran the patches.  in that case,
        // the local servlet will only acquire the lock after the other servlet has completed the patch run.
        if (AppVersionDAO.isUpToDate(NetworkRegistry.getInstance().getVersionStringForPatches())) {
            return true;
        }

        // bl: save a new app version since we are just now starting up.
        // nb. do in a separate transaction so that it is visible to all other connections immediately.
        // we will use the existence of this record to indicate that patches are currently running.
        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
            @Override
            protected Object doMonitoredTask() {
                AppVersion lastVersion = AppVersion.dao().getLastVersion();
                // bl: don't try to insert the AppVersion if there already is a record in place.  this could happen
                // if you try to restart the same version after the patch runner had failed when running on that version.
                if (exists(lastVersion) && lastVersion.getCompleteDatetime() == null && isEqual(lastVersion.getVersion(), NetworkRegistry.getInstance().getVersionStringForPatches())) {
                    return null;
                }
                AppVersion.dao().save(new AppVersion(NetworkRegistry.getInstance().getVersionStringForPatches()));
                return null;
            }
        });

        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
            protected Object doMonitoredTask() {
                partitionMap = Partition.dao().getAllByTypeMap();
                partitionToPatchNameToAppliedPatch = AppliedPatch.dao().getAllPartitionToPatchNameToAppliedPatches();
                return null;
            }
        });

        {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -180);
            Timestamp dateToPruneBefore = new Timestamp(cal.getTimeInMillis());
            final Collection<OID> appliedPatchOidsToDelete = new HashSet<OID>();
            // first, find out which applied
            for (Map<String, AppliedPatch> map : partitionToPatchNameToAppliedPatch.values()) {
                Iterator<AppliedPatch> iter = map.values().iterator();
                while (iter.hasNext()) {
                    AppliedPatch appliedPatch = iter.next();
                    if (!patchesAdded.contains(appliedPatch.getName())) {
                        // bl: only prune out applied patches that either have not completed or completed
                        // over 6 months ago.  this way, we don't accidentally get all applied patches
                        // deleted when switching between different branches that have different patch sets.
                        if (appliedPatch.getCompleteDatetime() == null || appliedPatch.getCompleteDatetime().before(dateToPruneBefore)) {
                            // remove this AppliedPatch from the map
                            iter.remove();
                            // add it to the list of keys to delete
                            appliedPatchOidsToDelete.add(appliedPatch.getOid());
                        }
                    }
                }
            }

            // bl: remove any AppliedPatch records that are no longer needed (i.e. those that have
            // been removed from the PatchRegistry).
            if (!appliedPatchOidsToDelete.isEmpty()) {
                TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                    protected Object doMonitoredTask() {
                        Collection<AppliedPatch> appliedPatchesToDelete = AppliedPatch.dao().getObjectsFromIDs(appliedPatchOidsToDelete);
                        for (AppliedPatch appliedPatch : appliedPatchesToDelete) {
                            AppliedPatch.dao().delete(appliedPatch);
                        }
                        return null;
                    }
                });
            }
        }

        if (!applyPatches(schemaChangePatches)) {
            return false;
        }
        if (!applyPatches(afterSchemaChangePatches)) {
            return false;
        }
        if (!applyPatches(afterServletStartupPatches)) {
            return false;
        }

        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
            protected Object doMonitoredTask() {
                //all patches ran ok, so lets add a new app version record to record the fact
                AppVersion appVersion = AppVersion.dao().getByVersion(NetworkRegistry.getInstance().getVersionStringForPatches());
                assert exists(appVersion) : "Should always have an AppVersion record after having installed patches!";
                assert appVersion.getCompleteDatetime() == null : "Current AppVersion should never have had an install datetime at this point!";
                appVersion.setCompleteDatetime(new Timestamp(System.currentTimeMillis()));
                return null;
            }
        });

        return true;
    }

    private boolean applyPatches(List<? extends PatchInfo<? extends Patch>> patches) {
        outer:
        for (final PatchInfo<? extends Patch> patchInfo : patches) {
            PartitionType partitionType = patchInfo.getPartitionType();
            final Patch patch = patchInfo.getPatch();
            Set<Partition> partitions = partitionMap.get(partitionType);
            for (final Partition partition : partitions) {
                Map<String, AppliedPatch> patchNameToAppliedPatch = partitionToPatchNameToAppliedPatch.get(partition);
                AppliedPatch appliedPatch = patchNameToAppliedPatch != null ? patchNameToAppliedPatch.get(patch.getName()) : null;

                //get the applied patch data
                final OID appliedPatchOid;
                final Properties data;
                final String lastVersion;
                if (exists(appliedPatch)) {
                    appliedPatchOid = appliedPatch.getOid();
                    lastVersion = appliedPatch.getVersion();
                    data = appliedPatch.getData();
                } else {
                    appliedPatchOid = null;
                    lastVersion = null;
                    data = new Properties();
                }

                boolean isComplete = appliedPatchOid != null && appliedPatch.isComplete();
                // it is partially complete if there is an AppliedPatch record, but it is not a complete patch.
                boolean isPartiallyComplete = appliedPatchOid != null && !isComplete;
                final long patchStartTime = System.currentTimeMillis();
                if (patchInfo.shouldPatchRun(partition, data, isComplete, lastVersion, isPartiallyComplete)) {
                    if (patch instanceof DowntimePatch && !inDowntime) {
                        logger.error(getLogPrefix(patch, partition, "Patch requires downtime, but downtime not specified."));
                        return false;
                    }

                    // apply each patch in their own session so that they are individually atomic operations.
                    try {
                        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                            protected Object doMonitoredTask() {
                                RuntimeException throwable = null;
                                try {
                                    if (logger.isInfoEnabled()) {
                                        logger.info(getLogPrefix(patch, partition, "Applying Patch"));
                                    }
                                    runPatch(patch, partition, data);
                                } catch (RuntimeException t) {
                                    throwable = t;
                                }
                                logAppliedPatch(appliedPatchOid, patch, partition, throwable, data, patchStartTime, false);
                                if (throwable != null) {
                                    throw throwable;
                                }
                                return null;
                            }
                        });
                    } finally {
                        // bl: call onEndOfThread after each patch is run.  this will clean up DatabaseResources
                        // and do any other end of thread operations.
                        IPUtil.onEndOfThread();
                    }

                    //the patch didn't satisfy the patch condition, but it also has never been recorded so we need to record it
                    //as having been run in non-execute mode
                } else if (!isComplete) {
                    data.setProperty(DID_NOT_EXECUTE_REASON, "patchCondition: " + patchInfo.getNoRunDescription());
                    if (logger.isInfoEnabled()) {
                        logger.info(getLogPrefix(patch, partition, "skipped due to condition: " + patchInfo.getNoRunDescription()));
                    }
                    logAppliedPatch(appliedPatchOid, patch, partition, null, data, patchStartTime, true);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(getLogPrefix(patch, partition, "skipped as already complete"));
                    }
                }
            }
        }
        return true;
    }

    public static void runBootstrapPatches() {
        assert isInitialized() : "Should only run bootstrap patches AFTER initialization!";
        // figure out if there are even any bootstrap patches to do. bail out early if there's nothing to do.
        if (bootstrapPatches.isEmpty()) {
            return;
        }
        //all servlets should always acquire a lock here.  They need to have the lock to attempt the bootstrap patches,
        //and they need to block if any other server is in the process of patching
        isBootstrapPatching = true;
        try {
            DatabaseResources dr = PartitionType.GLOBAL.getSingletonPartition().getBootstrapDatabaseResources();
            {
                ResultSet rs = null;
                try {
                    rs = dr.getResultSet("select oid from PatchRunnerLock where oid = ? for update", PatchRunnerLock.LOCK_OID);
                    if (!rs.next()) {
                        throw UnexpectedError.getRuntimeException("Should always get a result from the PatchRunnerLock table! If LOCK_OID doesn't exist, then something is wrong!");
                    }
                } finally {
                    PersistenceUtil.close(rs);
                }
            }
            runBootstrapPatches(bootstrapPatches);
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Unable to get PatchRunnerLock for executing bootstrap patches!", e);
        } finally {
            isBootstrapPatching = false;
            // bl: call onEndOfThread to cleanup the bootstrap DatabaseResources
            IPUtil.onEndOfThread();
        }
    }

    private static void runBootstrapPatches(Collection<PatchInfo<BootstrapPatch>> bootstrapPatches) {
        for (PatchInfo<BootstrapPatch> bootstrapPatch : bootstrapPatches) {
            long patchStartTime = System.currentTimeMillis();
            runBootstrapPatch(bootstrapPatch);
            long patchStopTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info("BOOTSTRAP PATCH: " + bootstrapPatch.getPatch().getName() + " - *******************************************");
            }
            if (logger.isInfoEnabled()) {
                logger.info("BOOTSTRAP PATCH: " + bootstrapPatch.getPatch().getName() + " - * Patch applied in " + (patchStopTime - patchStartTime) + "ms");
            }
            if (logger.isInfoEnabled()) {
                logger.info("BOOTSTRAP PATCH: " + bootstrapPatch.getPatch().getName() + " - *******************************************");
            }
        }
    }

    public static void logAppliedPatch(final OID appliedPatchOid, final Patch patch, final Partition partition, final Throwable t, final Properties data, long patchStartTime, boolean markCompleteIfAfterServletStartupPatch) {
        // bl: had issues sometimes recording exceptions in the AppliedPatch table (due to exceeding the errorText column size).
        // in order to avoid issues with that, let's log any patch errors up front.  otherwise, if the AppliedPatch
        // record can't be updated in the database for some reason, the underlying exception is going to be swallowed
        // and you'll never know what the real cause of the issue was.
        if (t != null) {
            logger.error(getLogPrefix(patch, partition, "!!!!!!!! Error applying patch. Quitting."), t);
        }

        final long currentPatchRunTime = System.currentTimeMillis() - patchStartTime;
        long patchRunTime = TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Long>() {
            protected Long doMonitoredTask() {
                AppliedPatch appliedPatch = AppliedPatch.dao().get(appliedPatchOid);
                boolean isNew = !exists(appliedPatch);
                long patchRunTime = currentPatchRunTime;
                if (isNew) {
                    appliedPatch = new AppliedPatch(patch, Partition.dao().get(partition.getOid()));
                } else {
                    // bl: aggregate the run time
                    patchRunTime += appliedPatch.getRunTimeMs();
                }
                if (t != null) {
                    appliedPatch.markFailedWithError(t);
                } else if (markCompleteIfAfterServletStartupPatch || !(patch instanceof AfterServletStartupPatch)) {
                    appliedPatch.markComplete();
                }
                appliedPatch.setData(data);
                appliedPatch.setRunTimeMs(patchRunTime);
                if (isNew) {
                    AppliedPatch.dao().save(appliedPatch);
                }
                return patchRunTime;
            }
        });

        // once we've successfully logged the successful AppliedPatch, then output to the logs
        if (t == null) {
            if (logger.isInfoEnabled()) {
                logger.info(getLogPrefix(patch, partition, "*******************************************"));
            }
            if (logger.isInfoEnabled()) {
                logger.info(getLogPrefix(patch, partition, "* Patch applied in " + patchRunTime + "ms" + (currentPatchRunTime != patchRunTime ? (" (" + currentPatchRunTime + "ms" + " this run)") : "")));
            }
            if (logger.isInfoEnabled()) {
                logger.info(getLogPrefix(patch, partition, "*******************************************"));
            }
        }
    }

    private static void runBootstrapPatch(final PatchInfo<BootstrapPatch> patchInfo) {
        // run the bootstrap patches in their own session
        try {
            BootstrapPatch patch = patchInfo.getPatch();
            Partition partition = PartitionType.GLOBAL.getSingletonPartition();
            if (patchInfo.shouldPatchRun(partition, new Properties(), false, null, false)) {
                patch.applyPatch();
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Bootstap Patch '" + patch.getName() + "' skipped due to condition: " + patchInfo.getNoRunDescription());
                }
            }
        } finally {
            // bl: in case the patch fails, call onEndOfThread to clear the thread in error flag.
            // bl: onEndOfThread will also cleanup the bootstrap DatabaseResources
            IPUtil.onEndOfThread();
        }
    }

    private void runPatch(final Patch patch, final Partition partition, final Properties data) {
        if (!recordOnly || patch instanceof RunForRecordOnlyPatch) {
            try {
                TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                    protected Object doMonitoredTask() {
                        partition.getPartitionType().doTask(partition, new TaskOptions(TaskIsolationLevel.NOT_ISOLATED), new PartitionTask<Object>() {
                            protected Object doMonitoredTask() {
                                // bl: we don't ever want recipes to process during patching. handled via the patching ThreadLocal.
                                setPatching(true);
                                try {
                                    patch.applyPatch(partition, data);
                                } finally {
                                    setPatching(false);
                                }
                                return null;
                            }
                        });
                        return null;
                    }
                });
            } finally {
                // bl: in case the patch fails, call onEndOfThread to clear the thread in error flag.
                IPUtil.onEndOfThread();
            }
        } else {
            data.setProperty(DID_NOT_EXECUTE_REASON, "recordOnly");
        }
    }

    private static String getLogPrefix(Patch patch, Partition part, String message) {
        return "PATCH: " + patch.getName() + ":" + part.getPartitionType() + ":" + part.getServer() + ":" + part.getDatabaseName() + " - " + message;
    }

    private static class PatchInfo<P extends NamedPatch> {
        private PatchCondition[] patchConditions;
        private PartitionType partitionType;
        private P patch;
        private String noRunDescription;

        private PatchInfo(PartitionType partitionType, P patch, PatchCondition... patchConditions) {
            this.partitionType = partitionType;
            this.patch = patch;
            this.patchConditions = patchConditions;
        }

        public PartitionType getPartitionType() {
            return partitionType;
        }

        public P getPatch() {
            return patch;
        }

        public String getNoRunDescription() {
            assert !isEmpty(noRunDescription) : "Should never attempt to get the noRunDescription unless it has already been set!";
            return noRunDescription;
        }

        public boolean shouldPatchRun(Partition partition, Properties data, boolean isPatchComplete, String lastVersion, boolean isPartiallyComplete) {
            // bl: if the patch is complete, let's check if we need to run it again if it's supposed to run on every new version
            if (isPatchComplete) {
                // bl: some patches such as InitDisplayResources must run every time the PatchRunner runs. in those cases, even if it's
                // already run on this version, we want it to run again since we are running patches.
                if (!(patch instanceof RunForEveryPatchRunnerPatch)) {
                    // if this isn't a patch that is supposed to always run on new versions, then we don't need to run it again!
                    if (!(patch instanceof RunForNewVersionPatch)) {
                        return false;
                    }
                    // bl: at this point, we know this patch should run once for every version. so let's check if it's a new version.
                    // if this isn't a new version, then we don't need to run it again
                    if (isEqual(NetworkRegistry.getInstance().getVersionStringForPatches(), lastVersion)) {
                        return false;
                    }
                    // bl: if this is a new version, then let's go ahead and continue below so we can check if the patch conditions are still true.
                }
            } else if (isPartiallyComplete) {
                // bl: if the patch is partially complete, that means it ran and failed previously. let's go ahead and try running it again.
                // e.g. this happens when a multi-named query patch fails in the middle, but a PatchCondition
                // on that named query patch would no longer be true.  we want to re-run the patch with the newly
                // (presumably) fixed queries.
                return true;
            }

            // bl: finally, let's check the PatchConditions
            boolean ret = true;
            StringBuilder sb = new StringBuilder();
            for (PatchCondition patchCondition : patchConditions) {
                if (!patchCondition.shouldPatchRun(patch, partition, data)) {
                    ret = false;
                    if (sb.length() > 0) {
                        sb.append(" && ");
                    }
                    sb.append(patchCondition.getDescription());
                }
            }
            if (!ret) {
                noRunDescription = sb.toString();
            }
            return ret;
        }

    }

}
