package org.narrative.network.core.content.base.services;

import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.core.content.base.ExistingFileData;
import org.narrative.network.core.content.base.FileData;
import org.narrative.network.core.content.base.FileDataUtil;
import org.narrative.network.core.content.base.UploadedFileData;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.services.DeleteFileOnDisk;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Feb 17, 2006
 * Time: 4:23:18 PM
 */
public class UpdateFilePointerSet extends GlobalTaskImpl<FilePointerSet> {

    private final FilePointerSet fps;
    private final List<? extends FileData> fileList;
    private boolean hasModifiedFiles = false;

    public UpdateFilePointerSet(Composition composition, FilePointerSet<? extends FilePointer> fps, List<? extends FileData> fileList) {
        this(getTotalFileCount(fileList) > 0 && fps == null ? new FilePointerSet(composition) : fps, fileList);
    }

    public UpdateFilePointerSet(FilePointerSet fps, List<? extends FileData> fileList) {
        this.fps = fps;
        this.fileList = fileList;
    }

    protected static int getTotalFileCount(List<? extends FileData> fileList) {
        int count = 0;
        if (fileList != null) {
            for (FileData fileData : fileList) {
                if (fileData.isInclude()) {
                    count++;
                }
            }
        }
        return count;
    }

    protected FilePointerSet doMonitoredTask() {
        //Handle Attachments
        if (getTotalFileCount(fileList) == 0) {
            if (fps != null) {
                List<FilePointer> filePointers = fps.getFilePointers();
                List<FileOnDisk> fileOnDisks = null;
                // bl: figure out what FileOnDisks were being pointed to in this FilePointerSet
                if (filePointers != null) {
                    fileOnDisks = newArrayList(filePointers.size());
                    for (FilePointer filePointer : filePointers) {
                        fileOnDisks.add(filePointer.getFileOnDisk());
                    }
                }
                fps.deleteFilePointerSet();
                // bl: now that the FilePointerSet (and all FilePointers) have been deleted, we need to delete the corresponding FileOnDisks (if possible)
                if (fileOnDisks != null) {
                    // jw: if we are removing FileOnDisk records that necessarily means that files are being modified :D
                    hasModifiedFiles = true;
                    for (FileOnDisk fileOnDisk : fileOnDisks) {
                        getNetworkContext().doGlobalTask(new DeleteFileOnDisk(fileOnDisk));
                    }
                }
            }

            return null;
        }

        FileDataUtil.sortFileDataByOrder(fileList);

        // first, go through all of the file data that is included for this request.
        for (FileData fileData : fileList) {
            if (fileData == null) {
                continue;
            }
            // if the file isn't included, just continue.  we'll catch it in the second for loop.
            if (!fileData.isInclude()) {
                continue;
            }
            FileOnDisk fileOnDisk = null;
            FilePointer fp = null;
            if (fileData instanceof ExistingFileData) {
                ExistingFileData existingFileData = (ExistingFileData) fileData;
                // we might be changing something about the file
                fp = fps.getFilePointerByFileOnDiskOid(existingFileData.getFileOnDiskOid());
                // not in the file pointer set yet?
                if (!exists(fp)) {
                    // attaching a previously uploaded file
                    fileOnDisk = existingFileData.getFileOnDisk();
                }
            } else {
                assert fileData instanceof UploadedFileData : "Found FileData that wasn't UploadedFileData or ExistingFileData!";
                UploadedFileData uploadedFileData = (UploadedFileData) fileData;
                // we are adding a new file
                assert uploadedFileData.getFileOnDiskOid() != null : "FileData.fileOnDiskOID must not be null.  Make sure this file data is saved before calling this task.";
                fileOnDisk = uploadedFileData.getFileOnDisk();
                // jw: if we have a new file then the files are being modified!
                hasModifiedFiles = true;
            }

            if (fileOnDisk != null) {
                fp = fps.addFilePointer(fileOnDisk);
                fileData.doSetFilePointer(fp);
            }

            fp.setThreadingOrder(fileData.getOrder());
        }

        // now that we've added and updated all of the necessary files, go through and remove the
        // files that are no longer in use.
        for (FileData fileData : fileList) {
            if (fileData == null) {
                continue;
            }
            if (fileData.isInclude()) {
                continue;
            }
            // only need to care about existing file data.  uploaded file data we can just discard.
            if (fileData instanceof ExistingFileData) {
                //we have a file to delete
                ExistingFileData existingFileData = (ExistingFileData) fileData;
                FilePointer fp = fps.getFilePointerByFileOnDiskOid(existingFileData.getFileOnDiskOid());
                // remove the file pointer if it is in this set.
                if (exists(fp)) {
                    fps.removeFilePointer(fp);
                    FileOnDisk fod = existingFileData.getFileOnDisk();
                    getNetworkContext().doGlobalTask(new DeleteFileOnDisk(fod));
                }
            }
        }

        fps.optimizeThreadingOrder();

        return fps;
    }

    public boolean isHasModifiedFiles() {
        return hasModifiedFiles;
    }
}
