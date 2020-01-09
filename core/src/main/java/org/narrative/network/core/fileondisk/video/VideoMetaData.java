package org.narrative.network.core.fileondisk.video;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.network.core.content.base.VideoThumbnailFrame;
import org.narrative.network.core.fileondisk.base.DimensionMetaData;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.image.ImageMetaData;
import org.narrative.network.core.fileondisk.image.ImageType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 8, 2007
 * Time: 4:11:14 PM
 */
public class VideoMetaData implements DimensionMetaData {
    private static final long serialVersionUID = -1270511479198046414L;
    private int width;
    private int height;
    private long lengthInMS;
    private VideoThumbnailFrame thumbnailFrame;

    public VideoMetaData() {
    }

    public VideoMetaData(int width, int height, long lengthInMS) {
        this.width = width;
        this.height = height;
        this.lengthInMS = lengthInMS;

    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getLengthInMS() {
        return lengthInMS;
    }

    public void setLengthInMS(long lengthInMS) {
        this.lengthInMS = lengthInMS;
    }

    public VideoThumbnailFrame getThumbnailFrame() {
        return thumbnailFrame;
    }

    public void setThumbnailFrame(VideoThumbnailFrame thumbnailFrame) {
        this.thumbnailFrame = thumbnailFrame;
    }

    @JsonIgnore
    public VideoTime getVideoTime() {
        return new VideoTime(lengthInMS);
    }

    public void setMetaData(byte[] data) {
        if (data != null) {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            try {
                width = dis.readInt();
                height = dis.readInt();
                lengthInMS = dis.readLong();

                // jw: this field may not be set on older videos from before it was added!
                try {
                    int id = dis.readInt();
                    if (id != VideoThumbnailFrame.NO_THUMBNAIL_ID) {
                        thumbnailFrame = EnumRegistry.getForId(VideoThumbnailFrame.class, id);
                    }
                } catch (IOException ignore) {
                    // jw: if we failed reading that data we know that there is nothing else to read.
                    return;
                }

            } catch (IOException e) {
                throw UnexpectedError.getRuntimeException("Unable to read image attributes for ImageOnDisk!", e, true);
            }
        }
    }

    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(width);
            dos.writeInt(height);
            dos.writeLong(lengthInMS);

            // jw: its important to note that as part of adding the thumbnailFrameUnencryptedByteSizeLookup here I also made it so that we will always write a value for the thumbnail
            //     frame.  This is necessary because we need to be able to reliably read values for each of these later, and so we need to always write SOMETHING for every field in
            //     order for the deserialization to reliably work. For example, the serializeImageTypeBytesLookup utility method will always store a boolean up front of whether there
            //     is a cached map stored, this ensures that the deserializeImageTypeBytesLookup knows what to do, always.

            // jw: because we need to be able to reliably read the cached image type bytes lookup we need this field to always have a value in the buffer, and we cant disturb the order
            //     so it needs to be a integer, so lets use -1 as a special flag so that we can skip assigning a value in the deserialization
            dos.writeInt(thumbnailFrame != null ? thumbnailFrame.getId() : VideoThumbnailFrame.NO_THUMBNAIL_ID);

        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to store image on disk data!", e, true);
        }
        return baos.toByteArray();
    }

    public FileType getFileType() {
        return FileType.VIDEO;
    }

    public ImageType getPrimaryImageType() {
        // jw: once we start using VideoOnDisk for Narrative we are going to need to figure out what FileUsageType to pass
        //     to this. Until we need to hook this up, let's not worry about it.
        // return ImageType.getMaxNecessaryImageTypeForImageInfo(width, height, ImageOnDisk.DEFAULT_PRIMARY_IMAGE_TYPE, null);

        throw UnexpectedError.getRuntimeException("Videos are currently not supported!");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VideoMetaData that = (VideoMetaData) o;

        if (width != that.width) {
            return false;
        }
        if (height != that.height) {
            return false;
        }
        if (lengthInMS != that.lengthInMS) {
            return false;
        }
        if (thumbnailFrame != that.thumbnailFrame) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + (int) (lengthInMS ^ (lengthInMS >>> 32));
        result = 31 * result + (thumbnailFrame != null ? thumbnailFrame.hashCode() : 0);
        return result;
    }
}
