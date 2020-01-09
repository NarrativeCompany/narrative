package org.narrative.network.core.search.services;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.EventListener;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.search.IndexHandlerManager;
import org.narrative.network.core.search.IndexOperation;
import org.narrative.network.core.search.IndexOperationId;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.hibernate.HibernateException;
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

/**
 * User: Barry
 */
public class SearchItemEventListener implements EventListener {

    private static final ThreadLocal<Boolean> indexOn = new ThreadLocal<Boolean>();

    private static boolean isIndexOn() {
        return indexOn.get() == null || indexOn.get();
    }

    /**
     * Turns off indexing.  Good for big metadata rebuilds.
     *
     * @param setOff true to turn live indexing off
     */
    public static void turnOffLiveIndexing(boolean setOff) {
        if (!setOff) {
            SearchItemEventListener.indexOn.remove();
        } else {
            SearchItemEventListener.indexOn.set(!setOff);
        }
    }

    public void onPostInsert(PostInsertEvent event) {
        //if it hasn't been initted yet, then we don't even have the indexes to update
        if (!isIndexOn() || !IndexHandlerManager.isInitialized()) {
            return;
        }
        if (NetworkRegistry.getInstance().isImporting()) {
            return;
        }

        if (event.getEntity() instanceof DAOObject) {
            final OID oid = (OID) event.getId();
            final IndexType indexType = getIndexType((DAOObject) event.getEntity());
            if (indexType != null) {
                sendIndexUpdateMessage(indexType, IndexOperation.Type.UPDATE, oid, null);

            } else if (event.getEntity() instanceof Niche) {
                Niche niche = (Niche) event.getEntity();
                sendIndexUpdateMessage(IndexType.NICHE, IndexOperation.Type.UPDATE, oid, niche.getArea().getOid());

            } else if (event.getEntity() instanceof Publication) {
                // bl: don't have to set the area OID here as extraDataOid. the Narrative platform area is always used now.
                sendIndexUpdateMessage(IndexType.PUBLICATION, IndexOperation.Type.UPDATE, oid, null);

            }
        }
    }

    public void onPostUpdate(PostUpdateEvent event) {
        if (NetworkRegistry.getInstance().isImporting()) {
            return;
        }

        if (event.getEntity() instanceof DAOObject) {
            final OID oid = (OID) event.getId();
            final IndexType indexType = getIndexType((DAOObject) event.getEntity());
            if (indexType != null) {
                sendIndexUpdateMessage(indexType, IndexOperation.Type.UPDATE, oid, null);

            } else if (event.getEntity() instanceof Niche) {
                Niche niche = (Niche) event.getEntity();
                sendIndexUpdateMessage(IndexType.NICHE, IndexOperation.Type.UPDATE, oid, niche.getArea().getOid());

            } else if (event.getEntity() instanceof Publication) {
                sendIndexUpdateMessage(IndexType.PUBLICATION, IndexOperation.Type.UPDATE, oid, null);

            }
        }

    }

    public void onPostDelete(PostDeleteEvent event) {
        if (NetworkRegistry.getInstance().isImporting()) {
            return;
        }

        if (event.getEntity() instanceof DAOObject) {
            final OID oid = (OID) event.getId();
            final IndexType indexType = getIndexType((DAOObject) event.getEntity());
            if (indexType != null) {
                sendIndexUpdateMessage(indexType, IndexOperation.Type.DELETE, oid, null);

            } else if (event.getEntity() instanceof Niche) {
                Niche niche = (Niche) event.getEntity();
                sendIndexUpdateMessage(IndexType.NICHE, IndexOperation.Type.DELETE, oid, niche.getArea().getOid());

            } else if (event.getEntity() instanceof Publication) {
                sendIndexUpdateMessage(IndexType.PUBLICATION, IndexOperation.Type.DELETE, oid, null);

            } else if (event.getEntity() instanceof Content) {
                Content content = (Content) event.getEntity();
                sendIndexUpdateMessage(IndexType.CONTENT, IndexOperation.Type.DELETE, oid, content.getArea().getOid());

            } else if (event.getEntity() instanceof Reply) {
                sendIndexUpdateMessage(IndexType.REPLY, IndexOperation.Type.DELETE, oid, PartitionType.COMPOSITION.currentSession().getPartitionOid());
            }
        }
    }

    // bk: should this be in the IndexHandlerManager or the IndexType classes?
    private static IndexType getIndexType(DAOObject daoObject) {
        if (daoObject instanceof User) {
            return IndexType.USER;
        }

        return null;
    }

    private void sendIndexUpdateMessage(final IndexType indexType, final IndexOperation.Type operation, final OID oid, final OID extraDataOID) {
        PartitionGroup.addEndOfPartitionGroupRunnable(() -> {
            try {
                if (extraDataOID == null) {
                    indexType.getIndexHandler().performOperation(new IndexOperation(operation, new IndexOperationId(oid)));
                } else {
                    indexType.getIndexHandler().performOperation(new IndexOperation(operation, new IndexOperationId(oid, extraDataOID)));
                }
            } catch (Exception e) {
                throw UnexpectedError.getRuntimeException("Unable to write index for indexType:" + indexType, e);
            }
        });
    }

    //Not Implemented
    public void onPreLoad(PreLoadEvent event) {}

    public void onPostLoad(PostLoadEvent event) {}

    public void onEventListenerPreUpdate(PreUpdateEvent event) {}

    public void onEventListenerPreInsert(PreInsertEvent event) {}

    public void onEventListenerPreDelete(PreDeleteEvent event) {}

    public void onEvict(EvictEvent event) throws HibernateException {}

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
