package org.narrative.network.core.quartz.services;

import org.narrative.network.core.cluster.partition.HighPriorityRunnable;
import org.narrative.network.core.cluster.partition.PartitionConnectionPool;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.quartz.JobPersistenceException;
import org.quartz.impl.jdbcjobstore.JobStoreSupport;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * We have our own in order to handle the db transaction in our layer vs. quartz's
 * <p/>
 * User: barry
 * Date: Mar 17, 2010
 * Time: 9:51:18 AM
 */
public class GJobStoreTX extends JobStoreSupport {

    @Override
    protected <T> T executeInLock(String lockName, TransactionCallback<T> txCallback) throws JobPersistenceException {
        boolean transOwner = false;
        Connection conn = null;
        try {
            if (lockName != null) {
                // If we aren't using db locks, then delay getting DB connection
                // until after acquiring the lock since it isn't needed.
                if (getLockHandler().requiresConnection()) {
                    conn = getConnection();
                }

                transOwner = getLockHandler().obtainLock(conn, lockName);
            }

            if (conn == null) {
                conn = getConnection();
            }

            final T result = txCallback.execute(conn);
            /*try {
                commitConnection(conn);
            } catch (JobPersistenceException e) {
                rollbackConnection(conn);
                if (txValidator == null || !retryExecuteInNonManagedTXLock(lockName, new TransactionCallback<Boolean>() {
                    @Override
                    public Boolean execute(Connection conn) throws JobPersistenceException {
                        return txValidator.validate(conn, result);
                    }
                })) {
                    throw e;
                }
            }*/

            // bl: we don't want to release the signal the scheduler until this transaction has actually committed,
            // so wait until the end of partition group to do this bit.
            PartitionGroup.getCurrentPartitionGroup().addEndOfGroupRunnableForSuccessOrError(new HighPriorityRunnable() {
                @Override
                public void run() {
                    Long sigTime = clearAndGetSignalSchedulingChangeOnTxCompletion();
                    if (sigTime != null && sigTime >= 0) {
                        signalSchedulingChangeImmediately(sigTime);
                    }
                }
            });

            return result;
        }/* catch (JobPersistenceException e) {
            rollbackConnection(conn);
            throw e;
        } catch (RuntimeException e) {
            rollbackConnection(conn);
            throw new JobPersistenceException("Unexpected runtime exception: "
                    + e.getMessage(), e);
        }*/ finally {
            try {
                releaseLock(lockName, transOwner);
            } finally {
                //cleanupConnection(conn);
            }
        }
    }

    @Override
    protected Connection getNonManagedTXConnection() throws JobPersistenceException {
        Connection conn;
        try {
            PartitionConnectionPool pool = PartitionConnectionPool.getPartitionConnectionPool(PartitionType.GLOBAL.getSingletonPartition());
            conn = pool.getDataSource().getConnection();
        } catch (SQLException sqle) {
            throw new JobPersistenceException("Failed to obtain DB connection from data source GDataSource : " + sqle.toString(), sqle);
        } catch (Throwable e) {
            throw new JobPersistenceException("Failed to obtain DB connection from data source GDataSource : " + e.toString(), e);
        }

        if (conn == null) {
            throw new JobPersistenceException("Could not get connection from GDataSource ");
        }

        // Protect connection attributes we might change.
        conn = getAttributeRestoringConnection(conn);

        // Set any connection connection attributes we are to override.
        try {
            if (!isDontSetAutoCommitFalse()) {
                conn.setAutoCommit(false);
            }

            conn.setTransactionIsolation(isTxIsolationLevelSerializable() ? Connection.TRANSACTION_SERIALIZABLE : Connection.TRANSACTION_READ_COMMITTED);
        } catch (SQLException sqle) {
            getLog().warn("Failed to override connection auto commit/transaction isolation.", sqle);
        } catch (Throwable e) {
            try {
                conn.close();
            } catch (Throwable ignored) {
            }

            throw new JobPersistenceException("Failure setting up connection.", e);
        }

        return conn;
    }
}