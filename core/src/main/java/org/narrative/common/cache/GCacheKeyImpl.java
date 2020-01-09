package org.narrative.common.cache;

import org.narrative.common.persistence.DAOObject;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

import java.io.Serializable;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Mar 9, 2006
 * Time: 2:39:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class GCacheKeyImpl implements Serializable, GCacheKey {
    private final Serializable id;
    private final Type idType;
    private final String entityName;
    private Serializable extraData;

    /**
     * Construct a new key for an entity instance.
     * Note that an entity name should always be the root entity
     * name, not a subclass entity name.
     */
    public GCacheKeyImpl(final Serializable id, EntityPersister entityPersister) {
        this.id = id;
        this.idType = entityPersister.getIdentifierType();
        this.entityName = entityPersister.getRootEntityName();
    }

    //Mainly for OSCache
    @Override
    public String toString() {
        return entityName + '#' + id.toString();//"CacheKey#" + type.toString(key, sf);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final GCacheKeyImpl gCacheKey = (GCacheKeyImpl) o;

        if (entityName != null ? !entityName.equals(gCacheKey.entityName) : gCacheKey.entityName != null) {
            return false;
        }
        if (id != null ? !id.equals(gCacheKey.id) : gCacheKey.id != null) {
            return false;
        }
        if (idType != null ? !idType.equals(gCacheKey.idType) : gCacheKey.idType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 29 * result + (idType != null ? idType.hashCode() : 0);
        result = 29 * result + (entityName != null ? entityName.hashCode() : 0);
        return result;
    }

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    @Override
    public Serializable getExtraData() {
        return extraData;
    }

    @Override
    public void setExtraData(Serializable obj) {
        extraData = obj;
    }

    @Override
    public boolean doesEntityNameRepresentClass(PartitionType partitionType, Class<? extends DAOObject> objectClass) {
        if (objectClass == null) {
            return false;
        }

        SessionFactoryImplementor impl = (SessionFactoryImplementor) partitionType.getGSessionFactory().getSessionFactory();
        EntityPersister ep = impl.getEntityPersister(objectClass.getName());

        return isEqual(ep.getRootEntityName(), getEntityName());
    }
}
