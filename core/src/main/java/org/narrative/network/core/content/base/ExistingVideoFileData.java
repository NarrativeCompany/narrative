package org.narrative.network.core.content.base;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.fileondisk.base.FileBase;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.fileondisk.video.VideoMetaData;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 8, 2007
 * Time: 4:17:12 PM
 */
public class ExistingVideoFileData extends ExistingFileData<VideoMetaData> implements VideoFileData {

    private OID newVideoOnDiskOid;

    public ExistingVideoFileData(OID fileUploadProcessOid, FileBase fileBase, FileUsageType fileUsageType) {
        super(fileUploadProcessOid, fileBase, fileUsageType);
    }

    public FileType getFileType() {
        return FileType.VIDEO;
    }

    @Override
    public OID getFileOnDiskOid() {
        if (newVideoOnDiskOid != null) {
            return newVideoOnDiskOid;
        }

        return super.getFileOnDiskOid();
    }

    public void setFileOnDiskOid(OID newVideoOnDiskOid) {
        this.newVideoOnDiskOid = newVideoOnDiskOid;
    }
}
