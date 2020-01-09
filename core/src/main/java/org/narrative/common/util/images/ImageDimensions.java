package org.narrative.common.util.images;

/**
 * Date: Mar 26, 2006
 * Time: 9:49:09 AM
 *
 * @author Brian
 */
public class ImageDimensions {
    private final int width;
    private final int height;

    public ImageDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean ge(ImageDimensions dimensions) {
        return ge(dimensions.getWidth(), dimensions.getHeight());
    }

    public boolean ge(int width, int height) {
        return getWidth() >= width || getHeight() >= height;
    }

    public boolean gt(ImageDimensions dimensions) {
        return gt(dimensions.getWidth(), dimensions.getHeight());
    }

    public boolean gt(int width, int height) {
        return getWidth() > width || getHeight() > height;
    }

    public boolean le(ImageDimensions dimensions) {
        return le(dimensions.getWidth(), dimensions.getHeight());
    }

    public boolean le(int width, int height) {
        return !gt(width, height);
    }

    public boolean eq(ImageDimensions dimensions) {
        return eq(dimensions.getWidth(), dimensions.getHeight());
    }

    public boolean eq(int width, int height) {
        return getWidth() == width && getHeight() == height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImageDimensions that = (ImageDimensions) o;

        return height == that.height && width == that.width;
    }
}
