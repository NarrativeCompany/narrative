package org.narrative.network.shared.daobase;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.persistence.hibernate.GSessionFactory;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.shared.util.NetworkLogger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.jetbrains.annotations.NotNull;

import javax.persistence.LockModeType;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 29, 2005
 * Time: 11:29:21 PM
 */
public abstract class NetworkDAOImpl<T extends DAOObject, ID extends Serializable> extends DAOImpl<T, ID> implements NetworkDAO<T, ID> {

    private static final NetworkLogger logger = new NetworkLogger(NetworkDAOImpl.class);

    @NotNull
    private final PartitionType type;

    private static Map<Class, ObjectPair<Integer, Long>> s_classToRowCount = new ConcurrentHashMap<>();
    private static Map<String, ObjectPair<Integer, Long>> s_classWithTypeToRowCount = new ConcurrentHashMap<>();

    protected NetworkDAOImpl(@NotNull PartitionType type, @NotNull Class<T> cls) {
        super(cls);
        this.type = type;
    }

    public T getLocked(ID id) {
        if (id == null) {
            return null;
        }

        T obj = getGSession().getFromSessionCache(id, cls);
        if (obj == null) {
            assert !getGSession().isReadOnly() : "Should never getLocked in a read-only session!";
            if (logger.isDebugEnabled()) {
                logger.debug("locking " + cls.getName() + "#" + id);
            }
            return getGSession().load(getDAOObjectClass(), id, LockMode.PESSIMISTIC_WRITE);
        } else {
            return lock(obj);
        }
    }

    public final Partition getCurrentPartition() {
        return type.currentPartition();
    }

    public final GSession getGSession() {
        return type.currentSession();
    }

    public final GSessionFactory getGSessionFactory() {
        return type.getGSessionFactory();
    }

    public final Session getSession() {
        return getGSession().getSession();
    }

    @NotNull
    public final PartitionType getPartitionType() {
        return type;
    }

    public List<T> getAllForCurrentArea() {
        assert false : "getAllForCurrentArea() must be implemented by the concrete DAO class.";
        return null;
    }

    public void save(T obj) {
        getPartitionType().currentSession().saveObject(obj);
    }

    public void saveOrUpdate(T obj) {
        getPartitionType().currentSession().saveOrUpdateObject(obj);
    }

    public void update(T obj) {
        getPartitionType().currentSession().updateObject(obj);
    }

    public void delete(T obj) {
        getPartitionType().currentSession().deleteObject(obj);
    }

    private LockMode getLockMode(T obj) {
        assert !getGSession().isReadOnly() : "Should never do object locking operations in a read-only session!";
        Session sess = getPartitionType().currentSession().getSession();
        return sess.getCurrentLockMode(obj);
    }

    public boolean isLocked(T obj) {
        return isLocked(obj, LockMode.PESSIMISTIC_WRITE);
    }

    public boolean isLocked(T obj, LockMode lockMode) {
        return !getLockMode(obj).lessThan(lockMode);
    }

    public T lock(T obj) {
        assert !getGSession().isReadOnly() : "Should never lock an object in a read-only session!";
        Session sess = getPartitionType().currentSession().getSession();
        LockMode currentLockMode = sess.getCurrentLockMode(obj);
        // bl: changed so that we will obtain an upgrade lock anytime the current lock mode is less restrictive
        // than upgrade.
        if (currentLockMode.lessThan(LockMode.PESSIMISTIC_WRITE)) {
            if (logger.isDebugEnabled()) {
                logger.debug("locking " + cls.getName() + "#" + obj.getOid() + " with current lockmode of: " + currentLockMode);
            }
            // bl: this wasn't working right for uninitialized proxies.  if the object is an uninitialized proxy,
            // then refresh doesn't actually do anything and will not obtain a LockMode of UPGRADE (LockMode will stay
            // at NONE).  thus, in the case of an uninitialized proxy, do a load to ensure that the object has been
            // loaded from the database.
            if (HibernateUtil.isObjectInitialized(obj)) {
                // todo: if we're doing a refresh, we ought to check to see if the object is dirty.  if it is dirty,
                // we should throw an assert since we should never update the object prior to locking/refreshing
                // it or else the previous changes to the object will be lost as part of the refresh.
                assert !isDirty(obj) : "Found an object that was already dirty when trying to do a refresh to get the object for update!  This is a coding error, as we shouldn't ever do this! oid/" + obj.getOid() + " cls/" + cls.getName() + " currentLockMode: " + currentLockMode;
                sess.refresh(obj, LockMode.PESSIMISTIC_WRITE);
            } else {
                // bl: note that per DefaultLockEventListener, this will result in two separate SQL selects:
                // one to perform the read and one to obtain the lock.  there is a todo in hibernate's
                // code to correct this double-read problem.

                //bk: adding this to fix a hibernate performance problem which will call the select twice if the object
                //    to lock is not already in our session. DefaultLockEventListener:77 for details
                //sess.lock(obj, LockMode.PESSIMISTIC_WRITE);

                obj = (T) sess.get(getDAOObjectClass(), getGSessionFactory().getIdentifier(obj), LockMode.PESSIMISTIC_WRITE);

            }
            assert sess.getCurrentLockMode(obj) == LockMode.PESSIMISTIC_WRITE : "LockMode not UPGRADE after obtaining a lock!  LockMode was instead: " + sess.getCurrentLockMode(obj);
        }
        return obj;
    }

    public boolean isDirty(T obj) {
        SessionImplementor session = ((SessionImplementor) getPartitionType().currentSession().getSession());
        EntityPersister persister = session.getEntityPersister(cls.getName(), obj);
        PersistenceContext persistenceContext = session.getPersistenceContext();
        EntityEntry entry = persistenceContext.getEntry(obj);
        // bl: proxies have entity entries for the inner object rather than the proxy object.
        // thus, we just need to get the concrete instance from the proxy. 
        if (entry == null) {
            T concrete = HibernateUtil.getConcreteClassInstance(obj);
            entry = persistenceContext.getEntry(concrete);
            assert entry != null : "Found a null EntityEntry for an object in the session!  Noticed before when objects are proxies in the PersistenceContext, but getting the concrete instance seemed to fix that problem.  How can we address this scenario?  oid/" + obj.getOid() + " cls/" + cls.getName() + " isProxy/" + persistenceContext.containsProxy(obj);
            obj = concrete;
        }
        final Object[] values = persister.getPropertyValues(obj);
        final Object[] loadedState = entry.getLoadedState();
        boolean mightBeDirty = entry.requiresDirtyCheck(obj);
        // if there's no possibility of it being dirty, then bail out.
        if (!mightBeDirty) {
            return false;
        }
        assert loadedState != null : "Failed to find the loaded state from an object!  This shouldn't ever be possible! oid/" + obj.getOid() + " cls/" + cls.getName();
        // bl: if we decide that we need to support the scenario where loadedState==null, then the following code
        // may work.  for now, commenting out, as I don't think it is necessary.
        // dirty check against the database snapshot, if possible/necessary
        /*if(loadedState==null) {
            OID oid = obj.getOid();
            final Object[] databaseSnapshot;
            if ( persister.isSelectBeforeUpdateRequired() ) {
                Object[] snapshot = session.getPersistenceContext()
                        .getDatabaseSnapshot(oid, persister);
                if (snapshot==null) {
                    //do we even really need this? the update will fail anyway....
                    if ( session.getFactory().getStatistics().isStatisticsEnabled() ) {
                        session.getFactory().getStatisticsImplementor()
                                .optimisticFailure( persister.getEntityName() );
                    }
                    throw new StaleObjectStateException( persister.getEntityName(), oid );
                }
                else {
                    databaseSnapshot = snapshot;
                }
            } else {
                EntityKey entityKey = new EntityKey( oid, persister, session.getEntityMode() );
                databaseSnapshot = session.getPersistenceContext()
                        .getCachedDatabaseSnapshot( entityKey ); 
            }
            
            if ( databaseSnapshot != null ) {
                dirtyProperties = persister.findModified(databaseSnapshot, values, obj, session);
            }
        }*/
        int[] dirtyProperties = persister.findDirty(values, loadedState, obj, session);
        return dirtyProperties != null && dirtyProperties.length != 0;
    }

    public void refresh(T obj) {
        refresh(obj, LockModeType.READ);
    }

    public void refresh(T obj, LockModeType lockModeType) {
        // bl: in order to ensure that state refreshed from the database is up to date, we should flush any
        // dirty objects in the session to the database (in the current transaction) _before_ we do the refresh.
        // this way, objects that cascade on refresh won't risk overwriting dirty state in the session with
        // stale data from the database.
        // bl: only refresh if we're in a writeable session
        if(!getGSession().isReadOnly()) {
            getGSession().flushSession();
        }

        getPartitionType().currentSession().getSession().refresh(obj, lockModeType);
    }

    public void refreshForLock(T obj) {
        // bl: only do the refresh if it's not yet locked. if it's already locked, then we should be good to go.
        if (isLocked(obj, LockMode.PESSIMISTIC_WRITE)) {
            return;
        }

        // bl: beware of calling refreshForLock on the same object multiple times in the same session! Hibernate will
        // initially set the LockMode to PESSIMISTIC_WRITE, but as soon as an update is flushed to the database,
        // the LockMode will be downgraded to just WRITE, even though the object is already locked at the database
        // level in this transaction/session. that means the above isLocked() check will return false, and this assert
        // could blow up if there are further state changes to the object. for now, the solution is to just not call
        // this method multiple times in the context of the same session.
        assert !isDirty(obj) : "Found an object that was already dirty when trying to do a refresh to get the object for update!  This is a coding error, as we shouldn't ever do this! oid/" + obj.getOid() + " cls/" + cls.getName() + " currentLockMode: " + getLockMode(obj);
        // bl: we had issues with refresh loading into a new object instance. that problem seems to have been solved
        // by simply fixing the batch_fetch_style. LEGACY has the problem whereas the newer PADDED style does not.
        // that is now being configured in hibernate.cfg.xml.
        // todo: figure out how to reduce the batch fetch size for this op to 1 so that we don't load other
        // objects awaiting batch load with the same LockMode? the problem is that even though we are asking for this
        // one object to be refreshed with PESSIMISTIC_WRITE, Hibernate will look for any other object IDs of this
        // type that are lazily initialized that it can load as part of the same query. so, those other objects
        // are included in the batch fetch, which is a select...for update. this means that we are actually locking
        // rows that we didn't intend to lock. for now, since this isn't a very widespread problem (the only case
        // i can think of and have seen is the Narrative platform Area's owner AreaUser's User's Wallet, which is
        // locked via select...for update for a different Wallet if it hadn't already been initialized.
        refresh(obj, LockModeType.PESSIMISTIC_WRITE);
    }

    /**
     * get the row count via a "select count(*)".
     *
     * @return the row count for this object.
     */
    public int getRowCount() {
        return getRowCount(null);
    }

    /**
     * get the row count via a "select count(*)".
     *
     * @return the row count for this object.
     */
    public int getRowCount(ObjectPair<String, Object> whereClause) {
        StringBuilder queryString = new StringBuilder().append("select count(*) from ").append(this.cls.getName());
        Query query;

        if (whereClause != null) {
            queryString.append(" t ").append("where ").append("t.").append(whereClause.getOne()).append("= :").append(whereClause.getOne());
            query = getGSession().createQuery(queryString.toString()).setParameter(whereClause.getOne(), whereClause.getTwo());
        } else {
            query = getGSession().createQuery(queryString.toString());
        }
        return ((Number) query.uniqueResult()).intValue();

    }

    /**
     * get the row count via a "select count(*)".  the value will be cached in memory for 5 minutes, so don't use this
     * if you need the query to be 100% accurate.
     *
     * @return the cached value of the row count for this object.
     */
    public int getRowCountCached() {
        ObjectPair<Integer, Long> countAndLastRefresh = s_classToRowCount.get(cls);
        if (needsRefresh(countAndLastRefresh)) {
            synchronized (cls) {
                countAndLastRefresh = s_classToRowCount.get(cls);
                if (needsRefresh(countAndLastRefresh)) {
                    s_classToRowCount.put(cls, countAndLastRefresh = new ObjectPair<Integer, Long>(getRowCount(), System.currentTimeMillis()));
                }
            }
        }
        return countAndLastRefresh.getOne();
    }

    /**
     * get the row count via a "select count(*)".  the value will be cached in memory for 5 minutes, so don't use this
     * if you need the query to be 100% accurate.
     *
     * @return the cached value of the row count for this object.
     */
    public int getRowCountCached(String field, Object typeValue) {
        String key = new StringBuilder().append(cls.getName()).append("/").append(field).append("-").append(typeValue.toString()).toString();
        ObjectPair<Integer, Long> countAndLastRefresh = s_classWithTypeToRowCount.get(key);
        if (needsRefresh(countAndLastRefresh)) {
            synchronized (cls) {
                countAndLastRefresh = s_classWithTypeToRowCount.get(key);
                if (needsRefresh(countAndLastRefresh)) {
                    s_classWithTypeToRowCount.put(key, countAndLastRefresh = new ObjectPair<Integer, Long>(getRowCount(new ObjectPair(field, typeValue)), System.currentTimeMillis()));
                }
            }
        }
        return countAndLastRefresh.getOne();
    }

    private static boolean needsRefresh(ObjectPair<Integer, Long> countAndLastRefresh) {
        if (countAndLastRefresh == null) {
            return true;
        }
        return System.currentTimeMillis() >= (countAndLastRefresh.getTwo() + 5 * IPDateUtil.MINUTE_IN_MS);
    }

    /**
     * This returns a row count estimate by innodb.  Its based on the phisical table size and the average size
     * of the records.  Don't use it if you need a precise count!
     *
     * @return the approximate row count for this table based on "show table status"
     * @deprecated "show table status" doesn't show accurate row counts in InnoDB.  In fact, they can
     * be off by up to 40-50%.  thus, we probably should use getRowCount() for now, which
     * does a select count(*) which is the only accurate way to get a row count in InnoDB.
     * refer: http://dev.mysql.com/doc/refman/5.0/en/show-table-status.html
     */
    public int getApproximateRowCount() {
        ResultSet rs = null;
        try {
            rs = getPartitionType().currentPartition().getDatabaseResources().getResultSetFromNamedSql("SHOW_TABLE_STATUS", IPUtil.getClassSimpleName(this.cls));
            if (rs.next()) {
                return rs.getInt("rows");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Unable to get row count", e, true);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw UnexpectedError.getRuntimeException("Unable to close resulstset", e, true);
                }
            }
        }

    }

    /**
     * some operations (such as creating temp tables) must happen in auto-commit mode. this utility
     * provides a way to easily run a command after first committing the transaction and enabling auto-commit.
     * once it's done, it will re-enable auto-commit and start a new transaction.
     * @param r the runnable to run while in auto-commit mode
     */
    public void runForAutoCommit(Runnable r) {
        // bl: in order to create the temp table, we need to commit the transaction so we are in
        // auto-commit mode. this is a requirement for GTID consistency used by Google Cloud SQL
        try {
            getGSession().commitTransactionForAutoCommit();
            r.run();
            getGSession().resetTransactionToDisableAutoCommit();
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Failed running query for auto-commit!", e);
        }
    }

}
