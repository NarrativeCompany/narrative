package org.narrative.network.core.fileondisk.base;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.common.util.enums.StringEnum;
import org.narrative.network.core.composition.files.AudioFilePointer;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.composition.files.ImageFilePointer;
import org.narrative.network.core.composition.files.VideoFilePointer;
import org.narrative.network.core.content.base.ExistingAudioFileData;
import org.narrative.network.core.content.base.ExistingFileData;
import org.narrative.network.core.content.base.ExistingImageFileData;
import org.narrative.network.core.content.base.ExistingVideoFileData;
import org.narrative.network.core.content.base.UploadedAudioFileData;
import org.narrative.network.core.content.base.UploadedFileData;
import org.narrative.network.core.content.base.UploadedImageFileData;
import org.narrative.network.core.content.base.UploadedVideoFileData;
import org.narrative.network.core.fileondisk.audio.AudioMetaData;
import org.narrative.network.core.fileondisk.audio.AudioOnDisk;
import org.narrative.network.core.fileondisk.image.ImageMetaData;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.fileondisk.video.VideoMetaData;
import org.narrative.network.core.fileondisk.video.VideoOnDisk;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Jul 28, 2006
 * Time: 8:50:14 AM
 *
 * @author Brian
 */
public enum FileType implements IntegerEnum, StringEnum {
    REGULAR((byte) 0, "REGULAR", null, ExistingFileData.class, UploadedFileData.class, FileOnDisk.class, FilePointer.class, null, false),
    IMAGE((byte) 1, "IMAGE", ImageMetaData.class, ExistingImageFileData.class, UploadedImageFileData.class, ImageOnDisk.class, ImageFilePointer.class, new String[]{"jpg", "jpeg", "gif", "png"}, true),
    AUDIO((byte) 2, "AUDIO", AudioMetaData.class, ExistingAudioFileData.class, UploadedAudioFileData.class, AudioOnDisk.class, AudioFilePointer.class, new String[]{"mp3", "aiff", "aif", "wav", "aac", "m4a", "wma"}, false),
    VIDEO((byte) 3, "VIDEO", VideoMetaData.class, ExistingVideoFileData.class, UploadedVideoFileData.class, VideoOnDisk.class, VideoFilePointer.class, new String[]{"mov", "avi", "3gp", "wmv", "divx", "flv", "mpg", "m4v", "mp4"}, true);

    public static final int MAX_BYTE_LIMIT = 100 * IPUtil.BYTES_PER_MB;

    public static final String TYPE = "org.narrative.network.core.fileondisk.base.FileType";

    public static final String REGULAR_TYPE_STRING = "REGULAR";
    public static final String IMAGE_TYPE_STRING = "IMAGE";
    public static final String AUDIO_TYPE_STRING = "AUDIO";
    public static final String VIDEO_TYPE_STRING = "VIDEO";

    // jw: its important for more than the UI that REGULAR is last here.
    public static final Set<FileType> ALL_FILE_TYPES_ORDERED = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(IMAGE, VIDEO, AUDIO, REGULAR)));

    private final byte id;
    private final String idStr;
    private final Class<? extends FileMetaData> fileMetaDataClass;
    private final Class<? extends ExistingFileData> existingFileDataClass;
    private final Class<? extends UploadedFileData> uploadedFileDataClass;
    private final Class<? extends FileOnDisk> fileOnDiskClass;
    private final Class<? extends FilePointer> filePointerClass;
    private final List<String> extensions;
    private final boolean hasImagePreview;

    FileType(byte id, String idStr, Class<? extends FileMetaData> fileMetaDataClass, Class<? extends ExistingFileData> existingFileDataClass, Class<? extends UploadedFileData> uploadedFileDataClass, Class<? extends FileOnDisk> fileOnDiskClass, Class<? extends FilePointer> filePointerClass, String[] extensions, boolean hasImagePreview) {
        this.id = id;
        this.idStr = idStr;
        this.fileMetaDataClass = fileMetaDataClass;
        this.existingFileDataClass = existingFileDataClass;
        this.uploadedFileDataClass = uploadedFileDataClass;
        this.fileOnDiskClass = fileOnDiskClass;
        this.filePointerClass = filePointerClass;
        this.extensions = extensions == null ? Collections.emptyList() : Arrays.asList(extensions);
        this.hasImagePreview = hasImagePreview;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getIdStr() {
        return idStr;
    }

    public String getDefaultExtension() {
        switch (this) {
            case IMAGE:
                return "jpg";
            case VIDEO:
                return "m4v";
            case AUDIO:
                return "mp3";
            case REGULAR:
                return "txt";
        }
        throw UnexpectedError.getRuntimeException("Must support all FileTypes for defaultExtension! ft/" + this);
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public Class<? extends FileMetaData> getFileMetaDataClass() {
        return fileMetaDataClass;
    }

    public Class<? extends ExistingFileData> getExistingFileDataClass() {
        return existingFileDataClass;
    }

    public Class<? extends UploadedFileData> getUploadedFileDataClass() {
        return uploadedFileDataClass;
    }

    public boolean isHasImagePreview() {
        return hasImagePreview;
    }

    public Class<? extends FileOnDisk> getFileOnDiskClass() {
        return fileOnDiskClass;
    }

    public Class<? extends FilePointer> getFilePointerClass() {
        return filePointerClass;
    }

    public boolean isRegularFile() {
        return this == REGULAR;
    }

    public boolean isImageFile() {
        return this == IMAGE;
    }

    public boolean isVideoFile() {
        return this == VIDEO;
    }

    public String getNameForDisplay() {
        return wordlet("fileType." + this);
    }

    public String getDescription() {
        return wordlet("fileType.description." + this);
    }

    public UploadedFileData getNewInstance(OID fileUploadProcessOid, FileUsageType fileUsageType, File file, String mimeType, String filename) {
        try {
            return getUploadedFileDataClass().getConstructor(OID.class, FileUsageType.class, File.class, String.class, String.class).newInstance(fileUploadProcessOid, fileUsageType, file, mimeType, filename);
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("Failed getting instance of UploadedFileData for specialized file type.  Must have a constructor that takes OID (fileUploadProcessOid), FileUsageType, File, String (mimeType), String (filename). class/" + getUploadedFileDataClass(), t, true);
        }
    }

    @Override
    public String toString() {
        return getIdStr();
    }
}
