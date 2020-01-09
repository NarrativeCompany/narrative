package org.narrative.network.core.fileondisk.base;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.common.util.images.ImageDimensions;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.fileondisk.image.ImageType;

import java.util.Collection;
import java.util.Collections;

/**
 * Date: Mar 26, 2006
 * Time: 9:05:10 AM
 *
 * @author Brian
 */
public enum FileUsageType implements IntegerEnum {
    ATTACHMENT(0, null),
    AVATAR(3, FileType.IMAGE),
    // jw: note: the 0 for fixedDimension width means that the width is not fixed, and any width image can be used.
    PUBLICATION_LOGO(52, FileType.IMAGE),
    PUBLICATION_HEADER(53, new ImageDimensions(1120, 120), FileType.IMAGE);

    private final int id;
    private final AggregateFileType allowedUploadFileType;
    private final ImageDimensions fixedDimensions;

    FileUsageType(int id, FileType fileType) {
        this(id, null, fileType);
    }

    FileUsageType(int id, ImageDimensions fixedDimensions, FileType fileType) {
        this.id = id;
        // bl: ordering of the allowed upload file types is important since it will now determine the order
        // in which we try to process files.  thus, use a LinkedHashSet to ensure proper insert/iteration order.
        // jw: revised the constructors to simplify the process so that the only time that a file type is provided is when
        //     its a single restriction, this ensures that when we support all file types that the order is accurate.
        if (fileType != null) {
            this.allowedUploadFileType = new AggregateFileType(Collections.singleton(fileType));
        } else {
            this.allowedUploadFileType = new AggregateFileType(FileType.ALL_FILE_TYPES_ORDERED);
        }
        this.fixedDimensions = fixedDimensions;
    }

    public int getId() {
        return id;
    }

    /**
     * get the allowed upload AggregateFileType for this FileUsageType
     *
     * @return the allowed upload file type for this FileUsageType
     */
    public AggregateFileType getAllowedUploadFileType() {
        return allowedUploadFileType;
    }

    public AggregateFileType getAllowedUploadFileTypeForCurrentRole() {
        return getAllowedUploadFileType();
    }

    public boolean isAllowFileTypeForCurrentRole(FileType fileType) {
        return getAllowedUploadFileTypeForCurrentRole().getFileTypes().contains(fileType);
    }

    /**
     * get the allowed existing AggregateFileType for this FileUsageType
     *
     * @return the allowed existing file type for this FileUsageType
     */
    public AggregateFileType getAllowedExistingFileType() {
        // bl: moved this to the getter to avoid static initialization ordering issues
        // bk: eve does not use exsting file types for eve #5353
        // bl: likewise for music instances since we no longer have prime clips.
        return AggregateFileType.EMPTY_AGGREGATE_FILE_TYPE;
        //return allowedExistingFileType;
    }

    public ImageDimensions getFixedDimensions() {
        return fixedDimensions;
    }

    public boolean isHasFixedDimensions() {
        return fixedDimensions != null;
    }

    public boolean isFixedDimensionsUpperLimit() {
        return isPublicationHeader();
    }

    public boolean isFixedDimensionsForHeightOnly() {
        if (!isHasFixedDimensions()) {
            return false;
        }
        return getFixedDimensions().getHeight() > 0 && getFixedDimensions().getWidth() <= 0;
    }

    public boolean isFixedDimensionsForWidthOnly() {
        if (!isHasFixedDimensions()) {
            return false;
        }
        return getFixedDimensions().getWidth() > 0 && getFixedDimensions().getHeight() <= 0;
    }

    public boolean isSquareImageRequired() {
        return isPublicationLogo();
    }

    public boolean isBypassFileProcessing() {
        return isPublicationHeader();
    }

    /**
     * Returns the max size allowed for this fileUsageType and fileType.  FileUsageType can override the size
     * and fileType is the fallback.
     *
     * @param fileType
     * @return
     */
    public Integer getMaxFileSize(FileType fileType) {
        assert (getAllowedExistingFileType().getFileTypes().contains(fileType) || allowedUploadFileType.getFileTypes().contains(fileType)) : "Supplied a file type that wasn't valid for this FileUsageType";
        return FileType.MAX_BYTE_LIMIT;
    }

    public boolean isAttachment() {
        return this == ATTACHMENT;
    }

    public boolean isPublicationLogo() {
        return this == PUBLICATION_LOGO;
    }

    public boolean isPublicationHeader() {
        return this == PUBLICATION_HEADER;
    }

    public boolean isSupportsTempFile() {
        // bl: for now, only the Publication logo and header support temp files
        return isPublicationLogo() || isPublicationHeader();
    }

    public Collection<ImageType> getSupportedImageTypes() {
        if (isSupportsMediumImageType()) {
            return ImageOnDisk.ADDITIONAL_IMAGE_TYPES_WITH_MEDIUM;
        }

        return ImageOnDisk.DEFAULT_ADDITIONAL_IMAGE_TYPES;
    }

    public Collection<ImageType> getSupportedLimitedSizeImageTypes() {
        if (isSupportsMediumImageType()) {
            return ImageType.LIMITED_SIZE_ORDERED_IMAGE_TYPES_WITH_MEDIUM;
        }

        return ImageType.LIMITED_SIZE_ORDERED_IMAGE_TYPES;
    }

    // jw: for simplicity, we are only supporting large image types now, except for attachments. For attachments we will
    //     also support medium.
    public boolean isSupportsMediumImageType() {
        return isAttachment();
    }

    public boolean isEqualOrLessThanFixedImageDimensions(ImageDimensions dimensions) {
        if (!isHasFixedDimensions()) {
            return false;
        }

        if (isFixedDimensionsForHeightOnly()) {
            return dimensions.getHeight() <= fixedDimensions.getHeight();
        }

        if (isFixedDimensionsForWidthOnly()) {
            return dimensions.getWidth() <= fixedDimensions.getWidth();
        }

        return !dimensions.gt(fixedDimensions);
    }

}
