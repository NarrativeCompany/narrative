package org.narrative.network.core.versioning.impl;

import org.narrative.common.persistence.hibernate.DatabaseResources;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.BootstrapPatch;

import java.sql.SQLException;

/**
 * Date: Jun 5, 2006
 * Time: 3:45:24 PM
 *
 * @author Brian
 */
public class BootstrapNamedQueryPatch implements BootstrapPatch {
    private final String queryName;

    public BootstrapNamedQueryPatch(String queryName) {
        this.queryName = queryName;
    }

    public String getName() {
        return queryName;
    }

    public void applyPatch() {
        DatabaseResources resources = PartitionType.GLOBAL.getSingletonPartition().getBootstrapDatabaseResources();
        try {
            resources.executeNamedStatement(queryName);
        } catch (SQLException sqle) {
            throw UnexpectedError.getRuntimeException("Failed executing bootstrap named query patch: " + queryName, sqle, true);
        }
    }
}
