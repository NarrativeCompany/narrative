package org.narrative.common.cache;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.hibernate.GSessionFactory;
import org.narrative.common.util.LRUMap;
import org.narrative.config.StaticConfig;
import org.narrative.config.cache.RedissonConfig;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.*;

class CacheImplTest {
    private static final String CACHE_NAME = "cacheName";

    private Collection<GSessionFactory> sfs;

    @Mocked
    private GSessionFactory sessionFactory;

    @Mocked
    private RedissonClient client;

    @Tested
    private CacheImpl<Integer, String> cacheImpl;

    @BeforeEach
    void beforeEach() {
        sfs = new HashSet<>();
        sfs.add(sessionFactory);

        new Expectations(StaticConfig.class){{
            StaticConfig.getBean(RedissonConfig.REDISSON_CLIENT_BEAN_NAME, RedissonClient.class);
            result=client;
        }};

        cacheImpl = new CacheImpl<>(CACHE_NAME, sfs);
    }

    // Test names should follow the standard: MethodName_StateUnderTest_ExpectedBehavior
    @Test
    void getName_nameIsSet_returnsNameOfCache() {
        assertEquals(CACHE_NAME, cacheImpl.getName());
    }

    @Test
    void get_keyIsInCache_returnsValue() {
        Integer key = 1;
        String value = "value";

        new Expectations(LRUMap.class) {{
            cacheImpl.cache.get(key);
            result = value;
        }};

        String retval = cacheImpl.get(key);

        assertEquals(value, retval);

    }

    @Test
    void get_keyIsNotInCache_returnsNull() {
        Integer key = 1;

        new Expectations(LRUMap.class) {{
            cacheImpl.cache.get(key);
            result = null;
        }};

        String retval = cacheImpl.get(key);

        assertNull(retval);

    }

    @Test
    void put_givenKeyAndValueNoInvalidatingClasses_callsPutOnUnderlyingCache() {
        Integer key = 1;
        String value = "value";

        new Expectations(LRUMap.class) {{
            cacheImpl.cache.put(key, value);
        }};

        cacheImpl.put(key, value);

    }

    @Test
    void put_givenKeyAndValueInvalidatingClass_callsPutOnUnderlyingCache(@Mocked DAOObject daoObject) {
        Integer key = 1;
        String value = "value";

        new Expectations(LRUMap.class) {{
            cacheImpl.cache.put(key, value);
        }};

        HashSet<String> entityNamesForInvalidatingClasses = new HashSet<>();
        entityNamesForInvalidatingClasses.add("Entity");

        new Expectations(CacheImpl.class) {{
            cacheImpl.getEntityNamesFromInvalidatingClasses((Collection) any);
            result = entityNamesForInvalidatingClasses;

        }};

        cacheImpl.put(key, value, daoObject.getClass());

    }

    @Test
    void put_givenKeyAndRemoveInterceptor_callsPutOnUnderlyingCache() {
        // Cache should have a removeInterceptor
        cacheImpl = new CacheImpl<>(CACHE_NAME, sfs);

        Integer key = 1;
        String value = "value";

        new Expectations(LRUMap.class) {{
            cacheImpl.cache.put(key, value);
        }};

        cacheImpl.put(key, value);
    }

    @Test
    void getSessionFactory_existsInSfEntityLookup_returnsSessionFactory(@Mocked GSessionFactory sessionFactory) {
        final String SESSION_FACTORY_NAME = "sessionFactory";

        CacheImpl.sfEntityLookup.put(SESSION_FACTORY_NAME, sessionFactory);
        assertEquals(sessionFactory, cacheImpl.getSessionFactory(SESSION_FACTORY_NAME));
    }

    @Test
    void getSessionFactory_notInSfEntityLookup_returnsSessionFactory() {
        final String SESSION_FACTORY_NAME = "sessionFactory";

        GSessionFactory sessionFactory = cacheImpl.getSessionFactory(SESSION_FACTORY_NAME);

        // A session factory was created and added to the collection
        assertEquals(1, cacheImpl.sfs.size());
        // Session factory is in the lookup now
        assertTrue(CacheImpl.sfEntityLookup.containsKey(SESSION_FACTORY_NAME));
    }

    @Test
    void remove_callsRemoveKeyFromCache_noException() {
        Integer key = 1;

        new Expectations(CacheImpl.class) {{
            cacheImpl.removeKeyFromCache(key);
        }};

        cacheImpl.remove(key);
    }

    @Test
    void removeKeyFromCache_givenKey_removesKeyFromCache(){
        Integer key = 1;

        new Expectations(CacheImpl.class){{
            cacheImpl.cache.remove(key);
            cacheImpl.removeKeyFromKeyCaches(key);
        }};

        cacheImpl.removeKeyFromCache(key);
    }

    @Test
    void clear_clearsCache_callsClearOnInternalCache(){

        new Expectations(CacheImpl.class){{
            cacheImpl.cache.clear();
        }};

        cacheImpl.clear();
    }


    @Test
    void clear() {
    }
}