package org.narrative.network.core.versioning.impl;

import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.StandardPatch;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 22, 2006
 * Time: 10:08:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class StandardMultiNamedQueryPatch extends MultiNamedQueryPatch implements StandardPatch {
    public StandardMultiNamedQueryPatch(String baseQueryName, PartitionType partitionType) {
        super(baseQueryName, partitionType);
    }
}
