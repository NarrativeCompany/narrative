package org.narrative.network.core.versioning.impl;

import org.narrative.common.cache.CacheManager;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 21, 2006
 * Time: 2:29:59 PM
 */
public abstract class NamedQueryPatch extends PatchImpl {

    private String queryName;

    protected NamedQueryPatch(String queryName, PartitionType partitionType) {
        super(queryName, partitionType);
        this.queryName = queryName;
    }

    protected NamedQueryPatch(String patchName, String queryName, PartitionType partitionType) {
        super(patchName, partitionType);
        this.queryName = queryName;
    }

    public final void applyPatch(Partition partition, Properties data) {
        try {
            partition.getDatabaseResources().executeNamedStatement(queryName);
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Error running named query patch: " + getName(), e, true);
        } finally {
            // bl: always flush all caches after executing SQL. just to be safe. we've run into patch issues in the
            // past where Hibernate had old data cached, and that can lead to patch issues, data consistency issues,
            // and other unintended and undesirable behavior :)
            CacheManager.clearAllCaches();
        }
    }
}