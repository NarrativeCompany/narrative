package org.narrative.network.core.versioning.impl;

import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.DowntimePatch;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 22, 2006
 * Time: 11:24:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class DowntimeQueryPatch extends QueryPatch implements DowntimePatch {
    public DowntimeQueryPatch(String patchName, String sql, PartitionType partitionType) {
        super(patchName, sql, partitionType);
    }
}
