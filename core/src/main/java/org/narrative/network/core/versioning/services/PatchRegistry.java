package org.narrative.network.core.versioning.services;

import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.cluster.services.RestartFailedQuartzTriggersTask;
import org.narrative.network.core.propertyset.base.services.InstallDefaultPropertySets;
import org.narrative.network.core.quartz.services.InstallSystemCronJobs;
import org.narrative.network.core.security.area.community.advanced.AreaCirclePermission;
import org.narrative.network.core.security.area.community.advanced.AreaResourceType;
import org.narrative.network.core.system.InitDisplayResources;
import org.narrative.network.core.versioning.impl.NewVersionStandardPatchImpl;
import org.narrative.network.core.versioning.impl.RunForEveryPatchRunnerPatchImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 21, 2006
 * Time: 12:16:27 PM
 */
public class PatchRegistry {

    private static boolean isInitialized;

    /**
     * This is the list of patches which are to be applied, in order
     */
    public static synchronized void init() {
        if (!isInitialized) {
            registerPatches();
            isInitialized = true;
        }
    }

    public static synchronized boolean isInitialized() {
        return isInitialized;
    }

    /**
     * All patches should be registered here.  Order is important as this will determine the order in which the patches
     * area applied.  Patches must either implement DowntimePatch or StandardPatch.  If they implement DowntimePatch that
     * signifies that the patch is a breaking schema change that requires the entire network be down during application.
     * DowntimePatches can only be run if the inDowntime flag is set when executing PatchRunner.
     */
    private static void registerPatches() {
        //pm:don't remove or move.  We will always check to see if we should update functions
        PatchRunner.addPatch(new UpdateFunctions());
        // bl: now that wordlets are initialized, let's initialize the display resources such as static images,
        // the Default Theme, and CustomGraphicUsageType (which can rely on wordlets).
        PatchRunner.addPatch(new RunForEveryPatchRunnerPatchImpl("InitDisplayResources", PartitionType.GLOBAL) {
            public void applyPatch(Partition partition, Properties properties) {
                TaskRunner.doRootGlobalTask(new InitDisplayResources());
            }
        });

        // bootstrap patches

        // 1.5.1 Patches

        // bl: always want to run a patch on new versions to update the default property sets when we run the patch runner.
        // DO NOT REMOVE THE FOLLOWING PATCHES!!

        PatchRunner.addSchemaChangePatch(new NewVersionStandardPatchImpl("RemoveInvalidAreaCirclePermissions", PartitionType.GLOBAL) {
            public void applyPatch(Partition partition, Properties properties) {
                for (AreaResourceType resourceType : AreaResourceType.values()) {
                    AreaCirclePermission.dao().deleteInvalidPermissions(resourceType);
                }
            }
        });

        PatchRunner.addPatch(new NewVersionStandardPatchImpl("UpdateDefaultPropertySets", PartitionType.GLOBAL) {
            public void applyPatch(Partition partition, Properties properties) {
                InstallDefaultPropertySets.initializeDefaultPropertySets();
            }
        });

        PatchRunner.addPatch(new NewVersionStandardPatchImpl("RestartFailedQuartzTriggers", PartitionType.GLOBAL) {
            public void applyPatch(Partition partition, Properties properties) {
                TaskRunner.doRootGlobalTask(new RestartFailedQuartzTriggersTask());
            }
        });

        PatchRunner.addPatch(new NewVersionStandardPatchImpl("UpdateSystemCronJobs", PartitionType.GLOBAL) {
            public void applyPatch(Partition partition, Properties properties) {
                InstallSystemCronJobs.initializeSystemCronJobs();
            }
        });
    }
}
