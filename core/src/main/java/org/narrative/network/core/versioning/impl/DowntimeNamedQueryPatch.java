package org.narrative.network.core.versioning.impl;

import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.DowntimePatch;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 22, 2006
 * Time: 10:07:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class DowntimeNamedQueryPatch extends NamedQueryPatch implements DowntimePatch {
    public DowntimeNamedQueryPatch(String queryName, PartitionType partitionType) {
        super(queryName, partitionType);
    }
}
