package org.narrative.network.core.fileondisk.base.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPDateUtil;
import org.narrative.network.core.content.base.FileData;
import org.narrative.network.core.quartz.NetworkJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * User: barry
 * Date: Mar 8, 2010
 * Time: 4:19:08 PM
 */
@DisallowConcurrentExecution
public class CleanUpOldFileDataJob extends NetworkJob {
    private final long olderThanMsAgo = (long) 2 * IPDateUtil.HOUR_IN_MS;

    @Deprecated //Quartz Only
    public CleanUpOldFileDataJob() {}

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        Set<OID> primaryRoleOids = newHashSet(FileUploadUtils.PRIMARY_ROLE_OID_TO_FILES_IN_USE.keySet());
        Set<OID> primaryRoleOidsToRemove = newHashSet();

        for (OID primaryRoleOid : primaryRoleOids) {
            synchronized (FileUploadUtils.PRIMARY_ROLE_OID_LOCK_MANAGER.getSyncObject(primaryRoleOid)) {
                Map<OID, FileUploadUtils.FileUploadProcessOidData<FileData>> fileUploadProcessOidToData = FileUploadUtils.getAllFormFileDataForUser(primaryRoleOid, false);
                if (!isEmptyOrNull(fileUploadProcessOidToData)) {
                    Collection<FileUploadUtils.FileUploadProcessOidData<FileData>> datas = newArrayList(fileUploadProcessOidToData.values());
                    for (FileUploadUtils.FileUploadProcessOidData<FileData> fileUploadProcessOidData : datas) {
                        if ((olderThanMsAgo + fileUploadProcessOidData.fileUploadProcessOidCreatedDatetime) < System.currentTimeMillis()) {
                            // clean up after form completion will delete the files on the local disk and also
                            // remove the fileUploadProcessOid from the map.
                            FileUploadUtils.cleanUpAfterFormCompletion(primaryRoleOid, fileUploadProcessOidData.fileUploadProcessOid);
                        }
                    }
                } else {
                    primaryRoleOidsToRemove.add(primaryRoleOid);
                }
            }
        }

        for (OID primaryRoleOid : primaryRoleOidsToRemove) {
            synchronized (FileUploadUtils.PRIMARY_ROLE_OID_LOCK_MANAGER.getSyncObject(primaryRoleOid)) {
                Map map = FileUploadUtils.getAllFormFileDataForUser(primaryRoleOid, false);
                if (isEmptyOrNull(map)) {
                    FileUploadUtils.PRIMARY_ROLE_OID_TO_FILES_IN_USE.remove(primaryRoleOid);
                }
            }
        }
    }
}
