package org.narrative.common.persistence;

import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.InvalidParamError;
import org.narrative.common.util.SubListIterator;
import lombok.Builder;
import lombok.Data;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.engine.spi.CacheImplementor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.query.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 24, 2005
 * Time: 2:01:43 AM
 */
public abstract class DAOImpl<T extends DAOObject, ID extends Serializable> implements DAO<T, ID> {
    private static final NarrativeLogger logger = new NarrativeLogger(DAOImpl.class);

    @NotNull
    protected final Class<T> cls;

    protected DAOImpl(@NotNull Class<T> cls) {
        this.cls = cls;
    }

    public abstract GSession getGSession();

    @NotNull
    public final Class<T> getDAOObjectClass() {
        return cls;
    }

    public Collection<Class<? extends T>> getDAOObjectDescendents() {
        return Collections.emptySet();
    }

    private EntityType<T> getEntityType() {
        return getGSession().getGSessionFactory().getSessionFactory().getMetamodel().entity(getDAOObjectClass());
    }

    private Class<ID> getIdClass() {
        Type<ID> type = (Type<ID>)getEntityType().getIdType();
        return type.getJavaType();
    }

    private SingularAttribute<? super T,ID> getIdAttribute() {
        return getEntityType().getId(getIdClass());
    }

    @Override
    public T get(ID id) {
        if (id == null) {
            return null;
        }
        return getGSession().load(cls, id);
    }

    public T getForApiParam(ID id, String paramName) throws InvalidParamError {
        if (id == null) {
            throw new InvalidParamError(paramName, null);
        }

        T ret = get(id);

        if (!exists(ret)) {
            throw new InvalidParamError(paramName, id.toString());
        }

        return ret;
    }

    public List<T> getAll() {
        return getAll(false);
    }

    public List<T> getAll(boolean cacheQuery) {
        // bl: IntelliJ is incorrectly reporting types as being erased, so I'm splitting these into separate statements
        /// to avoid excessive IDE warnings
        DAOObjectQueryBuilder builder = new DAOObjectQueryBuilder();
        DAOObjectQuery daoObjectQuery = builder.build();
        Query<T> query = daoObjectQuery.getQuery();
        query.setCacheable(cacheQuery);
        return query.list();
    }

    public List<T> getAllOrderBy(Collection<ObjectPair<String, Boolean>> orderBy, Integer first, Integer limit, boolean cacheQuery) {
        List<OrderByProperty> orderByProperties = new ArrayList<>(orderBy.size());
        for (ObjectPair<String, Boolean> pair : orderBy) {
            orderByProperties.add(new OrderByProperty(pair.getOne(), pair.getTwo()));
        }
        // bl: IntelliJ is incorrectly reporting types as being erased, so I'm splitting these into separate statements
        /// to avoid excessive IDE warnings
        DAOObjectQueryBuilder builder = new DAOObjectQueryBuilder();
        builder.orderByProperties(orderByProperties);
        DAOObjectQuery daoObjectQuery = builder.build();
        Query<T> query = daoObjectQuery.getQuery();
        if (first != null) {
            query.setFirstResult(first);
        }
        if (limit != null) {
            query.setMaxResults(limit);
        }

        query.setCacheable(cacheQuery);

        return query.list();
    }

    @Override
    public List<T> getAllBy(NameValuePair<?>... nameValuePairs) {
        return getAllBy((Integer) null, false, nameValuePairs);
    }

    public List<T> getAllBy(Integer limit, boolean cached, NameValuePair<?>... nameValuePairs) {
        // bl: IntelliJ is incorrectly reporting types as being erased, so I'm splitting these into separate statements
        /// to avoid excessive IDE warnings
        DAOObjectQueryBuilder builder = new DAOObjectQueryBuilder();
        builder.nameValuePairs(nameValuePairs);
        DAOObjectQuery daoObjectQuery = builder.build();
        Query<T> query = daoObjectQuery.getQuery();
        if(limit!=null) {
            query.setMaxResults(limit);
        }
        if (cached) {
            query.setCacheable(true);
        }
        return query.list();
    }

    public T getUniqueBy(NameValuePair<?>... nameValuePairs) {
        return getUniqueBy(false, nameValuePairs);
    }

    public T getUniqueByWithCache(NameValuePair<?>... nameValuePairs) {
        return getUniqueBy(true, nameValuePairs);
    }

    private T getUniqueBy(boolean cached, NameValuePair<?>... nameValuePairs) {
        // bl: IntelliJ is incorrectly reporting types as being erased, so I'm splitting these into separate statements
        /// to avoid excessive IDE warnings
        DAOObjectQueryBuilder builder = new DAOObjectQueryBuilder();
        builder.nameValuePairs(nameValuePairs);
        DAOObjectQuery daoObjectQuery = builder.build();
        Query<T> query = daoObjectQuery.getQuery();
        if (cached) {
            query.setCacheable(true);
        }
        return query.uniqueResult();
    }

    public long getCountForAllBy(NameValuePair<?>... nameValuePairs) {
        // bl: IntelliJ is incorrectly reporting types as being erased, so I'm splitting these into separate statements
        /// to avoid excessive IDE warnings
        CountQueryBuilder builder = new CountQueryBuilder();
        builder.nameValuePairs(nameValuePairs);
        CountQuery countQuery = builder.build();
        Query<Long> query = countQuery.getQuery();
        return query.getSingleResult();
    }

    private abstract class QueryBase<V> {
        final CriteriaBuilder cb;
        final CriteriaQuery<V> criteriaQuery;
        final Root<T> root;

        QueryBase(Class<V> returnType, NameValuePair<?>[] nameValuePairs, Collection<ID> idsIn) {
            cb = getGSession().getSession().getCriteriaBuilder();
            criteriaQuery = cb.createQuery(returnType);
            root = criteriaQuery.from(getDAOObjectClass());
            List<Predicate> predicates = new LinkedList<>();
            if(nameValuePairs!=null) {
                for (NameValuePair<?> nvp : nameValuePairs) {
                    if (nvp.getValue() instanceof Collection) {
                        predicates.add(root.get(nvp.getName()).in((Collection<?>)nvp.getValue()));
                    } else {
                        predicates.add(cb.equal(root.get(nvp.getName()), nvp.getValue()));
                    }
                }
            }
            if(!isEmptyOrNull(idsIn)) {
                predicates.add(root.get(getIdAttribute()).in(idsIn));
            }
            if(!predicates.isEmpty()) {
                criteriaQuery.where(cb.and(predicates.toArray(new Predicate[0])));
            }
        }

        public Query<V> getQuery() {
            return getGSession().getSession().createQuery(criteriaQuery);
        }
    }

    protected class CountQuery extends QueryBase<Long> {
        CountQuery(NameValuePair<?>[] nameValuePairs, Collection<ID> idsIn) {
            super(Long.class, nameValuePairs, idsIn);
            criteriaQuery.select(cb.count(root));
        }
    }

    private class OrderQueryBase<V> extends QueryBase<V> {
        OrderQueryBase(Class<V> returnType, NameValuePair<?>[] nameValuePairs, Collection<ID> idsIn, List<OrderByProperty> orderByProperties) {
            super(returnType, nameValuePairs, idsIn);
            if(!isEmptyOrNull(orderByProperties)) {
                for (OrderByProperty orderByProperty : orderByProperties) {
                    Path path = root.get(orderByProperty.getPropertyName());
                    criteriaQuery.orderBy(orderByProperty.isAsc() ? cb.asc(path) : cb.desc(path));
                }
            }
        }
    }

    protected class DAOObjectQuery extends OrderQueryBase<T> {
        DAOObjectQuery(NameValuePair<?>[] nameValuePairs, Collection<ID> idsIn, List<OrderByProperty> orderByProperties) {
            super(getDAOObjectClass(), nameValuePairs, idsIn, orderByProperties);
            criteriaQuery.select(root);
        }
    }

    protected class IDQuery extends OrderQueryBase<ID> {
        private IDQuery(NameValuePair<?>[] nameValuePairs, Collection<ID> idsIn, List<OrderByProperty> orderByProperties) {
            super(getIdClass(), nameValuePairs, idsIn, orderByProperties);
            criteriaQuery.select(root.get(getIdAttribute()));
        }
    }

    @Data
    private static class OrderByProperty {
        private final String propertyName;
        private final boolean asc;
    }
    
    @Builder(builderMethodName = "countQueryBuilder")
    private CountQuery createCountQuery(NameValuePair<?>[] nameValuePairs, Collection<ID> idsIn) {
        return new CountQuery(nameValuePairs, idsIn);
    }
    
    @Builder(builderMethodName = "daoObjectQueryBuilder")
    private DAOObjectQuery createDAOObjectQuery(NameValuePair<?>[] nameValuePairs, Collection<ID> idsIn, List<OrderByProperty> orderByProperties) {
        return new DAOObjectQuery(nameValuePairs, idsIn, orderByProperties);
    }
    
    @Builder(builderMethodName = "idQueryBuilder")
    private IDQuery createIDQuery(NameValuePair<?>[] nameValuePairs, Collection<ID> idsIn, List<OrderByProperty> orderByProperties) {
        return new IDQuery(nameValuePairs, idsIn, orderByProperties);
    }

    public List<ID> getAllIDsBy(NameValuePair<?>... nameValuePairs) {
        return getAllIDsBy(null, nameValuePairs);
    }

    public List<ID> getAllIDsBy(Integer results, NameValuePair<?>... nameValuePairs) {
        // bl: IntelliJ is incorrectly reporting types as being erased, so I'm splitting these into separate statements
        /// to avoid excessive IDE warnings
        IDQueryBuilder builder = new IDQueryBuilder();
        builder.nameValuePairs(nameValuePairs);
        IDQuery idQuery = builder.build();
        Query<ID> query = idQuery.getQuery();
        if (results != null && results > 0) {
            query.setMaxResults(results);
            query.setFetchSize(results);
        }
        return query.list();
    }

    public <V> List<T> getAllIn(NameValuePair<? extends Collection<V>>... nameValuePairs) {
        // bl: IntelliJ is incorrectly reporting types as being erased, so I'm splitting these into separate statements
        /// to avoid excessive IDE warnings
        DAOObjectQueryBuilder builder = new DAOObjectQueryBuilder();
        builder.nameValuePairs(nameValuePairs);
        DAOObjectQuery daoObjectQuery = builder.build();
        Query<T> query = daoObjectQuery.getQuery();
        return query.list();
    }

    @Override
    public T getFirstBy(NameValuePair<?>... nameValuePairs) {
        return getFirstBy(false, nameValuePairs);
    }

    private T getFirstBy(boolean cached, NameValuePair<?>... nameValuePairs) {
        List<T> l = getAllBy(1, cached, nameValuePairs);
        if (l != null && !l.isEmpty()) {
            return l.get(0);
        }

        return null;
    }

    @Nullable
    public T getReal(ID id) {
        if (id == null) {
            return null;
        }

        Class<T> clz = getDAOObjectClass();
        T ret = null;

        // bk: for some reason hibernate is caching the ObjectNotFoundException in its cache when an object that does not
        // exist is lazily loaded. This is here to catch that exception, log it, and
        // return null, since the object does not actually exist.
        try {
            ret = getGSession().getObject(clz, id);
        } catch (ObjectNotFoundException ex) {
            logger.warn("Tried a getReal on id: " + id + " of class: " + clz.getName(), ex);
        }

        return ret;
    }

    public <C extends T> List<C> getOrderedObjectListFromOrderedIdList(Collection<ID> ids, Collection<C> objs) {
        return getGSession().getOrderedObjectListFromOrderedIdList(ids, objs);
    }

    public List<T> getObjectsFromIDs(Collection<ID> ids, int maxResults, DAOObjectFilter<T> filter) {
        List<T> list = getObjectsFromIDs(ids);
        if (filter != null) {
            // remove any objects that should be filtered.
            list.removeIf(t -> !filter.includeObject(t));
        }
        if (list != null && list.size() > maxResults) {
            return list.subList(0, maxResults);
        }
        return list;
    }

    public <V> int deleteAllByPropertyValue(String propertyName, V value) {
        CriteriaBuilder cb = getGSession().getSession().getCriteriaBuilder();
        CriteriaDelete<T> delete = cb.createCriteriaDelete(getDAOObjectClass());
        Root<T> root = delete.from(getDAOObjectClass());
        delete.where(cb.equal(root.get(propertyName), value));
        return getGSession().getSession().createQuery(delete).executeUpdate();
    }

    /**
     * get a List of Objects from a given list of OIDs
     *
     * @param ids the IDs of the objects to load
     * @return a List of objects corresponding to the collection of ids specified
     */
    public <C extends T> List<C> getObjectsFromIDs(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        if (ids.size() == 1) {
            T obj = get(ids.iterator().next());
            // bl: if no object was identified, then return an empty list
            if (obj == null) {
                return Collections.emptyList();
            }
            // otherwise, return the list that actually has the object in it
            // bl: no reason to return a singleton list here, as those are unmodifiable. return a new List so that it can be modified.
            return new ArrayList<>(Collections.singleton((C)obj));
        }

        // bl: return a list in the same order as the list that was supplied
        List<T> list = newLinkedList();
        SubListIterator<ID> iter = new SubListIterator<>(new ArrayList<>(ids), SubListIterator.CHUNK_LARGE);
        while (iter.hasNext()) {
            List<ID> idChunk = iter.next();
            // bl: IntelliJ is incorrectly reporting types as being erased, so I'm splitting these into separate statements
            /// to avoid excessive IDE warnings
            DAOObjectQueryBuilder builder = new DAOObjectQueryBuilder();
            builder.idsIn(idChunk);
            DAOObjectQuery daoObjectQuery = builder.build();
            Query<T> query = daoObjectQuery.getQuery();
            list.addAll(query.list());
        }

        return (List<C>)getOrderedObjectListFromOrderedIdList(ids, list);
    }

    public List<T> getLockedObjectsFromIDs(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // jw: Unlike the getObjectsFromIDs we cannot chunk through the ids that we were given, we have to read them all in one statement.
        // bl: IntelliJ is incorrectly reporting types as being erased, so I'm splitting these into separate statements
        /// to avoid excessive IDE warnings
        DAOObjectQueryBuilder builder = new DAOObjectQueryBuilder();
        builder.idsIn(ids);
        DAOObjectQuery daoObjectQuery = builder.build();
        Query<T> query = daoObjectQuery.getQuery();
        List<T> list = query
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .list();

        return getGSession().getOrderedObjectListFromOrderedIdList(ids, list);
    }

    public List<T> getObjectsFromIDsWithCache(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        // first, prime the objects not yet in the cache
        primeObjectsNotInCache(ids);
        List<T> ret = new ArrayList<T>(ids.size());
        // now that we have primed the objects not in the cache, go through
        // and create the list of objects from the ids since they all should be in the cache now.
        for (ID id : ids) {
            ret.add(get(id));
        }
        return ret;
    }

    public Collection<T> removeNonExistentObjects(Collection<T> objs) {
        if (objs == null) {
            return null;
        }
        objs.removeIf(obj -> !exists(obj));

        return objs;
    }

    public Map<ID, T> getIDToObjectsFromIDs(Collection<ID> ids) {
        List<T> objs = getObjectsFromIDsWithCache(ids);
        return getIDToObjectsFromObjects(objs);
    }

    public Map<ID, T> getIDToObjectsFromObjects(Collection<T> objs) {
        return getGSession().getIDToObjectsFromObjects(objs);
    }

    public List<ID> getIdsFromObjects(Collection<? extends T> objs) {
        return getGSession().getIDsFromObjects(objs);
    }

    /**
     * Returns a list of object oids that are not yet in the session cache or 2nd tier cache
     *
     * @param idsToCheck The oids to check
     * @return A subset of the oids passed in that are not in the cache
     */
    private Set<ID> getObjectIDsNotInCache(Collection<ID> idsToCheck) {
        Set<ID> idsToDo = new HashSet<>();
        SessionFactoryImplementor sf = (SessionFactoryImplementor) getGSession().getGSessionFactory().getSessionFactory();
        EntityPersister cp = sf.getMetamodel().entityPersister(cls);
        if (!cp.hasCache()) {
            idsToDo.addAll(idsToCheck);
            return idsToDo;
        }

        CacheImplementor cache = sf.getCache();

        for (ID id : idsToCheck) {
            if (id == null) {
                continue;
            }
            if(!cache.containsEntity(cls, id)) {
                idsToDo.add(id);
            }
        }
        return idsToDo;
    }

    /**
     * Given a list of oids, this method will efficiently prime any objects not already in the cache.  It
     * also returns the list of objects not in the cache, but usually the return value of this function will be
     * discarded since its rather indeterminant as to which objects will be returned.
     *
     * @param ids A list of oids to prime
     * @return The objects that were not in the cache
     */
    public List<T> primeObjectsNotInCache(Collection<ID> ids) {
        try {
            Collection<ID> idsToDo = getObjectIDsNotInCache(ids);
            return getObjectsFromIDs(idsToDo);
        } catch (Throwable t) {
            // bl: deciding to instead swallow an exception when trying to prime objects not in the cache.
            // this way, even if the priming fails, it won't result in a completely broken page.
            logger.error("Unable to prime objects from cache of type " + cls, t);
            // just return an empty collection since the prime failed for one reason or another
            return new ArrayList<T>(0);
        }
    }

    private static final Map<Class<? extends DAOObject>, DAO> DAO_OBJECT_CLASS_TO_DAO_IMPL = new HashMap<>();
    private static final Map<Class<? extends DAOImpl>, DAO> DAO_CLASS_TO_DAO_IMPL = new HashMap<>();
    private static final Map<Class<? extends DAOObject>, DAO> DAO_OBJECT_DESCENDENT_CLASS_TO_DAO_IMPL = new HashMap<>();

    public static <T extends DAOObject, ID extends Serializable> void registerDAOImpl(DAOImpl<T, ID> daoImpl) {
        DAO_OBJECT_CLASS_TO_DAO_IMPL.put(daoImpl.getDAOObjectClass(), daoImpl);
        DAO_CLASS_TO_DAO_IMPL.put(daoImpl.getClass(), daoImpl);

        for (Class<? extends DAOObject> cls : daoImpl.getDAOObjectDescendents()) {
            DAO_OBJECT_DESCENDENT_CLASS_TO_DAO_IMPL.put(cls, daoImpl);
        }
    }

    public static <D extends DAO<O, ? extends Serializable>, O extends DAOObject<D>> D getDAO(Class<O> cls) {
        return (D) DAO_OBJECT_CLASS_TO_DAO_IMPL.get(cls);
    }

    public static <D extends DAO<O, ? extends Serializable>, O extends DAOObject<D>> D getDAOIncludingDescendents(Class<O> cls) {
        D dao = getDAO(cls);
        if (dao == null) {
            dao = (D) DAO_OBJECT_DESCENDENT_CLASS_TO_DAO_IMPL.get(cls);
        }
        return dao;
    }

    public static <T extends DAO> T getDAOFromDAOClass(Class<T> cls) {
        return (T) DAO_CLASS_TO_DAO_IMPL.get(cls);
    }

    public interface DAOObjectFilter<T extends DAOObject> {
        boolean includeObject(T t);
    }
}
