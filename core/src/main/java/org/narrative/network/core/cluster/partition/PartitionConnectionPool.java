package org.narrative.network.core.cluster.partition;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.DatabaseResources;
import org.narrative.common.persistence.hibernate.GDataSource;
import org.narrative.common.util.IPUtil;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 28, 2005
 * Time: 1:06:34 AM
 */
public class PartitionConnectionPool {

    private static final NetworkLogger logger = new NetworkLogger(PartitionConnectionPool.class);

    public static final Map<OID, PartitionConnectionPool> pools = newHashMap();
    public static final Map<PartitionType, Set<OID>> partitionTypeToPartitionOids = newHashMap();

    public static void initGlobalPartitionConnectionPool() {
        registerPartitionConnectionPool(PartitionType.GLOBAL.getSingletonPartition());
    }

    public static void initPartitionConnectionPools() {
        assert partitionTypeToPartitionOids.containsKey(PartitionType.GLOBAL) : "Should only attempt to initialize the PartitionConnectionPools after first initializing the global partition!";
        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
            @Override
            protected Object doMonitoredTask() {
                List<Partition> partitions = Partition.dao().getAll();
                for (Partition partition : partitions) {
                    // bl: don't need to register the global partition, as it should have already been initialized.
                    if (partition.getPartitionType().isGlobal()) {
                        continue;
                    }
                    registerPartitionConnectionPool(partition);
                }
                return null;
            }
        });
    }

    public static DatabaseResources getOneOffDatabaseResources(Partition partition) {
        GDataSource dataSource = getGDataSourceForPartition(partition);

        DatabaseResources ret = new DatabaseResources(dataSource, partition.getPartitionType().getNamedSQLCommands());
        final DatabaseResources databaseResources = ret;
        IPUtil.EndOfX.temporaryEndOfThreadThreadLocal.getEndOfX().addRunnable("CleanupDatabaseResources", new Runnable() {
            @Override
            public void run() {
                // free the resources for this DatabaseResources object
                databaseResources.freeResources();
            }
        });
        return ret;
    }

    public static PartitionConnectionPool registerPartitionConnectionPool(Partition partition) {
        GDataSource dataSource = getGDataSourceForPartition(partition);

        PartitionConnectionPool pcp = new PartitionConnectionPool(dataSource, partition);
        pools.put(partition.getOid(), pcp);
        Set<OID> partitionOids = partitionTypeToPartitionOids.get(partition.getPartitionType());
        if (partitionOids == null) {
            partitionTypeToPartitionOids.put(partition.getPartitionType(), partitionOids = newHashSet());
        }
        partitionOids.add(partition.getOid());
        return pcp;
    }

    private static GDataSource getGDataSourceForPartition(Partition partition) {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setJdbcUrl(partition.getDatabaseJdbcUrl());
        // bl: shouldn't need to set the MySQL driver class since it should have already been pre-loaded elsewhere,
        // in which case c3p0 should be able to obtain the driver class from JDBC's DriverManager.
        //dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setUser(partition.getUsername());
        dataSource.setPassword(partition.getPassword());
        dataSource.setDataSourceName(partition.toString());
        // get 10 connections at a time when we run out
        dataSource.setAcquireIncrement(10);
        // start with 10 connections
        dataSource.setInitialPoolSize(10);
        // allow at most 100 connections
        if (partition.getPartitionType().isGlobal()) {
            dataSource.setMaxPoolSize(100);
        } else {
            dataSource.setMaxPoolSize(50);
        }
        // just like we did in ConnectionPool, test the connection with a "select 1" query
        dataSource.setPreferredTestQuery("select 1");
        // just like we did in ConnectionPool, test the connection on every checkout.
        // NOTE: c3p0 documentation recommends AGAINST doing this.  instead, they recommend
        // that we use the idleConnectionTestPeriod to test idle, non-checked out connections
        // at a specified interval (in seconds).
        // from their documenation:
        /*  The most reliable time to test Connections is on check-out. But this is also the most costly choice from a
            client-performance perspective. Most applications should work quite reliably using a combination of idleConnectionTestPeriod
            and testConnectionsOnCheckIn. Both the idle test and the check-in test are performed asynchronously, which leads
            to better performance, both perceived and actual.
            Note that for many applications, high performance is more important than the risk of an occasional database
            exception. In its default configuration, c3p0 does no Connection testing at all. Setting a fairly long
            idleConnectionTestPeriod, and not testing on checkout and check-in at all is an excellent, high-performance approach. */
        dataSource.setTestConnectionOnCheckout(true);

        // bl: not enabling PreparedStatement pooling by default.  if we want to enable this or test it out,
        // we can do so via command line arguments:
        // -Dc3p0.maxStatements=10000
        //dataSource.setMaxStatements(100*100);
        // -Dc3p0.maxStatementsPerConnection=100
        //dataSource.setMaxStatementsPerConnection(100);

        // try to acquire new connections every 500ms once the database goes down.
        // retry up to 20 times, for a total of a possible 10 second delay before finally giving up and throwing
        // exceptions on all threads currently attempting to obtain connections.
        dataSource.setAcquireRetryAttempts(20);
        dataSource.setAcquireRetryDelay(500);

        // this is the default behavior, but i thought it was worth setting the flag explicitly here in order to
        // correspond to the fact that this was the behavior in our old ConnectionPool. when closing/releasing
        // a connection, we should rollback the outstanding transaction (if any) on the connection.
        dataSource.setAutoCommitOnClose(false);
        return new GDataSource(dataSource, partition.getOid());
    }

    public static PartitionConnectionPool getPartitionConnectionPool(Partition partition) {
        return getPartitionConnectionPool(partition.getOid());
    }

    public static PartitionConnectionPool getPartitionConnectionPool(OID partitionOid) {
        PartitionConnectionPool pcp = pools.get(partitionOid);
        if (pcp == null) {
            synchronized (pools) {
                pcp = pools.get(partitionOid);
                assert pcp != null : "Should always find the PartitionConnectionPool instance! Was PartitionConnectionPool properly initialized?";
            }
        }
        return pcp;
    }

    private final GDataSource dataSource;
    private final OID partitionOid;
    private final PartitionType partitionType;

    private static final String DATABASE_RESOURCES_PARTITION_GROUP_ID_PREFIX = PartitionConnectionPool.class.getName() + ".databaseResources";

    private PartitionConnectionPool(GDataSource dataSource, Partition partition) {
        this.dataSource = dataSource;
        this.partitionOid = partition.getOid();
        this.partitionType = partition.getPartitionType();
    }

    public DatabaseResources getDatabaseResources() {
        PartitionGroup partitionGroup = PartitionGroup.getCurrentPartitionGroup();
        String partitionGroupPropertyId = DATABASE_RESOURCES_PARTITION_GROUP_ID_PREFIX + partitionOid + partitionType;
        DatabaseResources dbr = partitionGroup.getPartitionGroupProperty(partitionGroupPropertyId);
        if (dbr == null) {
            dbr = new DatabaseResources(dataSource, partitionType.getNamedSQLCommands());
            final DatabaseResources databaseResources = dbr;
            partitionGroup.addPartitionGroupProperty(partitionGroupPropertyId, databaseResources);
            // bl: need this runnable to run at the end of partition group regardless of whether it was
            // for success or error.  otherwise, we would never clean up connections created by DatabaseResources
            // when a process ended up in error.  eventually, this would lead to connection pool exhaustion.
            partitionGroup.addEndOfGroupRunnableForSuccessOrError(new Runnable() {
                public void run() {
                    // free the resources for this DatabaseResources object
                    databaseResources.freeResources();
                }
            });
        }
        return dbr;
    }

    public GDataSource getDataSource() {
        return dataSource;
    }

    public OID getPartitionOid() {
        return partitionOid;
    }

    public PartitionType getPartitionType() {
        return partitionType;
    }
}
