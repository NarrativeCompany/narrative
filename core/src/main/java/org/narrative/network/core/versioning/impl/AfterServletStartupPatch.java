package org.narrative.network.core.versioning.impl;

import org.narrative.common.util.Debug;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.Timer;
import org.narrative.common.util.processes.GenericProcess;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.system.ThreadBucketType;
import org.narrative.network.core.versioning.services.PatchRunner;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.Properties;

/**
 * Date: 8/1/13
 * Time: 10:40 AM
 * User: jonmark
 */
public abstract class AfterServletStartupPatch extends StandardPatchImpl {

    private static final NetworkLogger logger = new NetworkLogger(AfterServletStartupPatch.class);

    private static boolean hasStartedPatches = false;

    public AfterServletStartupPatch() {
        super(PartitionType.GLOBAL);
    }

    public AfterServletStartupPatch(int iteration) {
        super(iteration, PartitionType.GLOBAL);
    }

    public AfterServletStartupPatch(String nameOverride) {
        super(nameOverride, PartitionType.GLOBAL);
    }

    /**
     * bl: all patches will be force writeable (since they are patching and thus likely making changes), but
     * there may be times that we want to run an after servlet startup patch that is run in a read-only task.
     * implementations of AfterServletStartupPatch can do so by overriding this method.
     *
     * @return true (the default) if the root GlobalTaskImpl that is run should be force writeable. false if not.
     */
    protected boolean isForceWritable() {
        return true;
    }

    protected abstract void applyPatch();

    @Override
    public void applyPatch(Partition partition, Properties data) {
        // bl: need to make sure we don't start it until after init has completed so that the Solr server URL is initialized.
        NetworkRegistry.getInstance().addEndOfInitRunnable("AfterServletStartupPatch:" + getName(), new Runnable() {
            @Override
            public void run() {
                if (!hasStartedPatches) {
                    ThreadBucketType.PATCH_RUNNER.addRunnable(() -> {
                        Timer timer = new Timer(logger);
                        timer.start("Waiting 60 seconds to start AfterServletStartupPatches");
                        // bl: let's just always delay the start of after servlet startup patches by one minute.
                        // handle that by doing a sleep in the PATCH_RUNNER thread bucket up front.
                        // bl: can't use Thread.sleep() or else this thread will yield to the next thread in the queue! whoops!
                        // wait will do the trick and allow this thread to block the ExecutorService for the specified
                        // amount of time (approximately).
                        try {
                            synchronized (this) {
                                wait(IPDateUtil.MINUTE_IN_MS);
                            }
                        } catch (InterruptedException e) {
                            // ignore, as this should never happen since nothing should notify this Runnable
                        }
                        timer.finish();
                    });
                    hasStartedPatches = true;
                }
                ThreadBucketType.PATCH_RUNNER.addRunnable(() -> {
                    final GenericProcess process = new GenericProcess(AfterServletStartupPatch.this.getName());
                    ProcessManager.getInstance().pushProcess(process);

                    Timer timer = new Timer(logger, process);
                    timer.start("Running AfterServletStartupPatch: " + AfterServletStartupPatch.this.getClass());
                    try {
                        // bl: we don't ever want recipes to process during patching. handled via the patching ThreadLocal.
                        //mk: originally this was inside of doRootGlobalTask, but because recipes processing happens as part of partition group runnables, we have to set it outside of the task runner.
                        PatchRunner.setPatching(true);
                        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>(isForceWritable()) {
                            @Override
                            protected Object doMonitoredTask() {
                                Throwable throwable = null;
                                final long patchStartTime = System.currentTimeMillis();
                                try {
                                    applyPatch();
                                } catch (Throwable t) {
                                    throwable = t;
                                    throw t;
                                } finally {
                                    //mk: this requires hibernate session so do in in scope of global task, since we are already running it here.
                                    PatchRunner.logAppliedPatch(getAppliedPatch().getOid(), AfterServletStartupPatch.this, getPartition(), throwable, data, patchStartTime, true);
                                }
                                return null;
                            }
                        });
                    } catch (Throwable t) {
                        try {
                            // need to send emails from the context of a PartitionGroup
                            TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                                @Override
                                protected Object doMonitoredTask() {
                                    NetworkRegistry.getInstance().sendDevOpsStatusEmail("AfterServletStartupPatching Failed", "Hello!\n\nAfterServletStartupPatch failed during execution of " + getName() + " patch.\n\nThere are " + ThreadBucketType.PATCH_RUNNER.getThreadPoolExecutor().getQueue().size() + " more patches queued which will not be executed.\n\n" + Debug.stackTraceFromException(t));
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            logger.error("Failed sending email about AfterServletStartupPatch failure.", t);
                        }
                        //mk: shutdown the ThreadBucket so consecutive patches are not executed
                        ThreadBucketType.PATCH_RUNNER.getThreadPoolExecutor().shutdownNow();
                        throw t;
                    } finally {
                        PatchRunner.setPatching(false);
                        timer.finish();
                        ProcessManager.getInstance().popProcess();
                    }
                });
            }
        });
    }
}
