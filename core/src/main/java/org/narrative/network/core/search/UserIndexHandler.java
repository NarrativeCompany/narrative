package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.ScrollableResults;

import java.util.Collection;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Sep 11, 2008
 * Time: 12:44:07 PM
 *
 * @author brian
 */
public class UserIndexHandler extends SimpleIndexHandler {

    private static final NetworkLogger logger = new NetworkLogger(UserIndexHandler.class);

    // bl: it seemed like Hibernate would bog down too much when doing 1000 per chunk. lowering to 100 users per chunk.
    private static final int USERS_PER_CHUNK = 100;

    public UserIndexHandler() {
        super(IndexType.USER);
    }

    private static final class UserIndexRebuildStats {
        private OID lastOid = new OID(0);
        private int rowsWritten = 0;
        private int rowsSkipped = 0;
    }

    public void rebuildSolrIndex(final long indexVersion) {
        final UserIndexRebuildStats stats = new UserIndexRebuildStats();
        while (true) {
            int count = TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Integer>(false) {
                protected Integer doMonitoredTask() {
                    final List<User> users = User.dao().getIndexRecordChunked(stats.lastOid, USERS_PER_CHUNK);

                    Collection<SolrInputDocument> documents = newLinkedList();
                    for (User user : users) {
                        SolrInputDocument document = createUserDocument(user, indexVersion);

                        if (document != null) {
                            documents.add(document);
                            addDocumentChunkSafely(documents, user.getOid());
                            stats.rowsWritten++;
                        } else {
                            stats.rowsSkipped++;
                        }

                        stats.lastOid = user.getOid();
                    }

                    addDocumentsSafely(documents);

                    return users.size();
                }
            });

            ProcessManager.getInstance().getCurrentProcess().updateStatusMessageAndLog(logger, "Finished writing indexes for " + stats.rowsWritten + " users (" + stats.rowsSkipped + " skipped).");

            if (count < USERS_PER_CHUNK) {
                break;
            }
        }

        ProcessManager.getInstance().getCurrentProcess().updateStatusMessageAndLog(logger, "Finished writing indexes for users. (" + stats.rowsWritten + " records written, " + stats.rowsSkipped + " skipped)");
    }

    @Override
    protected SolrInputDocument createSolrInputDocument(IndexOperationId id, long indexVersion, boolean forBulkOperation) {
        User user = User.dao().get(id.getDocId());
        if (exists(user)) {
            return createUserDocument(user, indexVersion);
        }

        return null;
    }

    protected SolrInputDocument createUserDocument(final User user, final long indexVersion) {
        return networkContext().doAuthZoneTask(user.getAuthZone(), new GlobalTaskImpl<SolrInputDocument>(false) {
            @Override
            protected SolrInputDocument doMonitoredTask() {
                SolrInputDocument document = UserIndexHandler.super.createDefaultDocument(user.getOid(), user.getDisplayNameResolved(), user.getProfileKeywords(), user.getUserFields().getRegistrationDate(), user.getAuthZone(), indexVersion);
                // bl: add all of the groups that this user belongs to.
                for (AreaUser areaUser : user.getAreaUsers()) {
                    // bl: a little bit of a hack, but let's index users by all of the Areas that they belong to (including
                    // the main site). this way, we can use the same code to query by user
                    // in AreaSearcherTask.
                    document.addField(AreaDataIndexHandlerBase.FIELD__COMMON__AREA_OID, areaUser.getArea().getOid().getValue());
                }

                return document;
            }
        });
    }

    @Override
    protected SearchResult getResultFromDocument(SolrDocument document, OID oid, int resultIndex, String name) {
        return new UserSearchResult(oid, resultIndex);
    }

    protected NetworkDAOImpl getNetworkDAO() {
        return User.dao();
    }

    protected ScrollableResults getAllIdsScrollable() {
        return User.dao().getAllNonDeletedScrollable();
    }

}
