package org.narrative.network.core.versioning.impl;

import org.narrative.common.persistence.hibernate.DatabaseResources;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Mar 20, 2006
 * Time: 11:47:37 PM
 */
public abstract class QueryPatch extends PatchImpl {

    private final String sql;

    protected QueryPatch(String patchName, String sql, PartitionType partitionType) {
        super(patchName, partitionType);
        this.sql = sql;
    }

    public void applyPatch(Partition partition, Properties data) {
        applyPatch(partition.getDatabaseResources());
    }

    protected void applyPatch(DatabaseResources resources) {
        try {
            resources.executeStatement(sql);
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Error running sql query patch: " + getName(), e, true);
        }
    }
}
