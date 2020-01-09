package org.narrative.network.core.propertyset.area;

import org.narrative.common.persistence.hibernate.EventListenerImpl;
import org.narrative.network.core.propertyset.area.dao.AreaPropertySetDAO;
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
public class AreaPropertySetEventListener extends EventListenerImpl {

    public void onPostInsert(PostInsertEvent event) {
        AreaPropertySetDAO.invalidateAreaPropertySet(((AreaPropertySet) event.getEntity()));
    }

    public void onPostUpdate(PostUpdateEvent event) {
        AreaPropertySetDAO.invalidateAreaPropertySet(((AreaPropertySet) event.getEntity()));
    }

    public void onPostDelete(PostDeleteEvent event) {
        AreaPropertySetDAO.invalidateAreaPropertySet(((AreaPropertySet) event.getEntity()));
    }

    public void onEvict(EvictEvent event) throws HibernateException {
        AreaPropertySetDAO.invalidateAreaPropertySet(((AreaPropertySet) event.getObject()));
    }
}
