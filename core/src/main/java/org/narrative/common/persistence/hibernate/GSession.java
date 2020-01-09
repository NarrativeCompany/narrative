package org.narrative.common.persistence.hibernate;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.Debug;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionTypeConnectionProvider;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.query.NativeQuery;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Nov 29, 2005
 * Time: 10:03:50 AM
 *
 * @author Paul Malolepsy
 */
public class GSession {

    private static final NarrativeLogger logger = new NarrativeLogger(GSession.class);

    private Session session;
    private SessionImplementor sessionImpl;
    private GConnection connection;
    private final GSessionFactory gSessionFactory;
    //private Set<EventListener> sessionEventListeners = new HashSet<EventListener>();
    private boolean readOnly = false;
    private GTransaction transaction;

    private final OID partitionOid;

    private boolean isInNonForceWritableTask = false;

    public GSession(GSessionFactory gSessionFactory, OID partitionOid) {
        this.gSessionFactory = gSessionFactory;
//        this.session.setFlushMode(FlushMode.NEVER);  //forces us to always explicitly call flush, which is behavoir we want
        this.partitionOid = partitionOid;
    }

    public OID getPartitionOid() {
        return partitionOid;
    }

    public boolean isHasSession() {
        return session != null;
    }

    private void initSessionAndConnection() {
        if (session != null) {
            return;
        }
        session = gSessionFactory.getSessionFactory().openSession();
        initConnection();
    }

    private void initConnection() {
        Connection con;
        try {
            PartitionTypeConnectionProvider.setCurrentPartitionOid(partitionOid);
            con = getSessionImpl().getSession().connection();
            if (!(con instanceof GConnection)) {
                throw UnexpectedError.getRuntimeException("Didn't find GConnection from Hibernate SessionImpl! class/" + con.getClass().getName());
            }
        } finally {
            PartitionTypeConnectionProvider.setCurrentPartitionOid(null);
        }
        connection = (GConnection) con;
        // update the readOnly status for the Session and Connection based on previous setting of the readOnly flag.
        updateReadOnly();
        // bl: always use a transaction.  start it.
        startTransaction();
        if (logger.isDebugEnabled()) {
            logger.debug("Starting transaction " + transaction.getId() + " for " + gSessionFactory.getName() + " (" + connection.getComboPooledDataSource().getJdbcUrl() + ")");
        }
    }

    public Session getSession() {
        initSessionAndConnection();
        return session;
    }

    private SessionImplementor getSessionImpl() {
        if (sessionImpl == null) {
            sessionImpl = (SessionImplementor) getSession();
        }
        return sessionImpl;
    }

    /* bl: removing session event listeners since they weren't being used at all
    public Set<EventListener> getSessionEventListeners() {
        return sessionEventListeners;
    }

    public void registerSessionEventListener(EventListener eventListener) {
        sessionEventListeners.add(eventListener);
    }*/

    /**
     * Sets the session to read only.  No flushes will occur and no dirty checking of objects will occur
     *
     * @return
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    private void updateReadOnly() {
        // bl: only update readOnly if it has a session.  don't want the setting of the read only flag to trigger a new session.
        if (isHasSession()) {
            if (readOnly) {
                getSession().setHibernateFlushMode(FlushMode.MANUAL);
            } else {
                getSession().setHibernateFlushMode(FlushMode.AUTO);
            }
            // bl: doesn't hurt to always set the connection's readOnly flag.  GConnection won't allow a call to
            // setReadOnly to trigger a new connection.
            setConnectionReadOnly(readOnly);
        }
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        updateReadOnly();
    }

    private void setConnectionReadOnly(boolean isReadOnly) {
        assert connection != null : "Should only attempt to set a connection's read-only status if the connection already exists!";
        try {
            this.connection.setReadOnly(isReadOnly);
        } catch (SQLException sqle) {
            throw UnexpectedError.getRuntimeException("Failed marking Connection as read only: " + isReadOnly, sqle, true);
        }
    }

    public boolean isInNonForceWritableTask() {
        return isInNonForceWritableTask;
    }

    public void setInNonForceWritableTask(boolean inNonForceWritableTask) {
        isInNonForceWritableTask = inNonForceWritableTask;
    }

    public void startTransaction() {
        assert transaction == null : "Shouldn't ever attempt to start a transaction while another transaction is already processing!  You should commit or rollback the current transaction first!";
        transaction = new GTransaction(getSession().getTransaction());
        try {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Unable to set transaction isolation level", e, true);
        }
    }

    public boolean isInTransaction() {
        return transaction != null;
    }

    public void commitTransaction() {
        if (isInTransaction() && transaction.isActive()) {
            transaction.commit();
        }
        transaction = null;
    }

    public void rollbackTransaction() {
        if (isInTransaction() && transaction.isActive()) {
            transaction.rollback();
        }
        transaction = null;
    }

    /**
     * Usefull during batch processing, commits the current transation, clears the session and starts a new transaction
     */
    public void commitBatch() {
        commitTransaction();
        clearSession();
        startTransaction();
    }

    public void commitTransactionForAutoCommit() throws SQLException {
        // bl: it's key to make sure we re-use the same connection!
        Connection con = connection;
        commitTransaction();
        if(session==null) {
            assert con==null : "Should never have a connection without a session!";
            initSessionAndConnection();
        } else {
            // bl: make sure we re-use the same connection!
            PartitionTypeConnectionProvider.setCurrentConnection(con);
            try {
                initConnection();
            } finally {
                PartitionTypeConnectionProvider.setCurrentConnection(null);
            }
            assert con==getConnection() : "Should have the same connection!";
            assert getSessionImpl().connection()==con : "Should have the same session connection!";
        }
        getConnection().setAutoCommit(true);
    }

    public void resetTransactionToDisableAutoCommit() throws SQLException {
        // bl: it's key to make sure we re-use the same connection!
        Connection con = getConnection();
        con.setAutoCommit(false);
        commitTransaction();
        // bl: make sure we re-use the same connection!
        PartitionTypeConnectionProvider.setCurrentConnection(con);
        try {
            initConnection();
        } finally {
            PartitionTypeConnectionProvider.setCurrentConnection(null);
        }
        assert con==getConnection() : "Should have the same connection!";
        assert getSessionImpl().connection()==con : "Should have the same session connection!";
    }

    public void close() {
        if (isHasSession()) {
            getSession().close();

            // when we're done, set the connection back to being non-read only
            setConnectionReadOnly(false);
        }
    }

    /**
     * Test method to see if a class has already been loaded into the cache.
     *
     * @param clazz The class you are interested in.
     * @param id    Its ID (aka OID)
     * @return True if the object is in the cache.
     */
    public boolean isInCache(Class clazz, Serializable id) {

        try {
            SessionImplementor si = getSessionImpl();
            return (si.getEntityPersister(clazz.getName(), clazz.newInstance()).getCacheAccessStrategy().get(si, id, System.currentTimeMillis()) != null);
        } catch (Exception e) {
            throw UnexpectedError.getRuntimeException("Unable to get cache status for class/" + clazz, e, true);
        }
    }

    /**
     * a simple wrapper method to get a Hibernate object.
     * uses Session.get() instead of Session.load().  returns
     * null if an Object with this identifier doesn't exist in the database.
     * probably should only be used by classes that are not lazily initialized
     * for now.
     * in the event of a HibernateException,  a Debug.assertMsg(logger, false)
     * is triggered.
     * nb. this method will _always_ return an object of the specified class.  if for some
     * reason Hibernate's cache returns an Object that is not of the specified class,
     * then this method will detect that and will return null.
     *
     * @param clazz the class to get an object for
     * @param id    the id of the object to retrieve
     * @return the Object.  null if an Object with the specified identifier doesn't exist.
     */
    @Nullable
    public <T> T getObject(Class<T> clazz, Serializable id) {
        return getObject(clazz, id, LockMode.NONE);
    }

    @Nullable
    public <T> T getObject(Class<T> clazz, Serializable id, LockMode lockMode) {
        if (id == null) {
            return null;
        }
        T o = (T) getSession().get(clazz, id, lockMode);
        if (o == null) {
            return null;
        }
        // bl: this test only should ever fail if doing a lookup in an Object hierarchy
        // for which an object belongs in the hierarchy and is cached, but isn't of the specified
        // type.  e.g.
        // Categories have corresponding Resources with the same ID as the Category.
        // if you try to load a Forum by the category ID and the Resource with the
        // Category ID is in the cache, that Resource will be returned, even though
        // the Resource isn't actually a Forum object.
        // nb. do _NOT_ do isObjectOfType() because that will result in initializing the
        // object every time that we do a read.  in the case of a get or a load, we _must_
        // get an object of the expected type back.  if we don't, it ultimately would likely
        // result in a ClassCastException or some other problem.
        //if(!isObjectOfType(o, clazz))
        //    return null;
        // check if the object return is of the expected type.  if not, return null.
        if (!clazz.isAssignableFrom(o.getClass())) {
            return null;
        }

        // bl: a double check to make sure that the object returned from getObject()
        // is the actual object itself, and not a proxy.
        o = HibernateUtil.getConcreteClassInstance(o);

        // bl: always set read only flag.  this is because we are allowing "upgrades" of a readOnly session
        // to being writable, so need to mark the object as writable in case it was previously read only.
        setReadOnlyStatus(o);

        return o;
    }

    public <T> T load(Class<T> clazz, Serializable id) {
        return load(clazz, id, null);
    }

    /**
     * a simple wrapper method to load a Hibernate object.
     * nb. this method will _always_ return an object of the specified class.  if for some
     * reason Hibernate's cache returns an Object that is not of the specified class,
     * then this method will detect that and will return null.
     *
     * @param clazz
     * @param id
     * @return
     * @throws org.hibernate.HibernateException
     */
    @Nullable
    public <T> T load(Class<T> clazz, Serializable id, LockMode mode) throws HibernateException {
        if (id == null) {
            return null;
        }

        T o;
        if (mode == null) {
            o = getSession().load(clazz, id);

        } else {
            o = getSession().load(clazz, id, mode);
        }

        if (o == null) {
            return null;
        }
        // bl: this test only should ever fail if doing a lookup in an Object hierarchy
        // for which an object belongs in the hierarchy and is cached, but isn't of the specified
        // type.  e.g.
        // Categories have corresponding Resources with the same ID as the Category.
        // if you try to load a Forum by the category ID and the Resource with the
        // Category ID is in the cache, that Resource will be returned, even though
        // the Resource isn't actually a Forum object.
        // nb. do _NOT_ do isObjectOfType() because that will result in initializing the
        // object every time that we do a read.  in the case of a get or a load, we _must_
        // get an object of the expected type back.  if we don't, it ultimately would likely
        // result in a ClassCastException or some other problem.
        //if(!isObjectOfType(o, clazz))
        //    return null;
        // check if the object return is of the expected type.  if not, return null.
        if (!clazz.isAssignableFrom(o.getClass())) {
            return null;
        }

        // the one caveat is that proxy objects can not be marked read only.
        //session.setReadOnly(o, true);
        // the previous line would just cause the following error:
        // TransientObjectException: Instance was not associated with the session

        // only set the object as read only if it is initialized.
        // bl: also need to check exists here since we are going to be doing a concrete.
        // otherwise, sometimes the object will be marked as initialized by the LazyInitializer
        // even when the object doesn't exist.  in this case, an ObjectNotFoundException would
        // be thrown by the call to concrete().
        if (HibernateUtil.isObjectInitialized(o) && exists(o)) {
            // bl: always set read only flag.  this is because we are allowing "upgrades" of a readOnly session
            // to being writable, so need to mark the object as writable in case it was previously read only.
            setReadOnlyStatus(o);
        }
        return o;
    }

    @Nullable
    public <T> T get(Class<T> clazz, Serializable id, LockMode mode) throws HibernateException {
        if (id == null) {
            return null;
        }

        T o;
        if (mode == null) {
            o = (T) getSession().get(clazz, id);

        } else {
            o = (T) getSession().get(clazz, id, mode);
        }

        if (o == null) {
            return null;
        }

        // bl: always set read only flag.  this is because we are allowing "upgrades" of a readOnly session
        // to being writable, so need to mark the object as writable in case it was previously read only.
        setReadOnlyStatus(o);

        return o;
    }

    public <T> void setReadOnlyStatus(T o) {
        EntityEntry entry = getSessionImpl().getPersistenceContext().getEntry(concrete(o));
        if (entry == null) {
            return;
        }
        // jw: if this is a immutable object then never upgrade to readOnly=false (it will result in a error)
        if (!readOnly && entry.getPersister() != null && !entry.getPersister().isMutable()) {
            return;
        }

        //only set the objects read only flag if it is changing
        if ((entry.getStatus() == Status.MANAGED && readOnly) || entry.getStatus() == Status.READ_ONLY && !readOnly) {
            getSession().setReadOnly(HibernateUtil.getConcreteClassInstance(o), readOnly);
        }
    }

    public <T> boolean isManaged(T o) {
        EntityEntry entry = getSessionImpl().getPersistenceContext().getEntry(concrete(o));
        return entry != null && entry.getStatus() == Status.MANAGED;
    }

    /**
     * a simple wrapper method to save a Hibernate object.
     * in the event of a HibernateException,  a Debug.assertMsg(logger, false)
     * is triggered
     *
     * @param o the object to save
     */
    public Serializable saveObject(Object o) {
        if (o == null) {
            Debug.assertMsg(logger, false, "Must specify an object to save!");
        }
        assert !readOnly : "Shouldn't attempt to save an object in a readOnly session!";
        assert !isInNonForceWritableTask : "Shouldn't attempt to save an object from within a non-force writable task!  This task should be changed to be force writable = true!";
        return getSession().save(o);
    }

    /**
     * a simple wrapper method to saveOrUpdate a Hibernate object.
     * in the event of a HibernateException,  a Debug.assertMsg(logger, false)
     * is triggered
     *
     * @param o the object to saveOrUpdate
     */
    public void saveOrUpdateObject(Object o) {
        if (o == null) {
            Debug.assertMsg(logger, false, "Must specify an object to save/update!");
        }
        assert !readOnly : "Shouldn't attempt to saveOrUpdate an object in a readOnly session!";
        assert !isInNonForceWritableTask : "Shouldn't attempt to save or update an object from within a non-force writable task!  This task should be changed to be force writable = true!";
        getSession().saveOrUpdate(o);
    }

    /**
     * a simple wrapper method to update a Hibernate object.
     * in the event of a HibernateException,  a Debug.assertMsg(logger, false)
     * is triggered
     *
     * @param o the object to update
     */
    public void updateObject(Object o) {
        if (o == null) {
            Debug.assertMsg(logger, false, "Must specify an object to update!");
        }
        assert !readOnly : "Shouldn't attempt to update an object in a readOnly session!";
        assert !isInNonForceWritableTask : "Shouldn't attempt to update an object from within a non-force writable task!  This task should be changed to be force writable = true!";
        getSession().update(o);
    }

    /**
     * a simple wrapper method to delete a Hibernate object.
     * in the event of a HibernateException,  a Debug.assertMsg(logger, false)
     * is triggered
     *
     * @param o the object to delete
     */
    public void deleteObject(Object o) {
        if (o == null) {
            Debug.assertMsg(logger, false, "Must specify an object to delete!");
        }
        assert !readOnly : "Shouldn't attempt to delete an object in a readOnly session!";
        assert !isInNonForceWritableTask : "Shouldn't attempt to delete an object from within a non-force writable task!  This task should be changed to be force writable = true!";
        getSession().delete(o);
    }

    /**
     * a simple wrapper method to refresh a Hibernate object.
     * in the event of a HibernateException,  a Debug.assertMsg(logger, false)
     * is triggered
     *
     * @param o the object to refresh
     */
    public void refreshObject(Object o) {
        if (o == null) {
            Debug.assertMsg(logger, false, "Must specify an object to refresh!");
        }
        getSession().refresh(o);
    }

    /**
     * evict an object from the session cache
     *
     * @param o the object to evict from the session cache
     */
    public void evict(Object o) {
        if (o == null) {
            Debug.assertMsg(logger, false, "Must specify an object to evict!");
        }
        getSession().evict(o);
        // bl: we have an onEvict handler in our CacheManager.CacheListener, so we shouldn't need to explicitly
        // invalidate the object here.
        //CacheManager.invalidateObject(o, this);
    }

    /**
     * Centralize the flushing of the current session here so that the calling code doesnt
     * have to worry about catching hibernate exceptions
     */
    public void flushSession() {
        if (isHasSession()) {
            assert !readOnly : "Shouldn't attempt to flush in a readOnly session!";
            assert !isInNonForceWritableTask : "Shouldn't attempt to flush a session from within a non-force writable task!  This task should be changed to be force writable = true!";
            getSession().flush();
        }
    }

    /**
     * clear the current Hibernate session
     */
    public void clearSession() {
        if (isHasSession()) {
            getSession().clear();
        }
    }

    public <T extends DAOObject, ID extends Serializable> List<T> getOrderedObjectListFromOrderedIdList(Collection<ID> ids, Collection<T> objs) {
        Map<ID, T> idToObj = getIDToObjectsFromObjects(objs);
        List<T> ret = new ArrayList<T>(objs.size());
        for (ID id : ids) {
            T obj = idToObj.get(id);
            if (obj != null) {
                ret.add(obj);
            }
        }
        return ret;
    }

    public <T extends DAOObject, ID extends Serializable> Map<ID, T> getIDToObjectsFromObjects(Collection<T> objs) {
        if (isEmptyOrNull(objs)) {
            return Collections.emptyMap();
        }
        Map<ID, T> ret = new HashMap<>(objs.size());
        for (T obj : objs) {
            ret.put(gSessionFactory.getIdentifier(obj), obj);
        }
        return ret;
    }

    public <T extends DAOObject, ID extends Serializable> List<ID> getIDsFromObjects(Collection<T> objs) {
        return gSessionFactory.getIDsFromObjects(objs);
    }

    public GSessionFactory getGSessionFactory() {
        return gSessionFactory;
    }

    public GConnection getConnection() {
        initSessionAndConnection();
        return connection;
    }

    public GTransaction getTransaction() {
        return transaction;
    }

    public Query getNamedQuery(String queryName) {
        return getSession().getNamedQuery(queryName).setReadOnly(readOnly);
    }

    public <T> org.hibernate.query.Query<T> createNamedQuery(String queryName, Class<T> resultType) {
        return getSession().createNamedQuery(queryName, resultType).setReadOnly(readOnly);
    }

    public NativeQuery getNamedNativeQuery(String queryName) {
        return getSession().getNamedNativeQuery(queryName).setReadOnly(readOnly);
    }

    public NativeQuery createNativeQuery(String sqlString) {
        return getSession().createNativeQuery(sqlString);
    }

    public org.hibernate.query.Query createQuery(String hql) {
        return getSession().createQuery(hql).setReadOnly(readOnly);
    }

    public <T> org.hibernate.query.Query<T> createQuery(String hql, Class<T> resultType) {
        return getSession().createQuery(hql, resultType).setReadOnly(readOnly);
    }

    public <T> T getFromSessionCache(Serializable id, Class<T> cls) {
        EntityKey key = new EntityKey(id, getSessionImpl().getFactory().getEntityPersister(cls.getName()));
        return (T) getSessionImpl().getEntityUsingInterceptor(key);
    }
}
