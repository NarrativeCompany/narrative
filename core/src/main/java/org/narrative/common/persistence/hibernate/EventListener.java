package org.narrative.common.persistence.hibernate;

import org.hibernate.event.spi.EvictEventListener;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.event.spi.PreUpdateEvent;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Jun 13, 2005
 * Time: 11:04:32 AM
 * To change this template use File | Settings | File Templates.
 */
public interface EventListener extends PreLoadEventListener, PostLoadEventListener, PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener, EvictEventListener {
    // bl: don't want to implement PreInsertEventListener, PreUpdateEventListener, and PreDeleteEventListener
    // here since they return a boolean veto value, but we don't need or want that (to avoid confusion).
    public void onEventListenerPreInsert(PreInsertEvent event);

    public void onEventListenerPreUpdate(PreUpdateEvent event);

    public void onEventListenerPreDelete(PreDeleteEvent event);
}
