package org.narrative.common.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * [b]A problem[/b]
 * I recently had to code an FTP transfer process.  Thousands of files were being
 * transfered.  Each of the files was quite small.  Even though I was on a T1 connection,
 * a 2K file was taking nearly a second to transfer.
 * <p>
 * The problem was a combination of network latency and the FTP protocol.  Each
 * request took time to establish itself.  Actually transferring the data was pretty
 * quick.
 * <p>
 * [b]A solution[/b]
 * I decided to transfer multiple files in parallel.  This way you could almost
 * guarantee one of the transfers would be pulling down data while others
 * where spinning CPU cycles waiting to make connections.  My goal was to
 * drive the bottleneck - in this case maxing out our bandwidth.
 * <p>
 * The result was pleasing.  The average transfer time for a single file sunk from
 * 831 milliseconds to 41 milliseconds!
 * [b]The Trick[/b]
 * The trick for me was how to [i]limit[/i] the number of running threads.  I did
 * not want to start a single thread for each file.  I suspect starting a thousand
 * threads with Java would be 'bad' - not even considering the load being put
 * placed on the poor FTP server at the other end - nor considering the fact
 * that our T1 would be suffering from massive contention.
 * <p>
 * The answer was (of course), ThreadBucket.  ThreadBucket's constructor takes
 * a number of threads as its sole argument.  You add runnables (effectively
 * workers.  In my case each FTP file transfer was a worker).  It spins up a new
 * Thread for the runnable.  If all the slots are already taken up with running
 * threads, it blocks until one becomes available.
 * [b]Example[/b]
 * [code]
 * FileFetchers fileFetchers[] = null;//... created here
 * ThreadBucket tb = new ThreadBucket(fileFetchers.length);
 * for(int i=0;i<memberNumbers.length;i++) {
 * // prime a few downloads
 * if(i ==0) {
 * for(int j=0; j < fileFetchers.length && j < memberNumbers.length; j++) {
 * fileFetchers[j].setFileToFetch("some file" + j);
 * tb.addRunnable(fileFetchers[j], true);
 * }
 * }
 * // get a file that has been retreived previously, blocking until one is available
 * FileFetcher aFileFetcher = (FileFetcher)tb.removeRunnable();
 * // ... do something with the data just returned ...
 * <p>
 * // prime the bucket with another file
 * if(i < memberNumbers.length - fileFetchers.length) {
 * aFileFetcher.setFileToFetch("some files" + i + fileFetchers.length);
 * tb.addRunnable(aFileFetcher,true);
 * }
 * }
 * [/code]
 * <p>
 * [b]Summary[/b]
 * ThreadBucket is useful where a task is best performed with multiple threads and
 * you want to put a limit on the number of threads active at any one time.
 * <p>
 * Can anyone else think of an application for ThreadBucket?  I created a C++ version
 * of it a few years back that was used for processing work queues from the
 * database (each job was added to the ThreadBucket).  It was great to identify
 * an optimal number of threads, set the bucket to that value and watch the CPU
 * climb closer to 100% (Find your limiting resource and push it to the limit).
 * Enjoy.  And feel free to contribute your own submissions to this forum.
 **/
public class ThreadBucket {
    private static final NarrativeLogger logger = new NarrativeLogger(ThreadBucket.class);

    private static final Set<ThreadBucket> ALL_THREAD_BUCKETS = new HashSet<ThreadBucket>();

    public static Set<ThreadBucket> getAllThreadBuckets() {
        return Collections.unmodifiableSet(ALL_THREAD_BUCKETS);
    }

    private final String name;
    private final TaskThread taskThreads[];
    final String threadBucketName;
    public final int maxQueueSize;
    /**
     * tries to keep age items in the queue for this many ms.  e.g. write out
     * session activity once every 60 seconds only
     */
    public int minQueueDelayMs = 0;
    /**
     * should the ThreadBucket keep task type statistics
     * for the average running time per task type?
     */
    private boolean keepTaskTypeStatistics = false;
    /**
     * the Hashtable in which to keep task type statistics
     * if enabled for this ThreadBucket.  the keys are Strings
     * representing the task type (optionally specified via
     * the DebugNameProvider interface).  the values are long
     * arrays with two elements in them.  the first element
     * is the number of context of this type that have executed
     * and the second is the total running time of the
     * context in milliseconds.
     */
    private Hashtable<String, long[]> taskTypeToStatistics = null;
    /**
     * keeps track of whether the current thread is being executed.  Allows
     * currentlyRunning to factor out the current thread if it is included
     * in the currently running count
     */
    private ThreadLocal<Boolean> isCurrentThreadACurrentlyRunningTask = new ThreadLocal<Boolean>();

    /**
     * Constructor that takes an argument which specifies the
     * number of TaskThreads to be constructed
     */
    public ThreadBucket(String name, int totalTaskThreads) {
        // bl: set the maxQueueSize to the integer max value.  most of the time, we don't want any queue size limit.
        this(name, totalTaskThreads, false);
    }

    public ThreadBucket(String name, int totalTaskThreads, int maxQueueSize) {
        this(name, totalTaskThreads, maxQueueSize, false);
    }

    /**
     * Constructor that takes an argument which specifies the
     * number of TaskThreads to be constructed and a flag indicating whether or not statistics should be kept
     */
    public ThreadBucket(String name, int totalTaskThreads, boolean keepTaskTypeStatistics) {
        // bl: set the maxQueueSize to the integer max value.  most of the time, we don't want any queue size limit.
        this(name, totalTaskThreads, Integer.MAX_VALUE, keepTaskTypeStatistics);
    }

    /**
     * @param keepTaskTypeStatistics true if the ThreadBucket should keep
     *                               statistics per task type that are obtainable via the
     *                               getThreadBucketStatistics() method.
     */
    public ThreadBucket(String name, int totalTaskThreads, int maxQueueSize, boolean keepTaskTypeStatistics) {
        this.name = name;
        threadBucketName = "TB-" + name;
        this.maxQueueSize = Math.max(maxQueueSize, totalTaskThreads);
        this.keepTaskTypeStatistics = keepTaskTypeStatistics;
        if (this.keepTaskTypeStatistics) {
            taskTypeToStatistics = new Hashtable<String, long[]>();
        }
        Debug.assertMsg(logger, totalTaskThreads > 0, "Task thread count must be >0");
        taskThreads = new TaskThread[totalTaskThreads];
        for (int i = 0; i < taskThreads.length; ++i) {
            TaskThread taskThread = new TaskThread(this);
            taskThreads[i] = taskThread;
            Thread thread = new Thread(taskThread, threadBucketName);
            taskThread.setThread(thread);
            thread.start();
        }
        // taking a two phase approach here because we want to 'join' on the thread
        // bucket.  without this approach we were getting deadlocks
        IPUtil.EndOfX.endOfAppComing.addRunnable("00ThreadBucket/" + name, new Runnable() {
            public void run() {
                synchronized (runningTasks) {
                    runningTasks.isStopping = true;
                    if (logger.isDebugEnabled()) {
                        logger.debug("eoac *");
                    }
                    runningTasks.notifyAll();
                }
            }
        });
        IPUtil.EndOfX.endOfApp.addRunnable("30ThreadBucket/" + name, new Runnable() {
            public void run() {
                waitForAllTasksToComplete(true);
            }
        });
        ALL_THREAD_BUCKETS.add(this);
    }

    public String getName() {
        return name;
    }

    public static class RunningTasks {
        private int currentlyRunning;
        private boolean isStopping;
        private int currentlyRunningTasksWaitingForCompletion;
        private LinkedList<RunnableWithTimeAdded> tasksToRun = new LinkedList<RunnableWithTimeAdded>();
    }

    private final RunningTasks runningTasks = new RunningTasks();

    /**
     * Clients calls this method to add their Tasks to
     * ThreadBucket. It'll return immediately if a thread was able to
     * process the request, else it will block until the job can be
     * taken by a running thread.
     * you almost certainly want to use addRunnableToQueue. this method will BLOCK if the queue is full!!!
     */
    public void addRunnableAndWaitIfFull(Runnable task) {
        addRunnable0(task, false);
    }

    /**
     * bl: adding this interface so that Runnables can define
     * their own names (other than just the class name) for
     * debugging purposes.  currently, we just log the class name,
     * which, in the case of the TaskScheduler, isn't useful since
     * all of the Runnable objects are TaskHandlerThread runnables
     * wrapping the "real" task being executed.  the TaskHandlerThread
     * can implement this interface to define its own name based
     * on the task that is currently executing.
     * <p>
     * nb. this class can be optionally implemented by the Runnable
     * object that is added to the ThreadBucket.  outside of
     * this usage, this interface serves no purpose.
     */
    public static interface DebugNameProvider {
        /**
         * get the name to use for debugging purposes
         * for this runnable.
         * nb. important!  if recording statistics for
         * the ThreadBucket this task will be used in, this
         * method must be consistent and always return
         * the same debug name for a given task!
         *
         * @return the name to use for debugging purposes
         */
        public String getDebugName();

        /**
         * get the debug info for this task.  only used
         * when generating XML for the currently running
         * context in this thread bucket.
         *
         * @return the debug info for a given task
         */
        public String getDebugInfo();
    }

    /**
     * get the name for the specified runnable to use
     * for debugging purposes.  checks to see if the Runnable
     * also implements DebugNameProvider and will use the
     * self-defined name, if supplied.
     *
     * @param r the runnable to get the name for
     * @return
     */
    private static String getNameForRunnable(Runnable r) {
        String ret = null;
        if (r instanceof DebugNameProvider) {
            ret = ((DebugNameProvider) r).getDebugName();
        }

        if (IPStringUtil.isEmpty(ret)) {
            ret = IPUtil.getClassSimpleName(r.getClass());
        }
        return ret;
    }

    /**
     * get the debug info for the specified runnable to use
     * for debugging purposes.  checks to see if the Runnable
     * also implements DebugNameProvider and will use the
     * self-defined debug info, if supplied.
     * if the runnable doesn't implement DebugNameProvider,
     * then null will be returned
     *
     * @param r the runnable to get the debug info for
     * @return null if the specified runnable doesn't implement the
     * DebugNameProvider.  otherwise, return the debug info
     * returned by the DebugNameProvider
     */
    private static String getDebugInfoForRunnable(Runnable r) {
        if (!(r instanceof DebugNameProvider)) {
            return null;
        }
        return ((DebugNameProvider) r).getDebugInfo();
    }

    /**
     * Add a task.  Will return immediately the queue isn't already full.
     */
    public void addRunnableToQueue(Runnable task) {
        addRunnable0(task, true);
    }

    public static class RunnableWithTimeAdded {
        final Runnable r;
        final long creationTime = System.currentTimeMillis();

        public RunnableWithTimeAdded(Runnable r) {
            this.r = r;
        }

        public String getName() {
            if (r == null) {
                return null;
            }
            return getNameForRunnable(r);
        }

        public String getDebugInfo() {
            if (r == null) {
                return null;
            }
            return getDebugInfoForRunnable(r);
        }

        public Runnable getRunnable() {
            return r;
        }
    }

    private void addRunnable0(Runnable task, boolean queueItUp) {
        String runnableName = getNameForRunnable(task);
        if (logger.isDebugEnabled()) {
            logger.debug("Adding runnable: " + runnableName + " to threadbucket with " + runningTasks.tasksToRun.size() + " tasks ahead of it");
        }
        synchronized (runningTasks) {
            do {
                // insert an entry into the task type statistics table for this task
                // if it doesn't already exist.
                if (keepTaskTypeStatistics) {
                    long[] stats = taskTypeToStatistics.get(runnableName);
                    if (stats == null) {
                        taskTypeToStatistics.put(runnableName, stats = new long[2]);
                        // initialize the values.  call me paranoid.
                        stats[0] = 0;
                        stats[1] = 0;
                    }
                }
                // if we're stopped just run inline
                if (runningTasks.isStopping) {
                    // get out of this sync section
                    break;
                }
                // pop it on the queue if..
                boolean addIt = false;
                do {
                    if (queueItUp) {
                        // its ok to queue it up and we're not over our max
                        if (runningTasks.currentlyRunning + runningTasks.tasksToRun.size() <= maxQueueSize) {
                            addIt = true;
                            break;
                        }
                        // if we only have one thread and a runnable on that thread is adding another
                        // runnable, extend our maxqueue by one, else we'll block for ever (its happened!)
                        if (null != isCurrentThreadACurrentlyRunningTask.get() && taskThreads.length == 1 && runningTasks.currentlyRunning + runningTasks.tasksToRun.size() <= maxQueueSize + 1) {
                            addIt = true;
                            break;
                        }
                        // if we only have the one thread, and its currently running
                        // and we're trying to add a new runnable (and we've already gone
                        // over our +1 generosity, then throw an exception and get the heck out of 
                        // dodge (vs. locking up).
                        if (null != isCurrentThreadACurrentlyRunningTask.get() && taskThreads.length == 1) {
                            Debug.assertMsg(logger, false, "Unable to add this job " + runnableName + " since the current task is in the way");
                        }
                        break;
                    }
                    // we're not going to max out our task threads
                    if (runningTasks.currentlyRunning + runningTasks.tasksToRun.size() <= taskThreads.length) {
                        addIt = true;
                        break;
                    }
                } while (false);
                if (addIt) {
                    RunnableWithTimeAdded rwta = new RunnableWithTimeAdded(task);
                    runningTasks.tasksToRun.add(rwta);
                    if (logger.isDebugEnabled()) {
                        logger.debug("addElement: " + runnableName);
                    }
                    runningTasks.notify();
                    return;
                }
                // else wait for some spare capacity
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("addElement wait: " + runnableName);
                    }
                    runningTasks.wait();
                } catch (InterruptedException ie) {
                    logger.error("Ignoring exception", ie);
                }
            } while (true);

        }
        // threadbucket is stopping.  run the task synchronously
        if (logger.isDebugEnabled()) {
            logger.debug("Running a task " + runnableName + " inline since we're already stopped");
        }
        runTask0(this, task);
    }

    /**
     * run a runnable task and record necessary logs and stats.
     *
     * @param threadBucket the ThreadBucket that this task is being run for.
     * @param r            the runnable to execute.
     */
    private static void runTask0(ThreadBucket threadBucket, Runnable r) {
        String runnableName = getNameForRunnable(r);
        long startTime = System.currentTimeMillis();
        try {
            // todo: need to track long running actions in ThreadBuckets
            //LongActionRegistry.getInstance().registerLongAction(lro, startTime, runnableName, null, LongActionRegistry.MISC_ACTION);
            r.run();
        } catch (Throwable t) {
            logger.error("Failed running " + runnableName, t);
        } finally {
            // if keep statistics for this thread bucket, then update the stats
            // once the task has completed.
            // nb. not synchronized because we don't terribly care about the
            // results being perfect.
            if (threadBucket.keepTaskTypeStatistics) {
                // the stats array gets initialized in addRunnable0() so this value
                // should always be specified in the Hashtable.
                long[] stats = threadBucket.taskTypeToStatistics.get(runnableName);
                assert stats != null : "found a runnable type that didn't have statistics entered.  does the Runnable implement the DebugNameProvider interface and implement an inconsistent getDebugName() method? class:" + r.getClass().getName() + " task:" + runnableName;
                // increment the count, and add on the total run time of context of this type.
                stats[0]++;
                stats[1] += (System.currentTimeMillis() - startTime);
            }
            //LongActionRegistry.getInstance().deregisterLongAction(lro);
            IPUtil.onEndOfThread();
        }
    }

    public int getThreadCount() {
        return taskThreads.length;
    }

    public int getIncompleteTaskCount() {
        return runningTasks.currentlyRunning + runningTasks.tasksToRun.size();
    }

    public List<TaskThread> getTaskThreads() {
        return Arrays.asList(taskThreads);
    }

    public Map<String, long[]> getTaskTypeToStatistics() {
        return new HashMap<String, long[]>(taskTypeToStatistics);
    }

    public long getTotalTasksRun() {
        long totalTasksRun = 0;
        for (long[] stats : taskTypeToStatistics.values()) {
            totalTasksRun += stats[0];
        }
        return totalTasksRun;
    }

    public long getTotalRunTime() {
        long totalRunTime = 0;
        for (long[] stats : taskTypeToStatistics.values()) {
            totalRunTime += stats[1];
        }
        return totalRunTime;
    }

    /**
     * determine if this ThreadBucket is in the process of stopping.
     *
     * @return true if this ThreadBucket is stopping. false otherwise.
     */
    public boolean isStopping() {
        return runningTasks.isStopping;
    }

    /**
     * If the stack is empty, checks the isStopping flag and if
     * it is not true puts the TaskThread in wait state. If
     * the isStopping flag is set to true, return null there by
     * stopping the TaskThread. If the stack is not empty
     * returns the lost ScheduledTask added into the ScheduledTask.
     */
    private RunnableWithTimeAdded getNextTask() {
        synchronized (runningTasks) {
            do {
                int remaining = runningTasks.tasksToRun.size();
                if (remaining > 0) {
                    RunnableWithTimeAdded r = runningTasks.tasksToRun.removeFirst();
                    runningTasks.currentlyRunning++;
                    isCurrentThreadACurrentlyRunningTask.set(Boolean.TRUE);
                    if (logger.isDebugEnabled()) {
                        logger.debug("removeElement");
                    }
                    runningTasks.notify();
                    // bit of debug info
                    {
                        if (remaining > 30) {
                            if (logger.isInfoEnabled()) {
                                logger.info("ThreadBucket(" + threadBucketName + ") pretty busy with " + remaining + " remaining jobs");
                            }
                        }
                        if (runningTasks.isStopping) {
                            if (logger.isInfoEnabled()) {
                                logger.info("ThreadBucket(" + threadBucketName + ") churning through " + remaining + " more jobs ... including a '" + r.r.getClass() + "'");
                            }
                        }
                    }
                    return r;
                }
                if (runningTasks.isStopping) {
                    // let everyone elese who is wait know we're done
                    if (logger.isDebugEnabled()) {
                        logger.debug("getNextTaskIsStopped *");
                    }
                    runningTasks.notifyAll();
                    return null;
                }
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getnexttask wait");
                    }
                    runningTasks.wait();
                } catch (InterruptedException ie) {
                    logger.error("Ignoring interrupted exception", ie);
                }
            } while (true);
        }
    }

    /**
     * Iterate through the vector to get all the TaskThreads
     * and calls it stopMe method. Then sets the isStopping flag
     * to true. At the end calls the notifyAll. This call sends
     * a signal to all the waiting TaskThreads to stop.
     * NOTE: stop method in java.lang.Thread is deprecated
     * in Java 1.2, so we can't use those methods to stop
     * the thread. For more information refer to the
     * documentation at the following web site:
     * www.javasoft.com/products/jdk/1.2/docs/api/index.asp
     */
    public void waitForAllTasksToComplete(boolean isStopping) {
        synchronized (runningTasks) {
            if (isStopping) {
                runningTasks.isStopping = true;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("waitforalltaskstocomplete *");
            }
            if (isCurrentThreadACurrentlyRunningTask.get() != null) {
                runningTasks.currentlyRunningTasksWaitingForCompletion++;
            }
            runningTasks.notifyAll();
        }
        try {
            synchronized (runningTasks) {
                do {
                    if (logger.isInfoEnabled()) {
                        logger.info("waitForAllTasksToComplete: " + "runqueue = " + runningTasks.tasksToRun.size() + ", currentlyRunning = " + runningTasks.currentlyRunning + ", currentlyRunningTasksWaitingForCompletion " + runningTasks.currentlyRunningTasksWaitingForCompletion);
                    }

                    // current thread is a ThreadBucket runnable
                    // it calls waitForAllTasksToComplete
                    // we'll never exit (since there will always be 1 currently
                    // executing thread.  Hence we keep track of whether this 
                    // thread is launched by the ThreadBucket or not
                    if (runningTasks.tasksToRun.size() + runningTasks.currentlyRunning - runningTasks.currentlyRunningTasksWaitingForCompletion == 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("wait for all done *");
                        }
                        // let everyone know we're done incase they were waiting on getNextTask
                        runningTasks.notifyAll();
                        return;
                    }
                    if (logger.isInfoEnabled()) {
                        logger.info("Waiting for " + runningTasks.tasksToRun.size() + " queued context and " + runningTasks.currentlyRunning + " currently running threads to complete");
                    }
                    try {
                        if (logger.isDebugEnabled()) {
                            logger.debug("wait for complete wait");
                        }
                        if (!isStopping) {
                            // bl: if we aren't stopping, then we want to wait with a delay to ensure
                            // that we will be woken back up
                            runningTasks.wait(IPDateUtil.SECOND_IN_MS);
                        } else {
                            runningTasks.wait();
                        }
                    } catch (InterruptedException ie) {
                        logger.error("Ignoring exception", ie);
                    }
                } while (true);
            }
        } finally {
            synchronized (runningTasks) {
                if (isCurrentThreadACurrentlyRunningTask.get() != null) {
                    runningTasks.currentlyRunningTasksWaitingForCompletion--;
                }
                runningTasks.notifyAll();
            }
        }
    }

    protected void finalize() throws Throwable {
        waitForAllTasksToComplete(true);
        super.finalize();
    }

    public static class TaskThread implements Runnable {
        private static int s_taskThreadNameIndex = 0;
        private final ThreadBucket threadBucket;
        private Thread thread;
        /**
         * keep track of the time the currently executing task started.
         */
        private long startTime;
        /**
         * keep track of the currently executing task
         */
        private RunnableWithTimeAdded currentTask = null;

        /**
         * Construct a TaskThread by taking an instance of
         * ThreadBucket object
         */
        public TaskThread(ThreadBucket threadBucket) {
            this.threadBucket = threadBucket;
        }

        private void setThread(Thread thread) {
            this.thread = thread;
        }

        public RunnableWithTimeAdded getCurrentTask() {
            return currentTask;
        }

        public long getCurrentTaskRunTime() {
            return System.currentTimeMillis() - startTime;
        }

        public void interrupt() {
            synchronized (this) {
                if (currentTask != null) {
                    thread.interrupt();
                }
            }
        }

        /**
         * This method is implemented from the java.lang.Runnable
         * interface. As long as the stopMe flag is false,
         * continuously gets the ScheduledTask from ThreadBucket and
         * executes it, notifies the task completion to
         * TaskDoneListener object. When the stopMe flag is true
         * comes out of the loop.
         */
        public void run() {
            try {
                do {
                    RunnableWithTimeAdded task = threadBucket.getNextTask();
                    if (task == null) {
                        return;
                    }
                    Thread.currentThread().setName(threadBucket.threadBucketName + "-" + getNameForRunnable(task.r) + "-" + (++s_taskThreadNameIndex));
                    if (threadBucket.minQueueDelayMs > 0) {
                        // provide a little processing delay (e.g. for session activity)
                        long timeSinceItemAdded = System.currentTimeMillis() - task.creationTime;
                        if (timeSinceItemAdded < threadBucket.minQueueDelayMs) {
                            long timeToSleep = threadBucket.minQueueDelayMs - timeSinceItemAdded;
                            if (timeToSleep > 0) {
                                IPUtil.uninterruptedSleep(timeToSleep);
                            }
                        }

                    }
                    currentTask = task;
                    startTime = System.currentTimeMillis();
                    runTask0(threadBucket, task.r);
                    startTime = 0;
                    synchronized (this) {
                        currentTask = null;
                        // clear the interrupted flag in case interrupt was called on this task.
                        Thread.interrupted();
                    }
                    Thread.currentThread().setName(threadBucket.threadBucketName + "-Waiting");
                    synchronized (threadBucket.runningTasks) {
                        //threadBucket.isCurrentThreadACurrentlyRunningTask.set(null);
                        threadBucket.runningTasks.currentlyRunning--;
                        // give getNextTask a chance to grab the next message if its waiting
                        if (threadBucket.runningTasks.isStopping) {
                            // make sure everyone gets a chance to wake up (else
                            // with notify a getNextTask may wake up, but not
                            // the waitoncomplete thread)
                            if (logger.isDebugEnabled()) {
                                logger.debug("runtask stopping *");
                            }
                            threadBucket.runningTasks.notifyAll();
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("runtask not stopping *");
                            }
                            threadBucket.runningTasks.notify();
                        }
                    }

                } while (true);
            } catch (Throwable t) {
                logger.error("please forward to peter: TaskThread died.  This threadbucket will start filling up now.", t);
            }
        }
    }
//    public static class TestHarness implements RegressionTest.Testable, Runnable {
//        Vector errors = new Vector();
//        static int runCount = 0;
//        static int concurrentRunCount = 0;
//        int sumOfConcurrentRunCount = 0;
//        static final int MAX_THREAD_COUNT = 3;
//        ThreadBucket tpm = new ThreadBucket("TestHarness", MAX_THREAD_COUNT);
//        public String getTestName() {
//            return "ThreadBucket";
//        }
//        /* 
//./runapp.sh org.narrative.common.util.ThreadBucket\$TestHarness
//*/
//
//        public static void main(String args[]) {
//            Debug.APP_NAME = "ThreadBucket";
//            IPUtil.uninterruptedSleep(4000);
//            try {
//                do {
//                    new TestHarness().performTest0();
//                } while(true);
//            } finally {
//                IPUtil.onEndOfApp();
//            }
//        }
//        public void performTest(RegressionTest.TestData td) {
//            performTest0();
//        }
//        private void performTest0() {
//            Thread t[] = new Thread[MAX_THREAD_COUNT];
//            for(int i=0;i<t.length;i++) {
//                t[i] = new Thread(this);
//                t[i].start();
//            }
//            for(int i=0;i<t.length;i++) {
//                try {
//                    t[i].join();
//                } catch(Throwable t1) {
//                    ; //ignore
//                }
//            }
//            if(logger.isInfoEnabled()) logger.info("context run: " + (runCount));
//            if(logger.isInfoEnabled()) logger.info("average simultaneous running context: " + (sumOfConcurrentRunCount/Math.max(0, runCount)));
//            if(errors.size()>0) {
//                Debug.assertMsg(logger, false, "got errors running the ThreadBucket tests.  e.g. " + Debug.getErrorDetail("err", (Throwable)errors.elementAt(0)));
//            }
//            /*
//            This test fails.  looks like the clock is only accurate to 10's of ms.
//            {
//                for(int i=0;i<100;i++) {
//                    long waitTime = IPUtil.getRandomNumber(4,80);
//                    long startTime = System.currentTimeMillis();
//                    IPUtil.uninterruptedSleep(waitTime);
//                    long now = System.currentTimeMillis();
//                    Debug.assertMsg(logger, startTime+waitTime<=now, "sleep didn't sleep the correct amount of time, only: " + (now-startTime) + " not " + waitTime);
//                }
//            }
//            */
//            {
//                final long waitTimeMs = 400;
//                if(logger.isInfoEnabled()) logger.info("Doing a shutdown.  SHould take " + waitTimeMs + "ms");
//                long startTime = System.currentTimeMillis();
//                tpm.addRunnableAndWaitIfFull(new Runnable() {
//                    public void run() {
//                        IPUtil.uninterruptedSleep(waitTimeMs);
//                    }
//                });
//                tpm.waitForAllTasksToComplete();
//                long timeWaited = System.currentTimeMillis()-startTime;
//                // due to clock only being accurate to 10ms
//                if(Math.abs(timeWaited-waitTimeMs)>20 && timeWaited<waitTimeMs) {
//                    Debug.assertMsg(logger, false, "Does not look like stopAllTaskThreads worked correctly since it took: " + timeWaited + "ms not " + waitTimeMs + "ms");
//                }
//                if(logger.isInfoEnabled()) logger.info("Shutdown took " + (timeWaited));
//            }
//            {
//                if(logger.isInfoEnabled()) logger.info("try a shutdown while there are runnables still in the queue");
//                tpm = new ThreadBucket("TestHarness", Math.max(2, MAX_THREAD_COUNT+IPUtil.getRandomNumber(-5,5)));
//                for(int i=0;i<MAX_THREAD_COUNT+IPUtil.getRandomNumber(-10,10);i++) {
//                    final int foo = i;
//                    tpm.addRunnableAndWaitIfFull(new Runnable() {
//                        public void run() {
//                            if(foo==5)
//                                tpm.waitForAllTasksToComplete();
//                            else
//                                IPUtil.uninterruptedSleep(10);
//                        }
//                    });
//                }
//                tpm.waitForAllTasksToComplete();
//                {
//                    // tests the min queue delay is effective.  a task should
//                    // be processed no sooner than that time after it was added.
//                    if(logger.isInfoEnabled()) logger.info("testing minQueueDelayMs");
//                    tpm = new ThreadBucket("test", 4, Integer.MAX_VALUE);
//                    tpm.minQueueDelayMs = 10000;
//                    long startTimeMs = System.currentTimeMillis();
//                    for(int i=0;i<100;i++) {
//                        tpm.addRunnableToQueue(new Runnable() {
//                                public void run() {
//                                }
//                                });
//                    }
//                    tpm.waitForAllTasksToComplete();
//                    long finishTime = System.currentTimeMillis();
//                    long duration = (finishTime-startTimeMs);
//                    String msg = "Completion time = " + duration + "ms, min should have been " + tpm.minQueueDelayMs + "ms";
//                    if(duration<tpm.minQueueDelayMs) {
//                        Debug.assertMsg(logger, false, msg);
//                    }
//                    if(duration>tpm.minQueueDelayMs*1.1) {
//                        Debug.assertMsg(logger, false, "took longer than expected to run with a minQueueDelayMs, " + msg);
//                    }
//                    if(logger.isInfoEnabled()) logger.info( msg);
//                }
//            }
//        }
//        public void run() {
//            long completionTime = System.currentTimeMillis()+1*IPDateUtil.SECOND_IN_MS;
//            do {
//                try {
//                    tpm.addRunnableAndWaitIfFull(new Runnable(){
//                        public void run() {
//                            runCount++;
//                            concurrentRunCount++;
//                            try {
//                                if(runCount%1000==0) {
//                                    if(logger.isInfoEnabled()) logger.info("Progress: " + runCount + " executions, " + concurrentRunCount + " concurrent");
//                                }
//                                IPUtil.uninterruptedSleep(IPUtil.getRandomNumber(0,10));
//                                if(concurrentRunCount>MAX_THREAD_COUNT) {
//                                    Debug.assertMsg(logger, false, "went over the max thread count.  Got " + concurrentRunCount + " concurrent threads");
//                                }
//                                sumOfConcurrentRunCount+=concurrentRunCount;
//                            } finally {
//                                concurrentRunCount--;
//                            }
//                        }
//                    });
//                } catch(Throwable t) {
//                    logger.error("Failed adding a runnable task", t);
//                    errors.addElement(t);
//                }
//
//            } while(System.currentTimeMillis()<completionTime);
//        }
//    }
}
