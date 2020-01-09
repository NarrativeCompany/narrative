package org.narrative.network.core.fileondisk.audio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.fileondisk.base.FileMetaData;
import org.narrative.network.core.fileondisk.base.FileType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.NumberFormat;

/**
 * User: barry
 * Date: Nov 24, 2009
 * Time: 3:21:22 PM
 */
public class BaseAudioMetaData implements FileMetaData {
    private int lengthSeconds;
    private int bitRateKbps;
    private final FileType fileType;

    public BaseAudioMetaData(FileType fileType) {
        this.fileType = fileType;
    }

    public BaseAudioMetaData(FileType fileType, int lengthSeconds, int bitRateKbps) {
        this(fileType);
        this.lengthSeconds = lengthSeconds;
        this.bitRateKbps = bitRateKbps;
    }

    public BaseAudioMetaData(FileType fileType, byte[] data) {
        this(fileType);
        setMetaData(data);
    }

    public void setMetaData(byte[] data) {
        if (data != null) {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            try {
                lengthSeconds = dis.readInt();
                bitRateKbps = dis.readInt();
            } catch (IOException e) {
                throw UnexpectedError.getRuntimeException("Unable to read audio file attributes for AudioOnDisk!", true);
            }
        }
    }

    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(lengthSeconds);
            dos.writeInt(bitRateKbps);
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to store audio on disk data!", e, true);
        }
        return baos.toByteArray();
    }

    final public FileType getFileType() {
        return fileType;
    }

    public int getLengthSeconds() {
        return lengthSeconds;
    }

    public void setLengthSeconds(int lengthSeconds) {
        this.lengthSeconds = lengthSeconds;
    }

    public int getBitRateKbps() {
        return bitRateKbps;
    }

    public void setBitRateKbps(int bitRateKbps) {
        this.bitRateKbps = bitRateKbps;
    }

    @JsonIgnore
    public String getDisplayTime() {
        return getDisplayTime(lengthSeconds);
    }

    public static String getDisplayTime(int lengthInSeconds) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(0);
        numberFormat.setMinimumIntegerDigits(2);
        StringBuilder ret = new StringBuilder();
        ret.append(numberFormat.format(lengthInSeconds / IPDateUtil.MINUTE_IN_SECONDS));
        ret.append(":");
        ret.append(numberFormat.format(lengthInSeconds % IPDateUtil.MINUTE_IN_SECONDS));
        return ret.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AudioMetaData that = (AudioMetaData) o;

        if (bitRateKbps != that.getBitRateKbps()) {
            return false;
        }
        if (lengthSeconds != that.getLengthSeconds()) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = lengthSeconds;
        result = 29 * result + bitRateKbps;
        return result;
    }
}
