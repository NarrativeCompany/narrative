package org.narrative.network.core.versioning.impl;

import org.narrative.common.cache.CacheManager;
import org.narrative.common.persistence.hibernate.MultiStatementQueryFailure;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;

import java.util.Properties;

import static org.narrative.common.util.CoreUtils.*;

/**
 * If you need to execute multiple queries as part of a single patch,
 * you can create a MultiNamedQueryPatch to do the task.  For some
 * reason, allowMultiQueries wasn't working in MySQL (at least it wasn't
 * working when doing things like multiple alter table statements
 * in a single prepared statement.
 * <p>
 * To use this patch, supply a baseQueryName, e.q. QUERY_TO_RUN
 * Also supply a count of the total number of queries, e.g. 4
 * In this example, the following queries will be executed as part
 * of this patch:
 * QUERY_TO_RUN1
 * QUERY_TO_RUN2
 * QUERY_TO_RUN3
 * QUERY_TO_RUN4
 */
public abstract class MultiNamedQueryPatch extends PatchImpl {

    private final String baseQueryName;
    private static final String LAST_QUERY_KEY = "last_query_key";

    protected MultiNamedQueryPatch(String baseQueryName, PartitionType partitionType) {
        super(baseQueryName, partitionType);
        this.baseQueryName = baseQueryName;
    }

    public final void applyPatch(Partition partition, Properties data) {
        try {
            int firstQuery = 1;
            String firstQueryString = data.getProperty(LAST_QUERY_KEY);
            if (!isEmpty(firstQueryString)) {
                firstQuery = Integer.parseInt(firstQueryString);
            }
            partition.getDatabaseResources().executeMultiNamedStatements(baseQueryName, false, firstQuery, null);
        } catch (MultiStatementQueryFailure e) {
            data.setProperty(LAST_QUERY_KEY, String.valueOf(e.getFailedOnQueryNumber()));
            throw e;
        } finally {
            // bl: always flush all caches after executing SQL. just to be safe. we've run into patch issues in the
            // past where Hibernate had old data cached, and that can lead to patch issues, data consistency issues,
            // and other unintended and undesirable behavior :)
            CacheManager.clearAllCaches();
        }
    }
}
