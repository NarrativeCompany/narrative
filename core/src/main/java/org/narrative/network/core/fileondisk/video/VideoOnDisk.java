package org.narrative.network.core.fileondisk.video;

import org.narrative.network.core.content.base.UploadedVideoFileData;
import org.narrative.network.core.content.base.VideoThumbnailFrame;
import org.narrative.network.core.fileondisk.base.DimensionMetaDataProvider;
import org.narrative.network.core.fileondisk.base.FileBaseType;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileOnDiskStatus;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.services.filesystem.NetworkPath;
import org.narrative.network.core.fileondisk.image.ImageType;
import org.narrative.network.core.user.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 8, 2007
 * Time: 4:13:28 PM
 */
@Entity
@DiscriminatorValue(FileType.VIDEO_TYPE_STRING)
public class VideoOnDisk extends FileOnDisk<VideoMetaData> implements DimensionMetaDataProvider<VideoMetaData> {

    /**
     * @deprecated for hibernate use only
     */
    public VideoOnDisk() {
    }

    public VideoOnDisk(UploadedVideoFileData videoFileData, User user) {
        super(videoFileData, user);

        setStatus(FileOnDiskStatus.PENDING_CONVERSION);
    }

    @Transient
    public NetworkPath getNetworkPath() {
        return new NetworkPath(FileBaseType.FILE_ON_DISK_VIDEO, this.getOid());
    }

    @Transient
    public String getThumbnailFileNameSuffix() {
        VideoThumbnailFrame frame = getFileMetaData().getThumbnailFrame();

        return frame == null ? null : frame.getFileNameSuffix();
    }

    @Override
    @Transient
    public NetworkPath getNetworkPathForImageType(ImageType imageType) {
        return new NetworkPath(FileBaseType.FILE_ON_DISK_VIDEO, imageType, getOid(), getThumbnailFileNameSuffix());
    }

    @Transient
    public FileType getFileType() {
        return FileType.VIDEO;
    }

    @Override
    @Transient
    public ImageType getPrimaryImageType() {
        return getFileMetaData().getPrimaryImageType();
    }

}
