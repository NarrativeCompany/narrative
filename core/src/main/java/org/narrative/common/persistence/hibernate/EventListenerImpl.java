package org.narrative.common.persistence.hibernate;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.EvictEvent;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Jun 13, 2005
 * Time: 10:08:51 AM
 * This is the abstract base class for any object wishing to monitor entity events for hibernate
 */
public abstract class EventListenerImpl implements EventListener {

    /*protected Object getPropertyValue(String propertyName, Object [] state, EntityPersister persister) {
        Debug.assertMsg(logger, persister.getPropertyNames().length == state.length, "state lenght and persister.propertyNames() lenght must be the same.  PropName/" + propertyName + " entityName/" + persister.getClassMetadata().getEntityName());
        for (int i = 0; i < persister.getPropertyNames().length; i++) {
            if (persister.getPropertyNames()[i].equals(propertyName))
                return state[i];
        }
        return null;
    }*/

    /**
     * get the original state of a given object
     *
     * @param cls   the class to get the original state for
     * @param event the PostUpdateEvent from which to get the original state for an event
     * @return the object containing the original state of an object
     */
    protected final <T> T getOriginalState(Class<T> cls, PostUpdateEvent event) {
        return getOriginalState(cls, event.getId(), event.getPersister(), event.getOldState(), event.getSession());
    }

    protected final <T> T getOriginalState(Class<T> cls, PreUpdateEvent event) {
        return getOriginalState(cls, event.getId(), event.getPersister(), event.getOldState(), event.getSession());
    }

    private <T> T getOriginalState(Class<T> cls, Serializable id, EntityPersister persister, Object[] oldState, SharedSessionContractImplementor sharedSessionContractImplementor) {
        T ret;
        try {
            ret = cls.newInstance();
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("Failed instantiating object of type " + cls + " to get original state of object on Hibernate event", t);
        }
        // bl: using POJO here since we currently aren't using any other entity modes.
        persister.setPropertyValues(ret, oldState);
        persister.setIdentifier(ret, id, sharedSessionContractImplementor);
        return ret;
    }

    @Override
    public void onPreLoad(PreLoadEvent event) {
    }

    @Override
    public void onPostLoad(PostLoadEvent event) {
    }

    @Override
    public void onEventListenerPreInsert(PreInsertEvent event) {
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
    }

    @Override
    public void onEventListenerPreUpdate(PreUpdateEvent event) {
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
    }

    @Override
    public void onEventListenerPreDelete(PreDeleteEvent event) {
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
    }

    @Override
    public void onEvict(EvictEvent event) throws HibernateException {
    }

    protected OID getIdFromObjectInState(EntityPersister entityPersister, Object[] state, String property) {
        OID output = null;
        int index = entityPersister.getEntityMetamodel().getPropertyIndex(property);

        if (state[index] != null) {
            output = ((DAOObject) state[index]).getOid();
        }
        return output;
    }

    /**
     * Does this listener require that after transaction hooks be registered?
     *
     * @param persister The persister for the entity in question.
     * @return {@code true} if after transaction callbacks should be added.
     * @deprecated use {@link #requiresPostCommitHandling(EntityPersister)}
     */
    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return true;
    }

    /**
     * Does this listener require that after transaction hooks be registered?
     *
     * @param persister The persister for the entity in question.
     * @return {@code true} if after transaction callbacks should be added.
     */
    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return true;
    }
}
