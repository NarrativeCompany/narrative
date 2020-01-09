package org.narrative.common.util.video;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 9, 2007
 * Time: 5:14:53 PM
 */
public class VideoMetadata {

    //common
    private String format;
    private String audioCodec;
    private String videoCodec;

    private long lengthInMs;

    //video
    private String videoEncoding;
    private int height;
    private int width;
    private double fps;

    public VideoMetadata(String format, String audioCodec, String videoCodec, long lengthInMs, String videoEncoding, int height, int width, double fps) {
        this.format = format;
        this.audioCodec = audioCodec;
        this.videoCodec = videoCodec;
        this.lengthInMs = lengthInMs;
        this.videoEncoding = videoEncoding;
        this.height = height;
        this.width = width;
        this.fps = fps;
    }

    public String getFormat() {
        return format;
    }

    public long getLengthInMs() {
        return lengthInMs;
    }

    public String getVideoEncoding() {
        return videoEncoding;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public double getFps() {
        return fps;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public String getVideoCodec() {
        return videoCodec;
    }
}
