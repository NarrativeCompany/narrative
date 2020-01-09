package org.narrative.common.cache;

import org.narrative.common.persistence.GSessionContainer;
import org.narrative.common.persistence.hibernate.EventListenerImpl;
import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.persistence.hibernate.GSessionFactory;
import org.hibernate.Session;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.EvictEvent;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Jun 11, 2005
 * Time: 7:38:19 PM
 * This class is used to create caches.  The cache entries will be invalidated based on hibernate objects that are
 * references during the "put" in the cache.  Any changes to the classes specified in the put will cause an invalidation
 * of the cache entry.
 */
@Component
public class CacheManager {
    private static final Map<String, Cache> caches = new ConcurrentHashMap<>();
    private static final Map<GSessionFactory, CacheListener> cacheListeners = new HashMap<>();

    public static final int DEFAULT_MAX_CACHE_SIZE = 1000;

    private static GSessionContainer gSessionContainer;

    public static void init() {
    }

    public static void setSessions(GSessionContainer gsc) {
        gSessionContainer = gsc;
    }

    /**
     * Creates a cache with the default size
     *
     * @param name The name of the cache
     * @return The cache
     */
    public static <K extends Serializable, V> Cache<K, V> createCache(String name) {
        return createCache(name, Collections.<GSessionFactory>emptySet());
    }

    /**
     * Creates a cache that will grow until it hits a target hit rate
     *
     * @param name    The name of the cache
     * @return The cache
     */
    public static <K extends Serializable, V> Cache<K, V> createCache(String name, Collection<GSessionFactory> sfs) {
        assert !caches.containsKey(name) : "Cache " + name + " already in use.";

        Cache<K, V> cache = new CacheImpl<>(name, sfs);
        caches.put(name, cache);
        synchronized (cacheListeners) {
            for (GSessionFactory sf : sfs) {
                if (!cacheListeners.containsKey(sf)) {
                    CacheListener cl = new CacheListener();
                    cacheListeners.put(sf, cl);
                    sf.registerGlobalEventListener(cl);
                }
            }
        }
        return cache;
    }

    /**
     * Invalidates all the cache entries that contain data from this object
     *
     * @param entity
     */
    public static void invalidateObject(Object entity, Session sess) {

        SessionFactoryImplementor impl = (SessionFactoryImplementor) sess.getSessionFactory();
        Class cls = entity.getClass();
        if (entity instanceof HibernateProxy) {
            cls = cls.getSuperclass();
        }

        //handle any single entity invalidation
        EntityPersister ep = impl.getEntityPersister(cls.getName());
        if (ep == null) {
            return;
        }
        Serializable key = ep.getIdentifier(entity, (SharedSessionContractImplementor) sess);
        GCacheKey gCacheKey = new GCacheKeyImpl(key, ep);
    }

    /**
     * Marks all collection entries in the current persistence context as processed.
     * bl: the purpose here is to avoid "collection not processed by flush" errors.  found this solution here:
     * http://opensource.atlassian.com/projects/hibernate/browse/HHH-2763
     */
    private static void markCollectionsAsProcessed() {
        // bl: not ideal since we are accessing network code here, but this is the most optimal way that
        // we can implement this for now.  the benefit is that we only have to mark the CollectionEntry
        // objects as processed once for every invalidated Hibernate object that was used by one of the
        // CacheRemoveInterceptors.
        Collection<GSession> sessions = gSessionContainer.getSessions();
        for (GSession gSession : sessions) {
            Session session = gSession.getSession();
            if (session instanceof SessionImplementor) {
                SessionImplementor sessionImpl = (SessionImplementor) session;
                final PersistenceContext persistenceContext = sessionImpl.getPersistenceContext();
                final Map collectionEntries = persistenceContext.getCollectionEntries();
                final Collection values = collectionEntries.values();
                for (Object obj : values) {
                    CollectionEntry ce = (CollectionEntry) obj;
                    ce.setProcessed(true);
                }
            }
        }
    }

    /**
     * Invalidates all the cache entires that contain data from an object of this class with this id
     *
     * @param entityClass
     * @param id
     */
    public static void invalidateObject(Class entityClass, Serializable id, GSession gSession) {
        Session sess = gSession.getSession();
        SessionFactoryImplementor impl = (SessionFactoryImplementor) sess.getSessionFactory();
        EntityPersister ep = impl.getEntityPersister(entityClass.getName());
        if (ep == null) {
            return;
        }

        GCacheKey gCacheKey = new GCacheKeyImpl(id, ep);


    }

    public static class CacheListener extends EventListenerImpl {
        @Override
        public void onPostUpdate(PostUpdateEvent event) {
            CacheManager.invalidateObject(event.getEntity(), event.getSession());
        }

        @Override
        public void onPostInsert(PostInsertEvent event) {
            CacheManager.invalidateObject(event.getEntity(), event.getSession());
        }

        @Override
        public void onPostDelete(PostDeleteEvent event) {
            CacheManager.invalidateObject(event.getEntity(), event.getSession());
        }

        @Override
        public void onEvict(EvictEvent event) {
            CacheManager.invalidateObject(event.getObject(), event.getSession());
        }
    }

    public static Cache getCache(String name) {
        if (caches.get(name) == null){
            createCache(name);
        }
        return caches.get(name);
    }

    public static void destroyCache(String name) {
        Cache cache = caches.remove(name);
        if (cache != null) {
            cache.clear();
        }
    }

    public static void clearCache(String name) {
        clearCache(getCache(name));
    }

    public static void clearCache(Cache cache) {
        clearCacheInternally(cache);

    }

    public static void clearAllCaches() {
        clearAllCachesInternally();
    }

    private static void clearCacheInternally(Cache cache) {
        cache.clear();
    }

    private static void clearAllCachesInternally() {
        for (Cache cache : caches.values()) {
            clearCacheInternally(cache);
        }
    }

}
