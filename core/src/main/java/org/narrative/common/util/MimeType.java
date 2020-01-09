package org.narrative.common.util;

import org.narrative.common.util.images.ImageInfoType;

/**
 * Date: Feb 28, 2006
 * Time: 8:14:50 AM
 *
 * @author Brian
 */
public enum MimeType {
    /**
     * Recognized mime types.
     * Note:
     * - Use with ::supports(String mimeType)
     */
    ALL("*/*"),
    HTML("text/html"),
    PLAIN("text/plain"),
    XML(IPHTMLUtil.MIME_TYPE_TEXT_XML),
    HDML("text/x-hdml"),
    WML("text/vnd.wap.wml"),
    WML_SCRIPT("text/vnd.wap.wmlscript"),
    IMG_WBMP("image/vnd.wap.wbmp"),
    IMG_GIF(ImageInfoType.FORMAT_GIF.getMimeType()),
    IMG_JPG(ImageInfoType.FORMAT_JPEG.getMimeType()),
    CSV("text/comma-separated-values");

    private final String mimeTypeString;

    private MimeType(String mimeTypeString) {
        this.mimeTypeString = mimeTypeString;
    }

    public String getMimeTypeString() {
        return mimeTypeString;
    }

    public String toString() {
        return mimeTypeString;
    }
}
