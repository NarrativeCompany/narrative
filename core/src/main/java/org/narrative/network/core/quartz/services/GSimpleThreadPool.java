package org.narrative.network.core.quartz.services;

import org.narrative.common.util.NamedThreadFactory;
import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: barry
 * Date: Mar 17, 2010
 * Time: 4:04:01 PM
 */
public class GSimpleThreadPool implements ThreadPool {

    private String instanceName;
    private String instanceId;
    private ExecutorService _executorService;

    @Override
    public void initialize() throws SchedulerConfigException {
        _executorService = Executors.newCachedThreadPool(new NamedThreadFactory(instanceName));
    }

    @Override
    public boolean runInThread(Runnable runnable) {
        return _executorService.submit(runnable) != null;
    }

    @Override
    public int blockForAvailableThreads() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void shutdown(boolean waitForJobsToComplete) {
        if (waitForJobsToComplete) {
            _executorService.shutdown();
        } else {
            _executorService.shutdownNow();
        }
    }

    @Override
    public int getPoolSize() {
        return -1;
    }

    @Override
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
