package org.narrative.network.core.system;

import org.narrative.common.util.IPUtil;
import org.narrative.common.util.NamedThreadFactory;
import org.narrative.network.shared.processes.DefaultProcess;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: barry
 * Date: 1/5/11
 * Time: 2:10 PM
 */
public enum ThreadBucketType implements ThreadBucket {
    UTILITY(5),
    // jw: this is a single thread bucket so that all after servlet startup patches will run serially.
    PATCH_RUNNER(1);

    ThreadPoolExecutor service;
    private static final NetworkLogger logger = new NetworkLogger(ThreadBucketType.class);

    ThreadBucketType(Integer maxThreads) {
        if (maxThreads != null) {
            setThreadPoolExecutor(getDefaultThreadPoolExecutor(maxThreads));
        }
    }

    static {
        for (ThreadBucketType threadBucketType : values()) {
            ThreadBucketManager.INSTANCE.addThreadBucket(threadBucketType);
        }
    }

    public void addRunnable(final Runnable runnable) {
        addRunnable(null, runnable);
    }

    public CompletableFuture<Void> addRunnable(final String procName, final Runnable runnable) {
        if (logger.isTraceEnabled()) {
            logger.trace("Adding " + IPUtil.getClassSimpleNameUnqualified(runnable.getClass()) + "-" + procName + " to " + this + " ThreadBucket");
        }
        return CompletableFuture.runAsync(() -> {
            if (logger.isTraceEnabled()) {
                logger.trace("Running " + IPUtil.getClassSimpleNameUnqualified(runnable.getClass()) + "-" + procName + " in " + ThreadBucketType.this + " ThreadBucket");
            }
            DefaultProcess.runProcess(ThreadBucketType.class.getSimpleName() + "-" + ThreadBucketType.this + "-" + IPUtil.getClassSimpleNameUnqualified(runnable.getClass()) + (procName == null ? "" : ("-" + procName)), runnable);
        }, getThreadPoolExecutor());
    }

    @Override
    public String getName() {
        return name();
    }

    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
        assert service == null : "Should only set a ThreadPoolExecutor when one hasn't already been set!";
        this.service = threadPoolExecutor;

        IPUtil.EndOfX.endOfApp.addRunnable("30ThreadManager/" + this.name(), () -> {
            service.shutdown();
            try {
                service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                logger.error("Thread Bucket Interrupted", e);
            }
        });
    }

    @Override
    public ThreadPoolExecutor getThreadPoolExecutor() {
        assert service != null : "Should never get the ThreadPoolExecutor before it has been set!";
        return service;
    }

    public ThreadPoolExecutor getDefaultThreadPoolExecutor(int maxThreads) {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads, getNamedThreadFactory());
    }

    public NamedThreadFactory getNamedThreadFactory() {
        return new NamedThreadFactory(this.name());
    }
}
