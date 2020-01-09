package org.narrative.network.core.propertyset.base;

import org.narrative.common.persistence.hibernate.EventListenerImpl;
import org.narrative.network.core.propertyset.base.dao.PropertySetDAO;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.EvictEvent;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;

/**
 * Date: Dec 9, 2005
 * Time: 1:52:14 PM
 *
 * @author Brian
 */
public class PropertyEventListener extends EventListenerImpl {

    public void onPostInsert(PostInsertEvent event) {
        PropertySetDAO.invalidatePropertySet(((Property) event.getEntity()).getPropertySet());
    }

    public void onPostUpdate(PostUpdateEvent event) {
        PropertySetDAO.invalidatePropertySet(((Property) event.getEntity()).getPropertySet());
    }

    public void onPostDelete(PostDeleteEvent event) {
        PropertySetDAO.invalidatePropertySet(((Property) event.getEntity()).getPropertySet());
    }

    public void onEvict(EvictEvent event) throws HibernateException {
        PropertySetDAO.invalidatePropertySet(((Property) event.getObject()).getPropertySet());
    }
}
