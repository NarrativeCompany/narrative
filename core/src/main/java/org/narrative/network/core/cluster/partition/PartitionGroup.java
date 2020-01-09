package org.narrative.network.core.cluster.partition;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.persistence.hibernate.GSessionFactory;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.ThreadBucketType;
import org.narrative.network.shared.tasktypes.PartitionTask;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Oct 13, 2006
 * Time: 1:08:04 PM
 * This is a class that manages groups of sessions that execute as a whole
 */
public class PartitionGroup {

    private static final NetworkLogger logger = new NetworkLogger(PartitionGroup.class);

    private static final ThreadLocal<PartitionGroup> currentPartitionGroup = new ThreadLocal<PartitionGroup>();

    private boolean inError = false;

    private boolean isReadOnly = false;

    private boolean isInNonForceWritableTask = false;

    private final Map<PartitionKey, GSession> openSessions = new TreeMap<PartitionKey, GSession>(new Comparator<PartitionKey>() {
        public int compare(PartitionKey o1, PartitionKey o2) {
            int ret = o1.getPartitionType().getCommitOrder().compareTo(o2.getPartitionType().getCommitOrder());
            if (ret != 0) {
                return ret;
            }
            ret = OID.compareOids(o1.getPartitionOid(), o2.getPartitionOid());
            assert ret == 0 || !o1.getPartitionType().isSingleton() : "Can't have two singleton partitions with the same commit order!";
            return ret;
        }

    });

    private final Map<PartitionType, Collection<Partition>> partitionTypeToPartitionWithOpenSessions = new HashMap<PartitionType, Collection<Partition>>();
    private final LinkedList<PartitionGroupRunnable> preCommitEndOfPartitionGroupRunnables = new LinkedList<PartitionGroupRunnable>();
    private final LinkedList<PartitionGroupRunnable> endOfPartitionGroupRunnables = new LinkedList<PartitionGroupRunnable>();
    private final LinkedList<PartitionGroupRunnable> endOfPartitionGroupRunnablesForUtilityThread = new LinkedList<PartitionGroupRunnable>();
    private final LinkedList<PartitionGroupRunnable> highPriEndOfPartitionGroupRunnables = new LinkedList<PartitionGroupRunnable>();
    private final LinkedList<PartitionGroupRunnable> highPriEndOfPartitionGroupRunnablesForUtilityThread = new LinkedList<PartitionGroupRunnable>();

    private final Map<String, Object> partitionGroupProperties = new HashMap<>();

    PartitionGroup() {}

    public boolean isInError() {
        return inError;
    }

    public void setInError(boolean inError) {
        this.inError = inError;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
        for (GSession gSession : openSessions.values()) {
            gSession.setReadOnly(readOnly);
        }
    }

    public boolean isInNonForceWritableTask() {
        return isInNonForceWritableTask;
    }

    public void setInNonForceWritableTask(boolean inNonForceWritableTask) {
        isInNonForceWritableTask = inNonForceWritableTask;
        for (GSession gSession : openSessions.values()) {
            gSession.setInNonForceWritableTask(isInNonForceWritableTask);
        }
    }

    public Map<PartitionKey, GSession> getOpenSessions() {
        return openSessions;
    }

    public void addPartitionWithOpenSession(Partition partition) {
        PartitionType partitionType = partition.getPartitionType();
        Collection<Partition> partitions = partitionTypeToPartitionWithOpenSessions.get(partitionType);
        if (partitions == null) {
            partitionTypeToPartitionWithOpenSessions.put(partitionType, partitions = new HashSet<Partition>());
        }
        partitions.add(partition);
    }

    public Map<PartitionType, Collection<Partition>> getPartitionTypeToPartitionWithOpenSessions() {
        return partitionTypeToPartitionWithOpenSessions;
    }

    public void addPartitionGroupProperty(String id, Object property) {
        partitionGroupProperties.put(id, property);
    }

    public <T> T getPartitionGroupProperty(String id) {
        return (T) partitionGroupProperties.get(id);
    }

    public void performSingleInstanceJitSafetyChecks(String jitKey) {
        // jw: The main piece of safety we are checking is to ensure that this key is only initialized once per
        //     partition group.
        Boolean hasInited = getPartitionGroupProperty(jitKey);
        if (hasInited != null) {
            throw UnexpectedError.getRuntimeException("Should only ever JIT-init value once per request! key/" + jitKey);
        }
        addPartitionGroupProperty(jitKey, Boolean.TRUE);
    }

    public void processRunnables() {

        //run the end of partition group runnables
        processRunnables(highPriEndOfPartitionGroupRunnables, endOfPartitionGroupRunnables, isInError());

        // send the utility thread runnables to be run in a separate thread (if there are any)
        if (!highPriEndOfPartitionGroupRunnablesForUtilityThread.isEmpty() || !endOfPartitionGroupRunnablesForUtilityThread.isEmpty()) {
            ThreadBucketType.UTILITY.addRunnable(new Runnable() {
                public void run() {
                    processRunnables(highPriEndOfPartitionGroupRunnablesForUtilityThread, endOfPartitionGroupRunnablesForUtilityThread, isInError());
                }
            });
        }
    }

    public void processPreCommitRunnables() {
        processRunnables(null, preCommitEndOfPartitionGroupRunnables, isInError());
    }

    private static void processRunnables(LinkedList<PartitionGroupRunnable> highPriRunnables, LinkedList<PartitionGroupRunnable> runnables, boolean isInError) {

        while ((highPriRunnables != null && !highPriRunnables.isEmpty()) || !runnables.isEmpty()) {

            PartitionGroupRunnable runnable;
            if (highPriRunnables != null) {
                //always process all the high priority runnables first
                while ((runnable = highPriRunnables.poll()) != null) {
                    runnable.runIfRequired(isInError);
                }
            }

            //then process one low priority runnable one at a time, allowing any more high priority runnables that may have been created a chance to sneak in
            runnable = runnables.poll();
            if (runnable != null) {
                runnable.runIfRequired(isInError);
            }

        }
    }

    public void clearAllSessions() {
        for (GSession sess : getOpenSessions().values()) {
            sess.clearSession();
        }
    }

    public void flushAllSessions() {
        for (GSession sess : getOpenSessions().values()) {
            if (sess.isReadOnly()) {
                continue;
            }

            PartitionType.doSessionTask(sess, () -> {
                GSessionFactory gsf = sess.getGSessionFactory();
                GSession oldCurrentGSession = gsf.hasCurrentSession() ? gsf.getCurrentSession() : null;
                try {
                    gsf.setCurrentSession(sess);
                    sess.flushSession();
                } finally {
                    gsf.setCurrentSession(oldCurrentGSession);
                }
            });
        }
    }

    public void commitBatchAllSessions() {
        for (GSession sess : getOpenSessions().values()) {
            if (sess.isReadOnly()) {
                continue;
            }

            PartitionType.doSessionTask(sess, () -> {
                GSessionFactory gsf = sess.getGSessionFactory();
                GSession oldCurrentGSession = gsf.hasCurrentSession() ? gsf.getCurrentSession() : null;
                try {
                    gsf.setCurrentSession(sess);
                    sess.commitBatch();
                } finally {
                    gsf.setCurrentSession(oldCurrentGSession);
                }
            });
        }
    }

    public void addPreCommitEndOfGroupRunnable(Runnable runnable) {
        preCommitEndOfPartitionGroupRunnables.add(new PreCommitPartitionGroupRunnable(runnable));
    }

    public void addEndOfGroupRunnable(Runnable runnable) {
        addEndOfGroupRunnable(runnable, false);
    }

    public void addEndOfGroupRunnableForError(Runnable runnable) {
        addEndOfGroupRunnable(runnable, true);
    }

    public void addEndOfGroupRunnableForSuccessOrError(Runnable runnable) {
        addEndOfGroupRunnable(runnable, null);
    }

    private void addEndOfGroupRunnable(Runnable runnable, Boolean ifInError) {
        if (runnable instanceof HighPriorityRunnable) {
            highPriEndOfPartitionGroupRunnables.add(new PartitionGroupRunnable(runnable, ifInError));
        } else {
            endOfPartitionGroupRunnables.add(new PartitionGroupRunnable(runnable, ifInError));
        }
    }

    public void addEndOfGroupRunnableForUtilityThread(Runnable runnable) {
        addEndOfGroupRunnableForUtilityThread(runnable, false);
    }

    public void addEndOfGroupRunnableForUtilityThreadForError(Runnable runnable) {
        addEndOfGroupRunnableForUtilityThread(runnable, true);
    }

    public void addEndOfGroupRunnableForUtilityThreadForSuccessOrError(Runnable runnable) {
        addEndOfGroupRunnableForUtilityThread(runnable, null);
    }

    private void addEndOfGroupRunnableForUtilityThread(Runnable runnable, Boolean ifInError) {
        if (runnable instanceof HighPriorityRunnable) {
            highPriEndOfPartitionGroupRunnablesForUtilityThread.add(new PartitionGroupRunnable(runnable, ifInError));
        } else {
            endOfPartitionGroupRunnablesForUtilityThread.add(new PartitionGroupRunnable(runnable, ifInError));
        }
    }

    public void initConnections() {
        for (GSession gSession : getOpenSessions().values()) {
            gSession.getConnection().initConnection();
        }
    }

    private static final String TASKS_FOR_FLUSHING_ON_SUCCESS = PartitionGroup.class.getName() + "-TasksForFlushingOnSuccess";

    public <T> void registerTaskForFlushingOnSuccess(PartitionTask<T> task) {
        Set<PartitionTask<T>> tasks = getPartitionGroupProperty(TASKS_FOR_FLUSHING_ON_SUCCESS);
        if (tasks == null) {
            addPartitionGroupProperty(TASKS_FOR_FLUSHING_ON_SUCCESS, tasks = new HashSet<>());
        }
        tasks.add(task);
    }

    public <T> void flushAllSessionsIfNecessaryForTask(PartitionTask<T> task) {
        // bl: not going to flush if in error
        if (isInError()) {
            return;
        }
        Set<PartitionTask<T>> tasks = getPartitionGroupProperty(TASKS_FOR_FLUSHING_ON_SUCCESS);
        // bl: we're only going to flush if this task was registered for flushing!
        if (tasks == null || !tasks.contains(task)) {
            return;
        }
        // found the task registered, so go ahead and flush for it!
        flushAllSessions();
    }

    private class PartitionGroupRunnable implements Runnable {
        protected final Runnable runnable;
        private final Boolean ifInError;

        private String debugInfo;

        public PartitionGroupRunnable(Runnable runnable, Boolean ifInError) {
            this.runnable = runnable;
            this.ifInError = ifInError;
        }

        public void run() {
            assert !isCurrentPartitionGroupSet() : "Should never process a PartitionGroupRunnable when the PartitionGroup is set!";
            setCurrentPartitionGroup(PartitionGroup.this);
            try {
                runnable.run();
            } catch (Throwable t) {
                // bl: ignore PartitionGroup runnable errors so that an error here will not cause other
                // PartitionGroup runnables not to run.
                StatisticManager.recordException(t, false, null);
                logger.error("Failed executing PartitionGroup runnable " + runnable.getClass() + " ifInError/" + ifInError + " debugInfo/" + debugInfo + ". Ignoring and continuing on to other runnables.", t);
            }
            setCurrentPartitionGroup(null);
        }

        public void runIfRequired(boolean inError) {
            if (ifInError == null || inError == ifInError.booleanValue()) {
                run();
            }
        }
    }

    private class PreCommitPartitionGroupRunnable extends PartitionGroupRunnable {
        private final Map<PartitionType, ObjectPair<GSession, OID>> sessionState;

        private PreCommitPartitionGroupRunnable(Runnable runnable) {
            super(runnable, false);
            // record the current partition state at the time the pre-commit runnable is added
            sessionState = PartitionType.extractCurrentPartitionSessions();
        }

        @Override
        public void run() {
            assert isEqual(PartitionGroup.this, getCurrentPartitionGroup()) : "Should never run a PreCommitPartitionGroupRunnable within the context of a PartitionGroup it was not constructed from.";

            Map<PartitionType, ObjectPair<GSession, OID>> originalState = newHashMap();
            PartitionType.extractAndClearCurrentPartitionSessions(originalState);
            try {
                // set the state back to the original state when the pre-commit runnable was first created
                PartitionType.replacePartitionSessions(sessionState);
                // bl: run the runnable directly. do _not_ wrap in a try/catch. if a pre-commit runnable
                // throws an exception, we _want_ it to bubble up to the PartitionType.doTask() invocation
                // so that the entire transaction is rolled back!
                runnable.run();
            } finally {
                // bl: clear the sessions first, and then replace the partition sessions
                PartitionType.clearCurrentPartitionSessions();
                PartitionType.replacePartitionSessions(originalState);
            }
        }
    }

    /**
     * Add a runnable to be executed at the end of the *thread* depending on if the partition group is in error or not
     *
     * @param runnable d
     */
    public static void addEndOfPartitionGroupRunnable(Runnable runnable) {
        getCurrentPartitionGroup().addEndOfGroupRunnable(runnable);
    }

    public static void addEndOfPartitionGroupRunnableForError(Runnable runnable) {
        getCurrentPartitionGroup().addEndOfGroupRunnableForError(runnable);
    }

    public static void addEndOfPartitionGroupRunnableForSuccessOrError(Runnable runnable) {
        getCurrentPartitionGroup().addEndOfGroupRunnableForSuccessOrError(runnable);
    }

    public static void addEndOfPartitionGroupRunnableForUtilityThread(final Runnable runnable) {
        getCurrentPartitionGroup().addEndOfGroupRunnableForUtilityThread(runnable);
    }

    public static void addEndOfPartitionGroupRunnableForUtilityThreadForError(Runnable runnable) {
        getCurrentPartitionGroup().addEndOfGroupRunnableForUtilityThreadForError(runnable);
    }

    public static void addEndOfPartitionGroupRunnableForUtilityThreadForSuccessOrError(Runnable runnable) {
        getCurrentPartitionGroup().addEndOfGroupRunnableForUtilityThreadForSuccessOrError(runnable);
    }

    public static boolean isCurrentPartitionGroupSet() {
        return currentPartitionGroup() != null;
    }

    static PartitionGroup currentPartitionGroup() {
        return currentPartitionGroup.get();
    }

    public static PartitionGroup getCurrentPartitionGroup() {
        assert isCurrentPartitionGroupSet() : "There is no current partition group set.  Must run in the context of a Network Task";
        return currentPartitionGroup();
    }

    public static boolean isCurrentPartitionGroupInError() {
        return getCurrentPartitionGroup().isInError();
    }

    public static void setCurrentPartitionGroupInError(boolean inError) {
        getCurrentPartitionGroup().setInError(inError);
    }

    static void setCurrentPartitionGroup(PartitionGroup partitionGroup) {
        currentPartitionGroup.set(partitionGroup);
    }

}
