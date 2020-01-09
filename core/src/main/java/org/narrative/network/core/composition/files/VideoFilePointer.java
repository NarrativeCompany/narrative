package org.narrative.network.core.composition.files;

import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.video.VideoMetaData;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * User: Paul
 * Date: Jan 9, 2007
 * Time: 11:55:24 AM
 */
@Entity
@DiscriminatorValue(FileType.VIDEO_TYPE_STRING)
public class VideoFilePointer extends FilePointer<VideoMetaData> {

    /**
     * @deprecated for hibernate use only
     */
    public VideoFilePointer() {}

    public VideoFilePointer(FilePointerSet filePointerSet, FileOnDisk fileOnDisk) {
        super(filePointerSet, fileOnDisk);
        // don't need to set any specific fields since the extra data will be set in the FilePointer
        // constructor automatically.  pretty cool.
    }

    @Transient
    public FileType getFileType() {
        return FileType.VIDEO;
    }
}
