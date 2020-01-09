package org.narrative.common.cache;

import org.narrative.common.persistence.DAOObject;
import org.hibernate.cache.CacheException;

import java.io.Serializable;

public interface Cache<K extends Serializable, T> {

    String getName();

    T get(K key) throws CacheException;

    void put(K key, T value, Class<? extends DAOObject>... invalidatingClasses) throws CacheException;

    void remove(K key);

    void clear() throws CacheException;
}
