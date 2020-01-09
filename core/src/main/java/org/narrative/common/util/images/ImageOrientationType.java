package org.narrative.common.util.images;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 8/6/18
 * Time: 2:20 PM
 * <p>
 * values based on:
 * https://www.daveperrett.com/articles/2012/07/28/exif-orientation-handling-is-a-ghetto/
 * http://sylvana.net/jpegcrop/exif_orientation.html
 */
public enum ImageOrientationType implements IntegerEnum {
    STANDARD(1),
    MIRRORED(2),
    ROTATED_180_DEGREES(3),
    MIRRORED_AND_ROTATED_180_DEGREES(4),
    MIRRORED_AND_ROTATED_90_DEGREES_CCW(5, true),
    ROTATED_90_DEGREES_CCW(6, true),
    MIRRORED_AND_ROTATED_90_DEGREES_CW(7, true),
    ROTATED_90_DEGREES_CW(8, true);

    private final int id;
    private final boolean sideways;

    ImageOrientationType(int id) {
        this(id, false);
    }

    ImageOrientationType(int id, boolean sideways) {
        this.id = id;
        this.sideways = sideways;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isSideways() {
        return sideways;
    }

    public boolean isStandard() {
        return this == STANDARD;
    }
}