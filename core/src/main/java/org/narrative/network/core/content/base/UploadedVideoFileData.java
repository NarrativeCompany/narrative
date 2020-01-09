package org.narrative.network.core.content.base;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.fileondisk.video.VideoMetaData;

import java.io.File;
import java.io.InputStream;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 8, 2007
 * Time: 4:15:55 PM
 */

public class UploadedVideoFileData extends UploadedFileData<VideoMetaData> implements VideoFileData {

    public UploadedVideoFileData(OID fileUploadProcessOid, FileUsageType fileUsageType, File file, String mimeType, String filename) {
        super(fileUploadProcessOid, fileUsageType, file, mimeType, filename);

        markInvalid();
        setErrorMessage(wordlet("video.invalidFormat"));
    }

    public FileType getFileType() {
        return FileType.VIDEO;
    }

    protected void postUploadSubProcess(OID fileUploadProcessOid) {
        //convert the thumbnail
        super.postUploadSubProcess(fileUploadProcessOid);

        // jw: if the type bypasses file processing then lets bail out here!
        if (getFileUsageType().isBypassFileProcessing()) {
            return;
        }
    }

    @Override
    public ObjectPair<InputStream, Integer> getFileInputStreamAndByteSize() {
        throw UnexpectedError.getRuntimeException("Method not supported for videos! Should only get thumbnails.");
    }
}
