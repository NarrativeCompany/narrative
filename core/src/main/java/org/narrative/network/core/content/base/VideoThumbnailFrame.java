package org.narrative.network.core.content.base;

import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.enums.IntegerEnum;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/21/15
 * Time: 11:52 AM
 */
public enum VideoThumbnailFrame implements IntegerEnum {
    // jw: never use a -1 id, that is used by the VideoMetaData as a special value to represent no thumbnail.
    FIRST(0, 0, null, "first"),
    ONE_SECOND(1, IPDateUtil.SECOND_IN_MS, "00:00:01", "onesec"),
    FIVE_SECONDS(2, IPDateUtil.SECOND_IN_MS * 5, "00:00:05", "fivesec"),
    THIRTY_SECONDS(3, IPDateUtil.SECOND_IN_MS * 30, "00:00:30", "thirtysec"),
    ONE_MINUTE(4, IPDateUtil.MINUTE_IN_MS, "00:01:00", "oneminute");

    public static final int NO_THUMBNAIL_ID = -1;

    private final int id;
    private final long millisIn;
    private final String format;
    private final String fileNameSuffix;

    VideoThumbnailFrame(int id, long millisIn, String format, String fileNameSuffix) {
        this.id = id;
        this.millisIn = millisIn;
        this.format = format;
        this.fileNameSuffix = fileNameSuffix;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isFirstFrame() {
        return this == FIRST;
    }

    public long getMillisIn() {
        return millisIn;
    }

    public String getFormat() {
        return format;
    }

    public String getFileNameSuffix() {
        return fileNameSuffix;
    }
}
