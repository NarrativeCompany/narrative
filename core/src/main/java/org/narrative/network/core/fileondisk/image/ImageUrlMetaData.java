package org.narrative.network.core.fileondisk.image;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Date: Jul 28, 2006
 * Time: 11:16:47 AM
 *
 * @author Brian
 */
public class ImageUrlMetaData extends ImageMetaData {

    private String url;

    /**
     * @deprecated for hibernate use only (actually, for use in FileMetaDataType)
     */
    public ImageUrlMetaData() {}

    public ImageUrlMetaData(String url, int width, int height) {
        super(width, height);
        this.url = url;
    }

    public void setMetaData(byte[] data) {
        if (data != null) {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            setMetaData(dis);
        }
    }

    void setMetaData(DataInputStream dis) {
        try {
            int urlLength = dis.readInt();
            if (urlLength > 0) {
                char[] urlChars = new char[urlLength];
                for (int i = 0; i < urlChars.length; i++) {
                    urlChars[i] = dis.readChar();
                }
                this.url = String.valueOf(urlChars);
            }
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to read image attributes for ImageOnDisk!", true);
        }
        super.setMetaData(dis);
    }

    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        serialize(dos);
        return baos.toByteArray();
    }

    void serialize(DataOutputStream dos) {
        try {
            dos.writeInt(IPStringUtil.strLength(url));
            if (!IPStringUtil.isEmpty(url)) {
                dos.writeChars(url);
            }
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to store image on disk data!", true);
        }
        super.serialize(dos);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImageUrlMetaData)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ImageUrlMetaData that = (ImageUrlMetaData) o;

        if (!url.equals(that.url)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }
}