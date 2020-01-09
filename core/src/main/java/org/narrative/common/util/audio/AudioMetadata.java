package org.narrative.common.util.audio;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 9, 2007
 * Time: 5:17:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class AudioMetadata {
    private final String format;
    private final int bitrate;
    private final long lengthInMs;

    public AudioMetadata(String format, int bitrate, long lengthInMs) {
        this.format = format;
        this.bitrate = bitrate;
        this.lengthInMs = lengthInMs;
    }

    public String getFormat() {
        return format;
    }

    public int getBitrate() {
        return bitrate;
    }

    public long getLengthInMs() {
        return lengthInMs;
    }
}
