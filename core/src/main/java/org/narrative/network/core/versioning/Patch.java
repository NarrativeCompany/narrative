package org.narrative.network.core.versioning;

import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 21, 2006
 * Time: 12:12:58 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Patch extends NamedPatch {
    PartitionType getPartitionType();

    Partition getPartition();

    void applyPatch(Partition partition, Properties data);
}
