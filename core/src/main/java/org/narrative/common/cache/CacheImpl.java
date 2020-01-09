package org.narrative.common.cache;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.hibernate.GSessionFactory;
import org.narrative.common.util.Debug;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.config.cache.RedissonConfig;
import org.narrative.config.StaticConfig;
import org.hibernate.cache.CacheException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.redisson.api.RedissonClient;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Jun 11, 2005
 * Time: 3:42:26 PM
 */
public class CacheImpl<K extends Serializable, T> implements Cache<K, T> {

    protected static final NarrativeLogger logger = new NarrativeLogger(CacheImpl.class);

    protected static final Map<String, GSessionFactory> sfEntityLookup = newConcurrentHashMap();

    protected final ConcurrentMap<K, T> cache;
    protected final Map<String, Set<K>> entityNameToCacheKeys = new HashMap<>();
    protected final Collection<GSessionFactory> sfs;
    protected final String name;

    CacheImpl(String name, Collection<GSessionFactory> sfs) {
        RedissonClient client = null;
        try {
            // If we're not in a Spring context, this will throw an NPE
            client = StaticConfig.getBean(RedissonConfig.REDISSON_CLIENT_BEAN_NAME, RedissonClient.class);
        } catch (NullPointerException e) {
            logger.warn("Executing outside of Spring Context");
        }

        if (client!=null){
            // In a Spring context, use the Redisson client to get an RMap
            this.cache = client.getMap(name);
        } else {
            // Not in a Spring context so just use a local HashMap
            this.cache = new ConcurrentHashMap<>();
        }

        this.sfs = sfs;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T get(K key) throws CacheException {
        return cache.get(key);
    }

    /**
     * Adds an item to the cache
     *
     * @param key                 The key to look up the cache item with
     * @param value               The value of the cache item
     * @param invalidatingClasses Classes of which data objects are contained within the cache value.  If those
     *                            underlying classes change, this cache item will be invalidated
     * @throws CacheException
     */
    @Override
    public void put(K key, T value, Class<? extends DAOObject>... invalidatingClasses) throws CacheException {

        final Set<String> allInvalidatingClassEntityNames;
        // provide an opportunity for the caller to specify the list of invalidating objects
        // currently we are only using this over ride in one place, and its to specify one class
        // where the invalidator is using two.
        if (invalidatingClasses != null && invalidatingClasses.length > 0) {
            allInvalidatingClassEntityNames = getEntityNamesFromInvalidatingClasses(Arrays.asList(invalidatingClasses));
        } else {
            allInvalidatingClassEntityNames = Collections.emptySet();
        }

        //for all of the invalidating objects, create a mapping to the cache entry
        for (String entityName : allInvalidatingClassEntityNames) {
            //get the entity mapping for the class
            Set<K> cacheKeys = entityNameToCacheKeys.get(entityName);
            if (cacheKeys == null) {
                //no mapping for this class yet, so create it
                synchronized (entityNameToCacheKeys) {
                    cacheKeys = entityNameToCacheKeys.get(entityName);
                    if (cacheKeys == null) {
                        entityNameToCacheKeys.put(entityName, cacheKeys = newConcurrentHashSet());
                    }
                }
            }

            cacheKeys.add(key);
        }

        //finally, add the value to the cache
        cache.put(key, value);
    }

    protected Set<String> getEntityNamesFromInvalidatingClasses(Collection<Class<? extends DAOObject>> invalidatingClasses) {
        Set<String> ret = newLinkedHashSet();
        for (Class<? extends DAOObject> cls : invalidatingClasses) {
            if (cls == null) {
                continue;
            }
            //is class a persistent class
            GSessionFactory sf = getSessionFactory(cls.getName());
            Debug.assertMsg(logger, sf != null, "Class " + cls.getName() + " is not a persistent hibernate class and cannot be used for cache invalidation.");
            SessionFactoryImplementor impl = (SessionFactoryImplementor) sf.getSessionFactory();
            EntityPersister ep = impl.getEntityPersister(cls.getName());
            Debug.assertMsg(logger, ep != null, "Class " + cls.getName() + " is not a persistent hibernate class and cannot be used for cache invalidation.");
            assert ep != null;
            ret.add(ep.getRootEntityName());
        }
        return ret;
    }

    protected GSessionFactory getSessionFactory(String name) {
        GSessionFactory sf = sfEntityLookup.get(name);
        if (sf == null) {
            synchronized (sfEntityLookup) {
                sf = sfEntityLookup.get(name);
                if (sf == null) {
                    for (GSessionFactory gsf : sfs) {
                        if (gsf.getSessionFactory().getClassMetadata(name) != null) {
                            sf = gsf;
                            sfEntityLookup.put(name, gsf);
                            break;
                        }
                    }
                }
            }
        }
        return sf;
    }

    @Override
    public void remove(K key) {
        removeKeyFromCache(key);
    }

    protected void removeKeyFromCache(K key) {
        cache.remove(key);
        removeKeyFromKeyCaches(key);
    }

    protected void removeKeyFromKeyCaches(K key) {
        // bl: when items are removed from the cache, remove the key from the entityNameToCacheKeys map, too.
        // otherwise, that map is just going to grow over time to contain all keys in the system (un-good
        // from a memory usage standpoint and from a processing standpoint when processing cache invalidations).
        for (Set<K> keys : entityNameToCacheKeys.values()) {
            keys.remove(key);
        }
    }

    public synchronized void clear() throws CacheException {
        cache.clear();
        synchronized (entityNameToCacheKeys) {
            entityNameToCacheKeys.clear();
        }
    }

}
