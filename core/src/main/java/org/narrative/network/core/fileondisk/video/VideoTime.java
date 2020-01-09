package org.narrative.network.core.fileondisk.video;

import java.text.NumberFormat;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 19, 2007
 * Time: 11:08:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class VideoTime {
    private int seconds;
    private int minutes;
    private int hours;

    public VideoTime(long timeInMiliseconds) {
        long length = (long) ((double) timeInMiliseconds / 1000);
        seconds = (int) length % 60;
        length -= seconds;
        length = (long) ((double) length / 60);
        minutes = (int) length % (60);
        length -= minutes;
        hours = (int) ((double) length / 24);
    }

    public String getSecondsPadded() {
        return getNumberPadded(seconds);
    }

    public String getMinutesPadded() {
        return getNumberPadded(minutes);
    }

    public String getHoursPadded() {
        return getNumberPadded(hours);
    }

    private String getNumberPadded(int number) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(0);
        nf.setMaximumIntegerDigits(2);
        nf.setMinimumIntegerDigits(2);
        return nf.format(number);
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getHours() {
        return hours;
    }
}
