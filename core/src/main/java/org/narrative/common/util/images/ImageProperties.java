package org.narrative.common.util.images;

/**
 * Date: 9/8/11
 * Time: 11:21 AM
 *
 * @author brian
 */
public class ImageProperties extends ImageDimensions {
    private final int numberOfImages;
    private final ImageInfoType format;
    private final String originalFormat;

    public ImageProperties(int width, int height, int numberOfImages, ImageInfoType format, String originalFormat) {
        super(width, height);
        this.numberOfImages = numberOfImages;
        this.format = format;
        this.originalFormat = originalFormat;
    }

    public ImageInfoType getFormat() {
        return format;
    }

    public int getNumberOfImages() {
        return numberOfImages;
    }

    public String getOriginalFormatString() {
        return originalFormat;
    }
}
