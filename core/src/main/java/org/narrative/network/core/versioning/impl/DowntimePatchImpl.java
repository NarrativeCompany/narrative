package org.narrative.network.core.versioning.impl;

import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.DowntimePatch;

/**
 * Date: Sep 15, 2006
 * Time: 8:13:13 AM
 *
 * @author Brian
 */
public abstract class DowntimePatchImpl extends PatchImpl implements DowntimePatch {
    protected DowntimePatchImpl(String name, PartitionType partitionType) {
        super(name, partitionType);
    }

    protected DowntimePatchImpl(PartitionType partitionType) {
        super(partitionType);
    }

}
