package org.narrative.network.core.fileondisk.base.services;

import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.shared.tasktypes.AllPartitionsTask;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskOptions;

/**
 * Date: May 29, 2008
 * Time: 9:09:46 AM
 *
 * @author brian
 */
public class DeleteFileOnDisk extends GlobalTaskImpl<Object> {
    private final FileOnDisk fileOnDisk;
    private boolean dontDeleteFileOnDisk = false;

    public DeleteFileOnDisk(FileOnDisk fileOnDisk) {
        this.fileOnDisk = fileOnDisk;
    }

    protected Object doMonitoredTask() {
        // jw: due to the old cross posting features that we had, combined with the old ability to re-use previous
        //     uploaded files, we need to ensure that all usages of a FileOnDisk are removed prior to allowing it to
        //     be deleted.  So this code will check to make sure its not used in any of those old places.
        PartitionType.COMPOSITION.doTaskInAllPartitionsOfThisType(new TaskOptions(), new AllPartitionsTask<Object>(false) {
            protected Object doMonitoredTask() {
                if (!dontDeleteFileOnDisk) {
                    dontDeleteFileOnDisk = !FilePointer.dao().getAllForFileOnDisk(fileOnDisk).isEmpty();
                }
                return null;
            }
        });
        if (dontDeleteFileOnDisk) {
            return null;
        }

        FileOnDisk.dao().delete(fileOnDisk);

        // bl: flush all sessions to ensure data is updated and file deletion is handled before the task finishes execution
        PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

        return null;
    }
}
