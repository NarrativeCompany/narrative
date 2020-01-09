package org.narrative.network.core.fileondisk.image;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.common.util.enums.StringEnum;
import org.narrative.common.util.images.ImageDimensions;
import org.narrative.network.core.fileondisk.base.FileUsageType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Nov 30, 2005
 * Time: 11:45:51 AM
 */
public enum ImageType implements IntegerEnum, StringEnum {
    MINI_THUMBNAIL(0, "MINI_THUMBNAIL", 50, 50, "minithumbnail"),
    THUMBNAIL(1, "THUMBNAIL", 100, 100, "thumbnail"),
    SMALL(2, "SMALL", 250, 250, "small"),
    MEDIUM(3, "MEDIUM", 600, 600, "medium"),
    LARGE(4, "LARGE", 1000, 1000, "large"),
    ORIGINAL(5, "ORIGINAL", -1, -1, "original"),
    SQUARE_THUMBNAIL(6, "SQUARE_THUMBNAIL", 100, 100, "square"),
    // jw: note that this ImageType is only used for User Avatars and nothing else currently
    LARGE_SQUARE_THUMBNAIL(7, "LARGE_SQUARE_THUMBNAIL", 300, 300, "largesquare");

    private final int id;
    private final String idStr;
    private final int maxWidth;
    private final int maxHeight;
    private final String fileExtension;

    public static final List<ImageType> LIMITED_SIZE_ORDERED_IMAGE_TYPES = Collections.singletonList(LARGE);
    public static final List<ImageType> LIMITED_SIZE_ORDERED_IMAGE_TYPES_WITH_MEDIUM = Collections.unmodifiableList(Arrays.asList(LARGE, MEDIUM));
    private static final List<ImageType> ALL_ORDERED_SQUARE_IMAGE_TYPES = Collections.unmodifiableList(Arrays.asList(LARGE_SQUARE_THUMBNAIL, SQUARE_THUMBNAIL));

    ImageType(int id, String idStr, int maxWidth, int maxHeight, String fileExtension) {
        this.id = id;
        this.idStr = idStr;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.fileExtension = fileExtension;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getIdStr() {
        return idStr;
    }

    @Override
    public String toString() {
        return getIdStr();
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public boolean hasLimits() {
        return maxWidth > 0 && maxHeight > 0;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public boolean isFixedImageDimensions() {
        return isSquareImageType();
    }

    public boolean isMiniThumbnail() {
        return this == MINI_THUMBNAIL;
    }

    public boolean isThumbnail() {
        return this == THUMBNAIL;
    }

    public boolean isSmall() {
        return this == SMALL;
    }

    public boolean isMedium() {
        return this == MEDIUM;
    }

    public boolean isLarge() {
        return this == LARGE;
    }

    public boolean isOriginal() {
        return this == ORIGINAL;
    }

    public boolean isSquareThumbnail() {
        return this == SQUARE_THUMBNAIL;
    }

    public boolean isLargeSquareThumbnail() {
        return this == LARGE_SQUARE_THUMBNAIL;
    }

    public boolean isSquareImageType() {
        return isSquareThumbnail() || isLargeSquareThumbnail();
    }

    public boolean isSmallerThan(ImageType type) {
        // shouldn't probably ever ask this, but handling anyway
        if (this == type) {
            return false;
        }
        // always must be smaller than original
        if (type.maxWidth < 0) {
            return true;
        }
        // original can't be smaller than anything
        if (this.maxWidth < 0) {
            return false;
        }
        // bl: assume the ImageType limits are square, so we can just test widths
        return this.maxWidth < type.maxWidth;
    }

    public List<ImageType> getLimitedSizeOrderedImageTypes(FileUsageType fileUsageType) {
        if (isSquareImageType()) {
            return ALL_ORDERED_SQUARE_IMAGE_TYPES;
        }
        if (fileUsageType.isSupportsMediumImageType()) {
            return LIMITED_SIZE_ORDERED_IMAGE_TYPES_WITH_MEDIUM;
        }

        return LIMITED_SIZE_ORDERED_IMAGE_TYPES;
    }

    public static ImageType getPrimarySquareImageType(ImageType primaryImageType) {
        // jw: In the past, we were storing SQUARE_THUMBNAILs for any image where the primaryImageType was smaller than
        //     SMALL. Since we are now only storing LARGE, MEDIUM and LARGE_SQUARE_THUMBNAILs we can know that if the
        //     primaryImageType is smaller than Small we need to use the SQUARE_THUMBNAIL. We should only have that for
        //     those legacy scenarios, which works out in our favor.
        return primaryImageType.isSmallerThan(SMALL) ? SQUARE_THUMBNAIL : LARGE_SQUARE_THUMBNAIL;
    }

    /**
     * get the ImageType to use for file lookups based on the primary image type for an image.
     * for example, if a file's primary ImageType is MEDIUM, and you're looking for a LARGE image,
     * then return MEDIUM since LARGE will be the same as MEDIUM.
     * on the other hand, if the primary image type is MEDIUM and you're looking for SMALL, return
     * SMALL since SMALL will have to be smaller than the MEDIUM primary image type.
     *
     * @param primaryImageType the primary image type from which to determine the proper ImageType
     * @return the ImageType to use for resolving files on disk
     */
    public ImageType getResolvedImageTypeForPrimaryImageType(ImageType primaryImageType) {
        //Square thumbnail is ALWAYS the resolved type 
        if (this.isSquareImageType()) {
            primaryImageType = getPrimarySquareImageType(primaryImageType);
        }

        // if they're the same, then just use it
        if (this == primaryImageType) {
            return this;
        }
        // if this is smaller than the primary image type, then just return this
        if (this.isSmallerThan(primaryImageType)) {
            return this;
        }
        // otherwise, the primary image type must be smaller than the current, so return the primary image type.
        return primaryImageType;
    }

    public static ImageType getMaxNecessaryImageTypeForImageProperties(ImageDimensions imageProperties, ImageType defaultMaxImageType, FileUsageType fileUsageType) {
        return getMaxNecessaryImageTypeForImageInfo(imageProperties.getWidth(), imageProperties.getHeight(), defaultMaxImageType, fileUsageType);
    }

    public static ImageType getMaxNecessaryImageTypeForImageInfo(int width, int height, ImageType defaultMaxImageType, FileUsageType fileUsageType) {
        assert !defaultMaxImageType.isSquareImageType() : "Square thumbnail should not be a choice for default image type";
        ImageType ret = defaultMaxImageType;
        for (ImageType imageType : fileUsageType.getSupportedLimitedSizeImageTypes()) {
            // skip anything bigger than the supplied default max image type
            if (defaultMaxImageType.isSmallerThan(imageType)) {
                continue;
            }
            // if the image fits within the contraints of the current type
            if (width <= imageType.maxWidth && height <= imageType.maxHeight) {
                // since these are now ordered, we can just take the current ImageType,
                // effectively downsizing the maximum necessary ImageType.
                ret = imageType;
            } else {
                // we're done since the current ImageType is smaller than the image (and therefore all remaining
                // ImageTypes will be smaller than the current), so break out of the loop.
                break;
            }
        }
        return ret;
    }
}
