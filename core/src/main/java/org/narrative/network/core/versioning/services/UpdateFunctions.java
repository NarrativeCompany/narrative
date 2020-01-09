package org.narrative.network.core.versioning.services;

import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.RunForNewVersionPatch;
import org.narrative.network.core.versioning.RunForRecordOnlyPatch;
import org.narrative.network.core.versioning.impl.StandardPatchImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Dec 7, 2007
 * Time: 10:33:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateFunctions extends StandardPatchImpl implements RunForRecordOnlyPatch, RunForNewVersionPatch {

    public UpdateFunctions() {
        super(PartitionType.GLOBAL);
    }

    public void applyPatch(Partition partition, Properties data) {
        TaskRunner.doRootGlobalTask(new UpdateFunctionsTask(data));
    }
}
