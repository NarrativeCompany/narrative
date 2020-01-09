package org.narrative.network.core.fileondisk.image;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.common.util.images.ImageDimensions;
import org.narrative.common.util.images.ImageProperties;
import org.narrative.network.core.fileondisk.base.DimensionMetaData;
import org.narrative.network.core.fileondisk.base.FileType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Jul 28, 2006
 * Time: 11:16:47 AM
 *
 * @author Brian
 */
public class ImageMetaData implements DimensionMetaData {
    public int width;
    public int height;
    public ImageType primaryImageType;

    /**
     * @deprecated for hibernate use only (actually, for use in FileMetaDataType)
     */
    public ImageMetaData() {}

    public ImageMetaData(ImageProperties imageProperties, ImageType primaryImageType) {
        this(imageProperties.getWidth(), imageProperties.getHeight(), primaryImageType);
    }

    public ImageMetaData(int width, int height) {
        this(width, height, ImageOnDisk.DEFAULT_PRIMARY_IMAGE_TYPE);
    }

    public ImageMetaData(int width, int height, ImageType primaryImageType) {
        this.width = width;
        this.height = height;
        this.primaryImageType = primaryImageType;
    }

    public ImageMetaData(byte[] data) {
        setMetaData(data);
    }

    public void setMetaData(byte[] data) {
        if (data != null) {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            setMetaData(dis);
        }
    }

    void setMetaData(DataInputStream dis) {
        try {
            width = dis.readInt();
            height = dis.readInt();
            primaryImageType = EnumRegistry.getForId(ImageType.class, dis.readInt());

        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to read image attributes for ImageOnDisk!", e, true);
        }
    }

    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        serialize(dos);
        return baos.toByteArray();
    }

    void serialize(DataOutputStream dos) {
        try {
            dos.writeInt(width);
            dos.writeInt(height);
            dos.writeInt(primaryImageType.getId());

        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to store image on disk data!", e, true);
        }
    }

    public static Map<ImageType, Integer> deserializeImageTypeBytesLookup(DataInputStream dis) throws IOException {
        // jw: need to determine how many rows are actually stored, since further data may need to be stored after the map so we need to know how much to read.
        int entries;
        try {
            entries = dis.readInt();

        } catch (IOException e) {
            // jw: if we failed to read the number of entries that means that this is a old record, and as such there is nothing to read.
            return null;
        }

        // jw: at this point, if we fail to read data there is something seriously wrong.
        if (entries > 0) {
            Map<ImageType, Integer> byteSizeLookup = new HashMap<>();
            for (int i = 0; i < entries; i++) {
                byteSizeLookup.put(EnumRegistry.getForId(ImageType.class, dis.readInt()), dis.readInt());
            }
            return byteSizeLookup;
        }

        return null;
    }

    public static void serializeImageTypeBytesLookup(DataOutputStream dos, Map<ImageType, Integer> byteSizeLookup) throws IOException {
        // jw: its vital that we write this data in the same order and under the same conditions as its read above.

        int entryCount = isEmptyOrNull(byteSizeLookup) ? 0 : byteSizeLookup.size();
        dos.writeInt(entryCount);
        if (entryCount > 0) {
            for (Map.Entry<ImageType, Integer> entry : byteSizeLookup.entrySet()) {
                dos.writeInt(entry.getKey().getId());
                dos.writeInt(entry.getValue());
            }
        }
    }

    public FileType getFileType() {
        return FileType.IMAGE;
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

    @JsonIgnore
    public ImageDimensions getImageDimensions() {
        return new ImageDimensions(width, height);
    }

    public ImageType getPrimaryImageType() {
        return primaryImageType;
    }

    public void setPrimaryImageType(ImageType primaryImageType) {
        this.primaryImageType = primaryImageType;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ImageMetaData that = (ImageMetaData) o;

        if (height != that.height) {
            return false;
        }
        if (width != that.width) {
            return false;
        }
        if (primaryImageType != that.primaryImageType) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = width;
        result = 29 * result + height;
        result = 29 * result + primaryImageType.hashCode();
        return result;
    }
}
