package org.narrative.network.core.versioning.impl;

import org.narrative.common.persistence.hibernate.DatabaseResources;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.BootstrapPatch;

import java.sql.SQLException;

/**
 * Date: Jun 5, 2006
 * Time: 3:46:04 PM
 *
 * @author Brian
 */
public class BootstrapQueryPatch implements BootstrapPatch {
    private final String sql;

    public BootstrapQueryPatch(String sql) {
        this.sql = sql;
    }

    public String getName() {
        return sql;
    }

    public void applyPatch() {
        DatabaseResources resources = PartitionType.GLOBAL.getSingletonPartition().getBootstrapDatabaseResources();
        try {
            resources.executeStatement(sql);
        } catch (SQLException sqle) {
            throw UnexpectedError.getRuntimeException("Failed executing bootstrap query patch: " + sql, sqle, true);
        }
    }
}
