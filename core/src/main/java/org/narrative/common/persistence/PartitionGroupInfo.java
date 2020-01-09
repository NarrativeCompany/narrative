package org.narrative.common.persistence;

/**
 * Date: 11/19/14
 * Time: 10:05 AM
 *
 * @author brian
 */
public class PartitionGroupInfo {
    private static PartitionGroupOptions partitionGroupOptions;

    public static void init(PartitionGroupOptions options) {
        partitionGroupOptions = options;
    }

    public static PartitionGroupOptions getPartitionGroupOptions() {
        return partitionGroupOptions;
    }
}
