package org.narrative.network.customizations.narrative.service.impl.file;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.content.base.UploadedFileData;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileOnDiskStatus;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.fileondisk.base.services.FileUploadUtils;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.services.RestUploadUtils;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.springframework.web.multipart.MultipartFile;

/**
 * Date: 2019-08-20
 * Time: 08:24
 *
 * @author brian
 */
public class UploadTempFileTask extends AreaTaskImpl<FileOnDisk> {
    private final FileUsageType fileUsageType;
    private final MultipartFile file;

    private UploadedFileData uploadedFileData;

    public UploadTempFileTask(FileUsageType fileUsageType, MultipartFile file) {
        this.fileUsageType = fileUsageType;
        this.file = file;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        // bl: for now, require the user be signed in. if we want to support this for guests (e.g. for avatar upload
        // during registration), we will have to re-think FileOnDisk, which requires a User
        getNetworkContext().getPrimaryRole().checkRegisteredUser();

        if(!fileUsageType.isSupportsTempFile()) {
            throw UnexpectedError.getRuntimeException("Can't upload temp files for FileUsageTypes that don't support it! fileUsageType/" + fileUsageType);
        }

        // create the UploadedFileData from the provided file.
        uploadedFileData = RestUploadUtils.getUploadedFileData(file, fileUsageType);

        FileType fileType = fileUsageType.getAllowedUploadFileType().isSingleFileType() ? fileUsageType.getAllowedUploadFileType().getLoneFileType() : null;

        // bl: for now, we only support image files
        assert fileType!=null && fileType.isImageFile() : "Only support temp image files for now!";

        // validate the uploaded file
        RestUploadUtils.validateUploadedFileData(getNetworkContext(), uploadedFileData, fileType, getValidationContext());
    }

    @Override
    protected FileOnDisk doMonitoredTask() {
        OID fileUploadProcessOid = uploadedFileData.getFileUploadProcessOid();
        // bl: process the file synchronously now
        uploadedFileData.postUploadProcess(fileUploadProcessOid);

        // bl: create the FileOnDisk
        FileOnDisk fileOnDisk = FileOnDisk.getNewFileOnDisk(uploadedFileData, getNetworkContext().getUser());

        // bl: set the status to TEMP_FILE since that's exactly what we're creating here!
        fileOnDisk.setStatus(FileOnDiskStatus.TEMP_FILE);

        // save the file on disk!
        FileOnDisk.dao().save(fileOnDisk);

        // bl: flush all sessions so that we save the files to GCP before we delete them all
        PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

        // bl: clean up even when in error. we don't want this sticking around beyond the duration of this request at all.
        FileUploadUtils.cleanUpFilesAfterFormCompletionAtEndOfPartitionGroup(fileUploadProcessOid, true);

        uploadedFileData.deleteAllTempFiles();

        return fileOnDisk;
    }
}
