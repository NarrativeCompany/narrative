package org.narrative.network.core.cluster.partition;

import org.narrative.common.persistence.DatabaseType;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.persistence.hibernate.GSessionFactory;
import org.narrative.common.util.Debug;
import org.narrative.common.util.NarrativeException;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.PartialTransactionCommitException;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.StringEnum;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.Encryption;
import org.narrative.network.shared.context.AreaContext;
import org.narrative.network.shared.context.NetworkContextImpl;
import org.narrative.network.shared.context.NetworkContextImplBase;
import org.narrative.network.shared.services.NetworkAnnotationConfiguration;
import org.narrative.network.shared.tasktypes.AllPartitionsTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.NetworkTaskImpl;
import org.narrative.network.shared.tasktypes.PartitionDownErrorReturnValue;
import org.narrative.network.shared.tasktypes.PartitionTask;
import org.narrative.network.shared.tasktypes.TaskIsolationLevel;
import org.narrative.network.shared.tasktypes.TaskOptions;
import org.narrative.network.shared.util.NetworkLogger;
import org.hibernate.cfg.Environment;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 24, 2005
 * Time: 1:50:51 AM
 */
public enum PartitionType implements StringEnum {
    GLOBAL("GLOBAL", true, 5) {
        @Override
        public List<String> getExtraInstallRootSqlQueries(String host, Partition partition) {
            // bl: also grant permissions for the global_user to the reputation tables for global views into reputation
            List<String> queries = new LinkedList<>();
            // bl: the reputation database name is currently hard-coded
            queries.add("GRANT SELECT ON TABLE `reputation`.CurrentReputation TO '" + partition.getUsername() + "'@'" + host + "'");
            queries.add("GRANT SELECT ON TABLE `reputation`.ReputationHistory TO '" + partition.getUsername() + "'@'" + host + "'");
            return queries;
        }
    },
    COMPOSITION("COMPOSITION", false, 1);

    private static final NetworkLogger logger = new NetworkLogger(PartitionType.class);
    public static final String ENUM_FIELD_TYPE = "enum('GLOBAL','COMPOSITION','UTILITY')";

    private final String idStr;
    private final boolean isSingleton;
    private Partition singletonPartition;
    private DatabaseType databaseType;
    private final Integer commitOrder;

    private PartitionType(String idStr, boolean singleton, int commitOrder) {
        this.idStr = idStr;
        isSingleton = singleton;
        this.commitOrder = commitOrder;
    }

    public static final List<PartitionType> ACTIVE_PARTITION_TYPES = Collections.unmodifiableList(new ArrayList<>(EnumSet.allOf(PartitionType.class)));

    @Override
    public String getIdStr() {
        return idStr;
    }

    /**
     * Determines the order in which open sessions will be stored.  Impacts things like commit order, flushing order, etc
     *
     * @return
     */
    public Integer getCommitOrder() {
        return commitOrder;
    }

    private NetworkAnnotationConfiguration configuration = null;
    private final GSessionFactory GSessionFactory = new GSessionFactory(this);
    private PartitionBalancer partitionBalancer = null;
    private final Map<String, String> namedSQLCommands = new HashMap<String, String>();
    private final List<String> installSQLCommandNames = new LinkedList<String>();
    private final List<String> installSQLCommands = new LinkedList<String>();

    private final ThreadLocal<OID> currentPartitionOid = new ThreadLocal<OID>();
//    private static final ThreadLocal<Map<Partition,Collection<GSession>>> partitionToCurrentOpenSessionsToCloseOnEndOfThread = new ThreadLocal<Map<Partition,Collection<GSession>>>();

    private static final Set<PartitionType> SINGLETON_PARTITION_TYPES;

    static {
        // bl: we use this list of singleton partition types during installation,
        // so we want to make sure to use the proper ordering so that the global
        // database is created first.
        Set<PartitionType> singletonTypes = newLinkedHashSet();
        for (PartitionType partitionType : PartitionType.ACTIVE_PARTITION_TYPES) {
            if (partitionType.isSingleton()) {
                singletonTypes.add(partitionType);
            }
        }
        SINGLETON_PARTITION_TYPES = Collections.unmodifiableSet(singletonTypes);
    }

    static {
        IPUtil.EndOfX.endOfApp.addRunnable("99CloseSessionFactories", new Runnable() {
            public void run() {
                synchronized (PartitionType.class) {
                    for (PartitionType partitionType : values()) {
                        partitionType.GSessionFactory.close();
                    }
                }
            }
        });
    }

    public static Set<PartitionType> getSingletonPartitionTypes() {
        return SINGLETON_PARTITION_TYPES;
    }

    public static final String PARTITION_TYPE_PROPERTY = PartitionType.class.getName() + "-PartitionType";

    private static final String START_DELIM = "[[";
    private static final String END_DELIM = "]]";

    //pm:this shouldn't be needed anymore because it shoudn't be possible to leave these open
//    static {
//        IPUtil.EndOfX.endOfThread.addRunnable("00SessionClosedChecker", new Runnable() {
//            public void run() {
//                // bl: this is here just to prevent coding errors.  any time something uses the ability to leave a session open,
//                // they must correspondingly close the open sessions with PartitionType.closeAllOpenSessions().  PartitionType
//                // can not handle closing sessions here since it can't know whether or not to rollback the transaction
//                // in the event that there was an error during execution of the request.
//                if(partitionToCurrentOpenSessionsToCloseOnEndOfThread.get()!=null) {
//                    String message = "If you are going to leave sessions open in PartitionType.doTask(), then you must correspondingly close them at the end of your operation via PartitionType.closeAllOpenSessions()!  Coding error.";
//                    // in development, we should raise an AssertionError in this case.
//                    assert false : message;
//                    // in production, we at least need to log this message.
//                    logger.error( message);
//                }
//
//                if (currentPartitionGroup.get() != null) {
//                    String message = "current partition group not cleaned up properly.  Coding Error.";
//                    assert false : message;
//                    // in production, we at least need to log this message.
//                    logger.error( message);
//                }
//            }
//        });
//    }

    private boolean init = false;

    public boolean isInit() {
        return init;
    }

    public synchronized void init(boolean isPatching, boolean isInstalling, Partition singletonPartition) {
        if (init) {
            return;
        }

        Map<String, String> allNamedSqlMap = newHashMap();
        {
            // bl: adding a all.named.sql file that works across all partition types.
            InputStream is = Partition.class.getResourceAsStream("/sql/all.named.sql");
            List<NamedSql> namedSqlList = getNamedSqlList(is);
            for (NamedSql namedSql : namedSqlList) {
                allNamedSqlMap.put(namedSql.getName(), namedSql.getSql());
            }
        }

        {
            InputStream is = Partition.class.getResourceAsStream("/sql/" + name().toLowerCase() + ".named.sql");
            addNamedSQL(is, false);
        }

        if (isPatching) {
            InputStream is2 = Partition.class.getResourceAsStream("/sql/" + name().toLowerCase() + ".patch.named.sql");
            addNamedSQL(is2, false);
        }

        // bl: make the install named SQL available to both patch runner and network install
        if (isPatching || isInstalling) {
            InputStream is2 = Partition.class.getResourceAsStream("/sql/" + name().toLowerCase() + ".install.named.sql");
            addNamedSQL(is2, true);

            addNamedSQL(getFunctions(), false);

            // once we've loaded them all, resolve any cross-reference named queries.
            // named queries can invoke other named queries by putting their SQL string as:
            // --NAMED_SQL_TO_RUN--
            for (Map.Entry<String, String> entry : namedSQLCommands.entrySet()) {
                String queryName = entry.getKey();
                String sql = entry.getValue();
                if (sql.startsWith("--") && sql.endsWith("--")) {
                    String referencedQueryName = sql.substring(2, sql.length() - 2);
                    // bl: also support lookups in the all.named.sql file
                    String queryToReplace = allNamedSqlMap.get(referencedQueryName);
                    if (IPStringUtil.isEmpty(queryToReplace)) {
                        queryToReplace = namedSQLCommands.get(referencedQueryName);
                        if (IPStringUtil.isEmpty(queryToReplace)) {
                            throw UnexpectedError.getRuntimeException(queryName + " mapped to " + referencedQueryName + " but the referenced query name could not be found!", true);
                        }
                    }
                    entry.setValue(queryToReplace);
                }
            }
            if (isInstalling) {
                // if installing, we need the list of install SQL commands, so
                // translate them from the query names to the queries themselves.
                for (String installSQLCommandName : installSQLCommandNames) {
                    installSQLCommands.add(namedSQLCommands.get(installSQLCommandName));
                }
            }
        }

        if (singletonPartition != null) {
            assert isSingleton() : "Can't attempt to set singletonPartition for non-singleton PartitionType! " + this;
            this.singletonPartition = singletonPartition;
        } else {
            assert !isSingleton() : "Must supply the singletonPartition for singleton PartitionTypes! " + this;
        }

        init = true;
    }

    private static final Pattern COMMENT_PATTERN = Pattern.compile("\\#[^\\n]*");

    private void addNamedSQL(InputStream is, boolean isInstallSql) {
        addNamedSQL(getNamedSqlList(is), isInstallSql);
    }

    private void addNamedSQL(List<NamedSql> namedSqlList, boolean isInstallSql) {
        for (NamedSql namedSql : namedSqlList) {
            String name = namedSql.getName();
            if (name.endsWith("*")) {
                name = getNextName(name);
            }
            if (namedSQLCommands.containsKey(name)) {
                throw UnexpectedError.getRuntimeException("Duplicate named sql command " + namedSql.getName() + " for partitionType " + this);
            }
            namedSQLCommands.put(name, namedSql.getSql());
            if (isInstallSql) {
                installSQLCommandNames.add(name);
            }
        }
    }

    private String getNextName(String name) {
        int next = 1;
        name = name.substring(0, name.length() - 1);
        while (true) {
            String newName = name + next;
            if (!namedSQLCommands.containsKey(newName)) {
                return newName;
            }
            next++;
        }
    }

    public List<NamedSql> getFunctions() {
        InputStream is = Partition.class.getResourceAsStream("/sql/" + name().toLowerCase() + ".functions.named.sql");
        List<NamedSql> namedSql = getNamedSqlList(is);

        is = Partition.class.getResourceAsStream("/sql/all.functions.named.sql");
        namedSql.addAll(getNamedSqlList(is));

        return namedSql;
    }

    // jw: lets treat this pattern as a case insensitive literal.
    private static final Pattern ENCRYPTION_DISABLED_PATTERN = Pattern.compile(" ENCRYPTION='N'", Pattern.CASE_INSENSITIVE | Pattern.LITERAL);

    private static List<NamedSql> getNamedSqlList(InputStream is) {
        List<NamedSql> namedSql = new LinkedList<NamedSql>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder commands = new StringBuilder();
        String line;
        try {
            try {
                while ((line = br.readLine()) != null) {
                    // bl: allow comments starting with a #
                    line = COMMENT_PATTERN.matcher(line).replaceAll("");
                    commands.append(line).append("\n");
                }
            } finally {
                br.close();
            }
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to get named sql file", e);
        }

        String c = commands.toString();
        int nameStart = c.indexOf(START_DELIM);
        while (nameStart != -1) {
            nameStart += START_DELIM.length();
            int nameEnd = c.indexOf(END_DELIM, nameStart);
            if (nameEnd == -1) {
                throw UnexpectedError.getRuntimeException("No matching ]] found for opening [[ near char " + nameStart);
            }
            String name = c.substring(nameStart, nameEnd);

            int commandStart = nameEnd + END_DELIM.length();
            int commandEnd = c.indexOf(START_DELIM, commandStart);
            String sql;
            if (commandEnd == -1) {
                sql = c.substring(commandStart);
            } else {
                sql = c.substring(commandStart, commandEnd);
            }
            sql = sql.trim();

            // jw: We will want to ensure that any create table statements on encrypted environments include the ENCRYPTION='Y' flag at the end.

            // jw: For create statements, only process if encryption is enabled.
            if (Encryption.INSTANCE.isUseMySqlEncryption()) {
                String lcSql = sql.toLowerCase();

                // jw: lets try and be more flexible with create statements:
                if (lcSql.startsWith("create table ")) {
                    // if the table already defines encryption, lets let them have it, this allows patches to force that a table is not encrypted on encrypted environments.
                    // bl: also do not want to add encryption for MyISAM tables, since they don't support it!
                    if (!lcSql.contains(" encryption=") && !lcSql.contains("engine=myisam")) {
                        // jw: if the sql already ends in a semi-colon, lets truncate it off to make this more consistent.
                        if (sql.endsWith(";")) {
                            // get everything except that last character.
                            sql = sql.substring(0, sql.length() - 1);
                        }

                        // jw: now the easy part, add the ENCRYPTION flag to the SQL.
                        sql += " ENCRYPTION='Y';";
                    }

                    // jw: otherwise, when creating stored procedures, we need to look for any tables that may be getting created with encryption...
                } else if (lcSql.startsWith("create procedure ")) {
                    // jw: if the table has encryption disabled,
                    Matcher matcher = ENCRYPTION_DISABLED_PATTERN.matcher(sql);
                    sql = matcher.replaceAll(" ENCRYPTION='Y'");
                }
            }

            namedSql.add(new NamedSql(name, sql));
            nameStart = commandEnd;
        }
        return namedSql;
    }

    /**
     * Returns a random partition oid, balanced based on the relative weights of all the partitions.
     *
     * @return The partition oid to use
     */
    public OID getBalancedPartitionOID() {

        //get a local copy of the balancer
        PartitionBalancer localBalancer = partitionBalancer;
        if (localBalancer == null) {

            //if our local copy is null, then sync and check if we still need to create a new balancer
            synchronized (this) {
                localBalancer = partitionBalancer;
                if (localBalancer == null) {

                    //we still need to create it so do so, then set the local copy
                    partitionBalancer = new PartitionBalancer(this);
                    localBalancer = partitionBalancer;
                }
            }
        }
        return localBalancer.getNextPartitionOID();
    }

    public Map<String, String> getNamedSQLCommands() {
        return namedSQLCommands;
    }

    public List<String> getInstallSQLCommands() {
        return installSQLCommands;
    }

    public Partition getBalancedPartition() {
        return Partition.dao().get(getBalancedPartitionOID());
    }

    public boolean isSingleton() {
        return isSingleton;
    }

    public Partition getSingletonPartition() {
        assert isSingleton() : "Can't get singleton partition for non-singleton PartitionType! " + this;
        return singletonPartition;
    }

    /**
     * Clears the blance info, forcing the partitions to re-calcualte it on the next partition oid request.
     */
    void clearBalanceInfo() {
        synchronized (this) {
            partitionBalancer = null;
        }
    }

    public void setConfiguration(NetworkAnnotationConfiguration configuration) {
        this.configuration = configuration;
        // bl: set the cache key provider to be PartitionCacheKeyProvider.  this will ensure
        // that the current partition oid is included in all hibernate cache keys, where appropriate.

        // bl: set the partition type property so that the PartitionTypeCacheKeyProvider can
        // know which partition oid to append to the cache key.
        configuration.setProperty(PARTITION_TYPE_PROPERTY, name());
        configuration.setProperty(Environment.CONNECTION_PROVIDER, PartitionTypeConnectionProvider.class.getName());

        GSessionFactory.init(configuration);
    }

    public NetworkAnnotationConfiguration getConfiguration() {
        assert configuration != null : "Can't get the configuration without first setting it!";
        return configuration;
    }

    public GSessionFactory getGSessionFactory() {
        assert GSessionFactory != null : "Can't get a GSessionFactory without first setting the annotation configuration!";
        return GSessionFactory;
    }

    public boolean hasCurrentSession() {
        return getGSessionFactory().hasCurrentSession();
    }

    @NotNull
    public GSession currentSession() {
        return getGSessionFactory().getCurrentSession();
    }

    public boolean hasCurrentPartitionOid() {
        return currentPartitionOid.get() != null;
    }

    @NotNull
    public OID currentPartitionOid() {
        OID ret = currentPartitionOid.get();
        assert ret != null : "Can't call currentPartitionOid for this partition unless it has already been set! pt/" + this;
        return ret;
    }

    @NotNull
    public Partition currentPartition() {
        return Partition.dao().get(currentPartitionOid());
    }

    public String getDisplayName() {
        return wordlet("partitionType." + this);
    }

    public boolean isGlobal() {
        return this == GLOBAL;
    }

    public boolean isComposition() {
        return this == COMPOSITION;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * Add support for new DB types here
     *
     * @param dialectClassName The dialect class name
     */
    public void setDatabaseType(String dialectClassName) {
        if (dialectClassName.toLowerCase().contains("mysql")) {
            databaseType = DatabaseType.MYSQL;
        } else {
            throw new IllegalArgumentException("Only MySQL is currently supported");
        }
    }

    public <T> T doTask(Partition partition, TaskOptions taskOptions, PartitionTask<T> task) {
        assert partition.getPartitionType() == this : "Partition type of the partition send in must match this partition type";

        if (taskOptions.getTaskIsolationLevel() != TaskIsolationLevel.NOT_ISOLATED && task.isForceNoIsolation()) {
            throw UnexpectedError.getRuntimeException("This task requires NOT_ISOLATED isolation level, and it is being run as " + taskOptions.getTaskIsolationLevel());
        }

        final NetworkContextImplBase originalNetworkContext = isNetworkContextSet() ? NetworkContextImplBase.current() : null;

        NetworkContextImplBase networkContext;
        if (taskOptions.getNetworkContextToUse() != null) {
            networkContext = (NetworkContextImplBase) taskOptions.getNetworkContextToUse();
        } else if (originalNetworkContext != null) {
            networkContext = originalNetworkContext;
        } else {
            networkContext = new NetworkContextImpl();
        }

        // set the network context on the network task, if applicable
        if (task instanceof NetworkTaskImpl) {
            ((NetworkTaskImpl) task).doSetNetworkContext(networkContext);
        }

        // set the area context on the area task, if applicable
        if (task instanceof AreaTaskImpl) {
            assert networkContext instanceof AreaContext : "Doing an area task, but the context used was not an AreaContext! cls/" + networkContext.getClass().getName();
            ((AreaTaskImpl) task).doSetAreaContext((AreaContext) networkContext);
        }

        //get the old session first
        GSession oldGSession = null;
        OID oldPartitionOid = currentPartitionOid.get();
        if (GSessionFactory.hasCurrentSession()) {
            oldGSession = currentSession();
        }

        // set the current context
        NetworkContextImplBase.setCurrentContext(networkContext);

        try {
            //try to use the old current session if at all possible
            if (!taskOptions.getTaskIsolationLevel().isIsolated() && oldGSession != null && IPUtil.isEqual(oldPartitionOid, partition.getOid())) {
                // if this task must be writable, then we need to ensure that the current session is not read only.
                // it's a coding error if you try to run a force-writable task in the context of a read-only session.
                assert !task.isForceWritable() || !oldGSession.isReadOnly() : "Writable tasks cannot be run within the same session as a read only task";
                // bl: disabling the upgrade process.  we no longer want to ever allow writable tasks to be run
                // inside of a read-only task.  this can lead to unexpected behavior.
                // bl: upgrade the old read-only task to being writable instead of giving an error to allow for this scenario.
                /*if(task.isForceWritable() && oldGSession.isReadOnly()) {
                    oldGSession.setReadOnly(false);
                }*/

                return doTaskWithNonForceWritableWrapper(task);
            }

            //could not use the current session, so set up the current partition group.  If one doesn't exist yet,
            //or this requries isolation, create a new one
            boolean rootOfPartitionGroup = false;
            PartitionGroup oldPartitionGroup = PartitionGroup.currentPartitionGroup();
            Map<PartitionType, ObjectPair<GSession, OID>> oldThreadLocalState = new HashMap<PartitionType, ObjectPair<GSession, OID>>();
            if (oldPartitionGroup == null || taskOptions.getTaskIsolationLevel().isIsolated()) {
                if (!taskOptions.getTaskIsolationLevel().isKeepingCurrentSessions()) {
                    extractAndClearCurrentPartitionSessions(oldThreadLocalState);
                }
                PartitionGroup.setCurrentPartitionGroup(new PartitionGroup());
                rootOfPartitionGroup = true;
                // bl: if it's not a force writeable task, then let's set the database/hibernate sessions as read only!
                if (!task.isForceWritable()) {
                    PartitionGroup.getCurrentPartitionGroup().setReadOnly(true);
                }
            }

            final PartitionKey partitionKey = partition.getPartitionKey();
            GSession gSession;
            TransactionStats stats = new TransactionStats();
            T retVal;
            try {

                //see if an existing session exists in the partition group
                gSession = PartitionGroup.getCurrentPartitionGroup().getOpenSessions().get(partitionKey);

                // if we haven't found a GSession yet, then open a new one
                if (gSession == null) {
                    //open the session and set it
                    gSession = partitionKey.openSession();

                    // if we opened a new session, then set to read only if the PartitionGroup is read-only
                    if (PartitionGroup.getCurrentPartitionGroup().isReadOnly()) {
                        assert !task.isForceWritable() : "Can't execute a force-writable task in a read-only PartitionGroup!";
                        gSession.setReadOnly(true);
                    }

                    //add the session to the current partition group
                    PartitionGroup.getCurrentPartitionGroup().getOpenSessions().put(partitionKey, gSession);
                    PartitionGroup.getCurrentPartitionGroup().addPartitionWithOpenSession(partition);
                } else {
                    assert !task.isForceWritable() || !gSession.isReadOnly() : "Writable tasks cannot be run within the same session as a read only task";
                    // bl: disabling the upgrade process.  we no longer want to ever allow writable tasks to be run
                    // inside of a read-only task.  this can lead to unexpected behavior.
                    // bl: upgrade the old read-only task to being writable instead of giving an error to allow for this scenario.
                    /*if(task.isForceWritable() && gSession.isReadOnly()) {
                        gSession.setReadOnly(false);
                    }*/
                }

                // always need to set the thread locals for the current session and partition oid
                GSessionFactory.setCurrentSession(gSession);
                currentPartitionOid.set(partition.getOid());

                retVal = doTaskWithNonForceWritableWrapper(task);

                //all went well, so commit if necessary
                if (rootOfPartitionGroup) {

                    //first try to flush all sessions in the partition group, in case we get an error
                    // bl: we may have already tried to flush the session and failed, in which case there is
                    // no point in trying again.  thus, only try to flush if the partition group is not in error.
                    // just like we do in the TransactionAndSessionInterceptor.
                    if (!PartitionGroup.getCurrentPartitionGroup().isInError()) {
                        // bl: flush first, then process the pre-commit runnables, and then commit the transaction.
                        // this way, the first flush will get almost everything out into the db, then we will run
                        // the pre-commit runnables, and commit immediately after.  the commit should only have
                        // to flush whatever changes were made (if any) in the pre-commit runnables.
                        flushAllOpenSessionsForCurrentPartitionGroup();
                        // bl: for even better performance with GlobalStats, we could save running these until
                        // right before we actually commit the global transaction (after we commit all realm/comp transactions),
                        // but the issue there is if we get some kind of exception with the GlobalStats, then
                        // the realm/comp transactions would have already been committed, thus leading to data
                        // inconsistencies between partitions.  for now, leaving here until we determine that even
                        // this is an insufficient means to update GlobalStats.  ideally, the committing of the
                        // transactions in the realm/comp databases should happen very quickly anyway.
                        PartitionGroup.getCurrentPartitionGroup().processPreCommitRunnables();
                    }

                    stats.totalTransactions = PartitionGroup.getCurrentPartitionGroup().getOpenSessions().size();

                    //now commit or rollback all transactions
                    for (GSession sess : PartitionGroup.getCurrentPartitionGroup().getOpenSessions().values()) {
                        doSessionTask(sess, () -> {
                            // if we're not leaving the session/transaction open, then go ahead and commit it
                            if (PartitionGroup.getCurrentPartitionGroup().isInError()) {
                                // first off, if this thread is in error, then let's rollback this transaction
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Rolling back transaction " + (sess.isInTransaction() ? sess.getTransaction().getId() : null));
                                }
                                sess.rollbackTransaction();
                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Committing transaction " + (sess.isInTransaction() ? sess.getTransaction().getId() : null));
                                }

                                sess.commitTransaction();
                                stats.committedTransactions++;
                            }
                        });
                    }
                } else {
                    // bl: allow tasks to be registered for flushing. this will allow us to flush tasks
                    // that are not at the root of the PartitionGroup when they complete. useful for AreaTasks
                    // that we will want to flush as part of before exiting.
                    PartitionGroup.getCurrentPartitionGroup().flushAllSessionsIfNecessaryForTask(task);
                }

                return retVal;
            } catch (Throwable t) {

                boolean someCommitted = stats.committedTransactions > 0;
                boolean allCommitted = stats.committedTransactions == stats.totalTransactions;

                //if some transactions committed but not all, this is VERY bad so lets log the heck out of it
                if (someCommitted && !allCommitted) {
                    logger.error("Some transactions committed (" + stats.committedTransactions + "), but not all (" + stats.totalTransactions + ")!  This most likely will lead to a database inconsistency", t);
                }

                //see if this task handles the exception gracefully
                PartitionDownErrorReturnValue<T> ret;
                try {
                    ret = task.handlePartitionDownError(partition, t);
                } catch (Throwable t2) {
                    // bl: log the partition down error, but throw the exception with the original
                    logger.error("Failed handling partition down error due to another exception. Original error: " + Debug.stackTraceFromException(t), t2);
                    throw UnexpectedError.getRuntimeException("Failed handling partition down error.  Going to report the original error.  Partition down error: " + Debug.stackTraceFromException(t2), t, true);
                }

                // mark this thread as being in error so that other transactions can be rolled back
                if (!ret.isHandled()) {
                    PartitionGroup.getCurrentPartitionGroup().setInError(true);

                    //if we are the root of the partition group, then rollback all active transactions
                    if (rootOfPartitionGroup) {
                        for (GSession sess : PartitionGroup.getCurrentPartitionGroup().getOpenSessions().values()) {
                            doSessionTask(sess, () -> {
                                try {
                                    sess.rollbackTransaction();
                                } catch (Throwable e) {
                                    logger.error("Error rolling back transation.  Continuing on to rollback any other transactions", e);
                                }
                            });
                        }
                    }
                }

                // bl: we want to record/log the wrapped exception in order to ensure that any extra logging information
                // e.g. InvalidStateException info, will be included in the exception.  logging just the original exception
                // will not result in this behavior.
                NarrativeException wrappedException = UnexpectedError.getRuntimeException("Failed executing task/" + task.getClass().getName() + " on partition/" + partition, t);

                // bl: only record exceptions at the root of the partition group
                if (rootOfPartitionGroup && !taskOptions.isBypassErrorStatisticRecording()) {
                    StatisticManager.recordException(wrappedException, false, null);
                }

                if (ret.isHandled()) {
                    logger.error("Partition Down Error Handled for partition: " + partition.getDisplayName() + " for task: " + task.getClass(), wrappedException);
                    return ret.getReturnValue();
                }

                if (someCommitted && !allCommitted) {
                    throw new PartialTransactionCommitException("Partial transaction commit!  " + stats.committedTransactions + "/" + stats.totalTransactions + " transactions committed.  task/" + task.getClass().getName() + " on partition/" + partition, wrappedException);
                }

                throw wrappedException;
            } finally {
                // bl: clear out all of the thread locals so that the partition group runnables can do their own root network tasks.
                GSessionFactory.setCurrentSession(null);
                currentPartitionOid.set(null);

                PartitionGroup lastPartitionGroup = PartitionGroup.currentPartitionGroup();
                PartitionGroup.setCurrentPartitionGroup(null);

                // clear the NetworkContext so that it is not set during execution of end of partition group runnables.
                NetworkContextImpl.clearCurrent();

                try {
                    //if we are the root of the partition group, then close all partitions
                    try {
                        if (rootOfPartitionGroup) {
                            for (Map.Entry<PartitionKey, GSession> entry : lastPartitionGroup.getOpenSessions().entrySet()) {
                                doSessionTask(entry.getValue(), () -> {
                                    try {
                                        entry.getKey().closeSession(entry.getValue());
                                    } catch (Throwable e) {
                                        logger.error("Error closing transaction.  Continuing on to close any other transactions", e);
                                    }
                                });
                            }

                            //add the partition group to the list of partition groups for this thread
                            lastPartitionGroup.getOpenSessions().clear();
                            lastPartitionGroup.getPartitionTypeToPartitionWithOpenSessions().clear();

                            lastPartitionGroup.processRunnables();
                        }
                    } finally {
                        if (rootOfPartitionGroup && !taskOptions.getTaskIsolationLevel().isKeepingCurrentSessions()) {
                            // since we're now at the root of the partition group, restore the thread locals for all of
                            // the different partition types to their previous state.
                            replacePartitionSessions(oldThreadLocalState);
                        } else {
                            // not yet at the root of the partition group, so set the thread locals back to their original values
                            GSessionFactory.setCurrentSession(oldGSession);
                            currentPartitionOid.set(oldPartitionOid);
                        }
                    }

                } finally {
                    PartitionGroup.setCurrentPartitionGroup(oldPartitionGroup);
                }
            }
        } finally {
            NetworkContextImpl.setCurrentContext(originalNetworkContext);
        }
    }

    private static class TransactionStats {
        int committedTransactions = 0;
        int totalTransactions = 0;
    }

    public static void doSessionTask(GSession sess, Runnable runnable) {
        sess.getGSessionFactory().getPartitionType().doSessionTask(sess.getPartitionOid(), runnable);
    }

    public void doSessionTask(OID partitionOid, Runnable runnable) {
        OID originalPartitionOid = currentPartitionOid.get();
        try {
            currentPartitionOid.set(partitionOid);
            runnable.run();
        } finally {
            currentPartitionOid.set(originalPartitionOid);
        }
    }

    private <T> T doTaskWithNonForceWritableWrapper(PartitionTask<T> task) {
        boolean oldIsInNonForceWritableTask = PartitionGroup.getCurrentPartitionGroup().isInNonForceWritableTask();
        try {
            PartitionGroup.getCurrentPartitionGroup().setInNonForceWritableTask(!task.isForceWritable());
            //do the deed
            return task.doTask();
        } finally {
            PartitionGroup.getCurrentPartitionGroup().setInNonForceWritableTask(oldIsInNonForceWritableTask);
        }
    }

    static void replacePartitionSessions(Map<PartitionType, ObjectPair<GSession, OID>> oldThreadLocalState) {
        for (Map.Entry<PartitionType, ObjectPair<GSession, OID>> entry : oldThreadLocalState.entrySet()) {
            PartitionType partitionType = entry.getKey();
            ObjectPair<GSession, OID> values = entry.getValue();
            partitionType.getGSessionFactory().setCurrentSession(values.getOne());
            partitionType.currentPartitionOid.set(values.getTwo());
        }
    }

    static void extractAndClearCurrentPartitionSessions(Map<PartitionType, ObjectPair<GSession, OID>> oldThreadLocalState) {
        oldThreadLocalState.putAll(extractCurrentPartitionSessions());
        clearCurrentPartitionSessions();
    }

    static Map<PartitionType, ObjectPair<GSession, OID>> extractCurrentPartitionSessions() {
        Map<PartitionType, ObjectPair<GSession, OID>> ret = newHashMap();
        for (PartitionType partitionType : PartitionType.ACTIVE_PARTITION_TYPES) {
            GSession typeSession = partitionType.hasCurrentSession() ? partitionType.currentSession() : null;
            OID typePartitionOid = partitionType.hasCurrentPartitionOid() ? partitionType.currentPartitionOid() : null;
            ret.put(partitionType, new ObjectPair<GSession, OID>(typeSession, typePartitionOid));
        }
        return ret;
    }

    static void clearCurrentPartitionSessions() {
        for (PartitionType partitionType : PartitionType.ACTIVE_PARTITION_TYPES) {
            // clear out all of the ThreadLocals
            partitionType.getGSessionFactory().setCurrentSession(null);
            partitionType.currentPartitionOid.set(null);
        }
    }

    public <T> void doTaskInAllPartitionsOfThisType(TaskOptions taskOptions, AllPartitionsTask<T> partitionTask) {
        Collection<Partition> partitions = Partition.dao().getAllForType(this);
        for (Partition partition : partitions) {
            partitionTask.setCurrentPartition(partition);
            doTask(partition, taskOptions, partitionTask);
        }
    }

//pm:this has been replaces by the partitionGroups concept
//    private static Collection<GSession> getOpenSessions(Partition partition) {
//        Map<Partition,Collection<GSession>> partitionToGSessions = partitionToCurrentOpenSessionsToCloseOnEndOfThread.get();
//        if(partitionToGSessions!=null) {
//            return partitionToGSessions.get(partition);
//        }
//        return null;
//    }

//pm:this has been replaces by the partitionGroups concept
//    private static void addOpenSession(Partition partition, GSession gSession) {
//        Map<Partition,Collection<GSession>> partitionToOpenSessions = partitionToCurrentOpenSessionsToCloseOnEndOfThread.get();
//        if(partitionToOpenSessions==null) {
//            partitionToCurrentOpenSessionsToCloseOnEndOfThread.set(partitionToOpenSessions = new HashMap<Partition,Collection<GSession>>());
//        }
//        Collection<GSession> openSessions = partitionToOpenSessions.get(partition);
//        if(openSessions==null) {
//            partitionToOpenSessions.put(partition, openSessions = new HashSet<GSession>());
//        }
//        openSessions.add(gSession);
//    }

//pm:this has been replaces by the partitionGroups concept
//    public static void closeAllOpenSessions(boolean rollbackTransactions) {
//        Map<Partition,Collection<GSession>> partitionToOpenSessions = partitionToCurrentOpenSessionsToCloseOnEndOfThread.get();
//        if(partitionToOpenSessions!=null) {
//            Throwable t = null;
//            for (Map.Entry<Partition, Collection<GSession>> entry : partitionToOpenSessions.entrySet()) {
//                Partition partition = entry.getKey();
//                for (GSession gSession : entry.getValue()) {
//                    // in the event of an exception, always rollback the transaction first.
//                    // note that the logic here pretty closely mimics the logic in PartitionType.doTask().
//                    // the biggest difference is that there aren't any thread locals to reset here.
//                    try {
//                        try {
//                            // rollback transactions if this thread is in error, too.
//                            if(rollbackTransactions || IPUtil.isThreadInError()) {
//                                gSession.rollbackTransaction();
//                            } else {
//                                gSession.commitTransaction();
//                            }
//                        } finally {
//                            partition.closeSession(gSession);
//                        }
//                    } catch(Throwable t1) {
//                        // mark this thread as being in error so that other transactions will be rolled back appropriately.
//                        IPUtil.setThreadInError(true);
//                        t = t1;
//                        logger.error( "Failed closing session", t1);
//                    }
//
//                }
//            }
//
//            // bl: reset the thread local here.  then, in onEndOfThread we can check
//            // to see if there are any open sessions, and if there are, we can assert since
//            // that is a coding error.
//            partitionToCurrentOpenSessionsToCloseOnEndOfThread.set(null);
//
//            if(t!=null) {
//                throw UnexpectedError.getRuntimeException("Failed closing open sessions", t, true);
//            }
//        }
//    }

    /**
     * attempt to flush all _open_ sessions.  this is different from active sessions since
     * flushing all active sessions will only flush the sessions set on the thread local,
     * which is limited to one per partition type.
     */
    public static void flushAllOpenSessionsForCurrentPartitionGroup() {
        PartitionGroup.getCurrentPartitionGroup().flushAllSessions();
    }

    /**
     * Get the URL for the baseline schema for this partition type
     *
     * @return {@link URL} for this partition type
     */
    public URL getSchemaBaselineScriptURL() {
        return getClass().getResource("/schema/baseline/" + name().toLowerCase() + "_baseline.sql");
    }

    public List<String> getExtraInstallRootSqlQueries(String host, Partition partition) {
        // return nothing by default
        return Collections.emptyList();
    }
}
