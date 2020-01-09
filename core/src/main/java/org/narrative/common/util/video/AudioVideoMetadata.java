package org.narrative.common.util.video;

import org.narrative.common.util.audio.AudioMetadata;

/**
 * Date: 5/14/14
 * Time: 11:06 AM
 *
 * @author brian
 */
public class AudioVideoMetadata {
    private String errorOutput;
    private AudioMetadata audioMetadata;
    private VideoMetadata videoMetadata;

    public AudioVideoMetadata() {}

    public String getErrorOutput() {
        return errorOutput;
    }

    public void setErrorOutput(String errorOutput) {
        this.errorOutput = errorOutput;
    }

    public AudioMetadata getAudioMetadata() {
        return audioMetadata;
    }

    public void setAudioMetadata(AudioMetadata audioMetadata) {
        this.audioMetadata = audioMetadata;
    }

    public VideoMetadata getVideoMetadata() {
        return videoMetadata;
    }

    public void setVideoMetadata(VideoMetadata videoMetadata) {
        this.videoMetadata = videoMetadata;
    }
}
