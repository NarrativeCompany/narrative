package org.narrative.network.core.versioning.impl;

import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.StandardPatch;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 22, 2006
 * Time: 10:08:06 AM
 */
public class StandardNamedQueryPatch extends NamedQueryPatch implements StandardPatch {
    public StandardNamedQueryPatch(String queryName, PartitionType partitionType) {
        super(queryName, partitionType);
    }

    public StandardNamedQueryPatch(String patchName, String queryName, PartitionType partitionType) {
        super(patchName, queryName, partitionType);
    }

}
