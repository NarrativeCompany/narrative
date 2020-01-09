package org.narrative.common.persistence;

import org.narrative.common.util.SyncObjectManager;
import org.narrative.common.util.Task;
import org.narrative.common.util.TaskInterface;
import org.narrative.network.core.cluster.partition.PartitionGroup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The purpose of this class is to allow commonly executed operations to be executed a single time
 * while allowing for multiple concurrent requests to share the result.
 * <p>
 * Example: 5 threads try to view a sidebar at the exact same time and the sidebar results have not yet been cached.
 * Also, let's assume that the process of generating the sidebar results is somewhat slow so that the first process
 * starts generating the results (presumably some kind of query) and the rest of the processes are waiting on
 * a synchronization.  Once the first process successfully finishes, one of the remaining 4 threads will now
 * obtain the sync lock and start executing the exact same process.  This serial ordering continues until all
 * 5 threads have separately generated their own results (using the exact same query).
 * <p>
 * As you can imagine, this is highly inefficient behavior and ultimately led to global connection pool exhaustion.
 * <p>
 * In the same scenario described above, if we use ThreadSafeFetchTask
 * <p>
 * Now, we can (and probably should) use this thread-safe task behavior on all of the sidebars.
 * <p>
 * Date: Sep 24, 2009
 * Time: 10:08:37 AM
 *
 * @author brian
 */
public class ThreadSafeFetchTask {

    private static final Map<ObjectPair<Class, Object>, SyncResultHolder<?>> syncObjectToSyncResultHolder = new ConcurrentHashMap<>();
    // bl: opting to just use a single sync object manager for all tasks with the task class as part of the key
    private static final SyncObjectManager<ObjectPair<Class, Object>> SYNC_OBJECT_MANAGER = new SyncObjectManager<>();

    public static <T> T doThreadSafeTask(Task<T> task) {
        return doThreadSafeTask(null, task);
    }

    public static <K, T> T doThreadSafeTask(K syncKey, TaskInterface<T> task) {
        final SyncResultHolder<T> syncResultHolder;
        ObjectPair<Class, Object> pair = new ObjectPair<>(task.getClass(), syncKey);
        synchronized (SYNC_OBJECT_MANAGER.getSyncObject(pair)) {
            SyncResultHolder<T> srh = (SyncResultHolder<T>)syncObjectToSyncResultHolder.get(pair);
            if (srh == null) {
                syncObjectToSyncResultHolder.put(pair, srh = new SyncResultHolder<T>());
            }
            syncResultHolder = srh;
        }

        // prior to acquiring the lock for the ThreadSafeFetchTask, we should make sure that we have initialized
        // all connections for the existing sessions. without this, a thread that acquires the lock to do the task
        // may block while waiting for a connection (if the pool is exhausted, for example), at which point
        // the entire server will lockup up irrevocably until it's forcefully killed.
        // note that there's still an outside chance of a deadlock issue if the underlying task being performed
        // here needs to acquire a connection from a different partition that does not yet have an open session
        // in the current PartitionGroup (e.g. for a composition partition). in most cases, the global and realm
        // partitions will already be in the PartitionGroup, so they will be initialized here, which should
        // solve the vast majority of deadlock/contention issues that we were seeing (particularly on db075).
        PartitionGroup.getCurrentPartitionGroup().initConnections();

        try {
            // if we can get the lock up front, we must be first.  in that case, just execute the runnable.
            if (syncResultHolder.lock.tryLock()) {
                // if we get straight in, then check if anybody else already built it
                if (syncResultHolder.result != null) {
                    assert syncObjectToSyncResultHolder.get(pair) != syncResultHolder : "SyncResultHolder should already have been removed from the map!";
                    return syncResultHolder.result;
                }
                try {
                    return doTask(syncResultHolder, pair, task);
                } finally {
                    synchronized (SYNC_OBJECT_MANAGER.getSyncObject(pair)) {
                        syncObjectToSyncResultHolder.remove(pair);
                    }
                }
            }
            syncResultHolder.lock.lock();
            return doTask(syncResultHolder, pair, task);
        } finally {
            if (syncResultHolder.lock.isHeldByCurrentThread()) {
                syncResultHolder.lock.unlock();
            }
        }
    }

    private static <T> T doTask(SyncResultHolder<T> syncResultHolder, ObjectPair<Class, Object> pair, TaskInterface<T> task) {
        if (syncResultHolder.result != null) {
            assert syncObjectToSyncResultHolder.get(pair) != syncResultHolder : "SyncResultHolder should already have been removed from the map!";
            return syncResultHolder.result;
        }
        syncResultHolder.result = task.doTask();

        return syncResultHolder.result;
    }

    private static class SyncResultHolder<T> {
        private final ReentrantLock lock = new ReentrantLock();
        private T result;
    }
}
