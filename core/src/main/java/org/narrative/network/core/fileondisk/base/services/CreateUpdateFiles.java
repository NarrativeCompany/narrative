package org.narrative.network.core.fileondisk.base.services;

import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.content.base.ExistingFileData;
import org.narrative.network.core.content.base.ExistingVideoFileData;
import org.narrative.network.core.content.base.FileData;
import org.narrative.network.core.content.base.UploadedFileData;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.video.VideoOnDisk;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * CreateFilesOnDisk handles a bit of massaging of the FileData objects supplied to it.  It will
 * call scrub() on each FileData to disable any HTML, etc.  Additionally, CreateFilesOnDisk
 * will also track how many new files were added and how many characters were changed in
 * the title and description of the FileDatas.
 * <p>
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Jan 26, 2006
 * Time: 7:44:54 PM
 */
public class CreateUpdateFiles extends GlobalTaskImpl<List<FileOnDisk>> {
    private final Collection<? extends FileData> fileDatas;
    private final User user;
    private int filesAdded = 0;
    private boolean charsChanged = false;

    public CreateUpdateFiles(Collection<? extends FileData> fileDataList, User user) {
        fileDatas = fileDataList;
        this.user = user;
    }

    protected List<FileOnDisk> doMonitoredTask() {
        List<FileOnDisk> fileList = new ArrayList<>(fileDatas.size());

        for (FileData fileData : fileDatas) {
            if (fileData == null) {
                continue;
            }
            if (!fileData.isInclude()) {
                continue;
            }

            // scrub the file data in a central place.  enables us to do this just once, even when posting content to multiple areas.
            fileData.scrub();

            // track how many new files are being added, based on the isNew flag on FileData
            if (fileData.isNew()) {
                filesAdded++;
            }

            // track if any chars were changed in the title/description fields of the files
            if (fileData.isCharsChanged()) {
                charsChanged = true;
            }

            FileOnDisk fod = null;
            if (fileData instanceof ExistingFileData) {
                ExistingFileData existingFileData = (ExistingFileData) fileData;
                fod = existingFileData.getFileOnDisk();
                if (exists(fod)) {
                    fileList.add(fod);
                }
            } else {
                assert fileData instanceof UploadedFileData : "Found a FileData that wasn't an ExistingFileData or an UploadedFileData!";
                final UploadedFileData uploadedFileData = (UploadedFileData) fileData;
                // bl: updated so that in case the same FileOnDisk record exists in the collection twice,
                // we will only store the FileOnDisk record a single time.  this is handled by checking
                // the fileOnDiskOid for the file data, which will be set as soon as the FileOnDisk
                // object is created.
                if (uploadedFileData.getTempFile() != null && uploadedFileData.getFileOnDiskOid() == null) {
                    fod = FileOnDisk.getNewFileOnDisk(uploadedFileData, user);

                    FileOnDisk.dao().save(fod);

                    uploadedFileData.setFileOnDiskOid(fod.getOid());

                    // if there's an error, reset the fileOnDiskOid.
                    PartitionGroup.addEndOfPartitionGroupRunnableForError(new Runnable() {
                        public void run() {
                            uploadedFileData.setFileOnDiskOid(null);
                        }
                    });

                    fileList.add(fod);
                }
            }

            // users can only edit their own files
            if (exists(fod)) {
                fod.setTitleResolved(fileData.getTitle());
                fod.setDescription(fileData.getDescription());
            }
        }

        return fileList;
    }

    public int getFilesAdded() {
        return filesAdded;
    }

    public boolean isCharsChanged() {
        return charsChanged;
    }
}
