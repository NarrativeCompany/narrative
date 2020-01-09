package org.narrative.common.util.images;

/**
 * Date: Mar 31, 2006
 * Time: 2:20:10 PM
 *
 * @author Brian
 */
public enum ImageInfoType {
    /**
     * ImageInfoType for JPEG streams.
     * ImageInfo can extract physical resolution and comments
     * from JPEGs (only from APP0 headers).
     * Only one image can be stored in a file.
     * It is determined whether the JPEG stream is progressive
     * (see ImageInfo.isProgressive()).
     */
    FORMAT_JPEG("JPEG", "image/jpeg", "jpg"),

    /**
     * ImageInfoType for GIF streams.
     * ImageInfo can extract comments from GIFs and count the number
     * of images (GIFs with more than one image are animations).
     * It is determined whether the GIF stream is interlaced (see ImageInfo.isProgressive()).
     */
    FORMAT_GIF("GIF", "image/gif", "gif"),

    /**
     * ImageInfoType for PNG streams.
     * PNG only supports one image per file.
     * Both physical resolution and comments can be stored with PNG,
     * but ImageInfo is currently not able to extract those.
     * It is determined whether the PNG stream is interlaced (see ImageInfo.isProgressive()).
     */
    FORMAT_PNG("PNG", "image/png", "png"),

    /**
     * ImageInfoType for BMP streams.
     * BMP only supports one image per file.
     * BMP does not allow for comments.
     * The physical resolution can be stored.
     */
    //FORMAT_BMP("BMP", "image/bmp"),

    /**
     * ImageInfoType for PCX streams.
     * PCX does not allow for comments or more than one image per file.
     * However, the physical resolution can be stored.
     */
    //FORMAT_PCX("PCX", "image/pcx"),

    /**
     * ImageInfoType for IFF streams.
     */
    //FORMAT_IFF("IFF", "image/iff"),

    /**
     * ImageInfoType for RAS streams.
     * Sun Raster allows for one image per file only and is not able to
     * store physical resolution or comments.
     */
    //FORMAT_RAS("RAS", "image/ras"),

    /** ImageInfoType for PBM streams. */
    //FORMAT_PBM("PBM", "image/x-portable-bitmap"),

    /** ImageInfoType for PGM streams. */
    //FORMAT_PGM("PGM", "image/x-portable-graymap"),

    /** ImageInfoType for PPM streams. */
    //FORMAT_PPM("PPM", "image/x-portable-pixmap"),

    /** ImageInfoType for PSD streams. */
    //FORMAT_PSD("PSD", "image/psd"),

    /**
     * ImageInfoType for ICO (icon) files
     * http://en.wikipedia.org/wiki/ICO_(file_format)#MIME_type
     * official registered mimeType is "image/vnd.microsoft.icon"
     * but image/x-icon should work fine.
     */
    FORMAT_ICO("ICO", "image/x-icon", "ico");

    private final String formatName;
    private final String mimeType;
    private final String defaultFileExtension;

    private ImageInfoType(String formatName, String mimeType, String defaultFileExtension) {
        this.formatName = formatName;
        this.mimeType = mimeType;
        this.defaultFileExtension = defaultFileExtension;
    }

    public String getFormatName() {
        return formatName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getDefaultFileExtension() {
        return defaultFileExtension;
    }

    public boolean isGif() {
        return this == FORMAT_GIF;
    }

    public boolean isIco() {
        return this == FORMAT_ICO;
    }

    public static ImageInfoType getFormatForFileFormat(String fileFormat) {
        // from: http://www.imagemagick.org/script/formats.php
        for (ImageInfoType imageInfoType : values()) {
            if (imageInfoType.formatName.equals(fileFormat)) {
                return imageInfoType;
            }
        }
        // bl: don't treat PDFs as images. ever. please. thanks.
        if ("PDF".equalsIgnoreCase(fileFormat)) {
            return null;
        }
        // bl: it looks like the newer version of ImageMagick is treating single-page PDFs as PBM files. let's not treat
        // those as images anymore, either.
        if ("PBM".equalsIgnoreCase(fileFormat)) {
            return null;
        }
        // bl: don't want SVG to be treated as an image, either.
        if ("SVG".equalsIgnoreCase(fileFormat)) {
            return null;
        }
        /*if(FORMAT_ICO.formatName.equals(fileFormat)) {
            return FORMAT_ICO;
        } else if("JPEG".equals(fileFormat)) {
            return FORMAT_JPEG;
        } else if(FORMAT_GIF.formatName.equals(fileFormat)) {
            return FORMAT_GIF;
        } else if(FORMAT_PNG.formatName.equals(fileFormat)) {
            return FORMAT_PNG;
        } else if("BMP".equals(fileFormat)) {
            return FORMAT_BMP;
        } else if("PCX".equals(fileFormat)) {
            return FORMAT_PCX;
        } *//*else if("".equals(fileFormat)) {
            return FORMAT_IFF;
        } else if("".equals(fileFormat)) {
            return FORMAT_RAS;
        } *//*else if("PBM".equals(fileFormat)) {
            return FORMAT_PBM;
        } else if("PGM".equals(fileFormat)) {
            return FORMAT_PGM;
        } else if("PPM".equals(fileFormat)) {
            return FORMAT_PPM;
        } else if("PSD".equals(fileFormat)) {
            return FORMAT_PSD;
        }*/
        // bl: default to jpg output for all other image types
        return FORMAT_JPEG;
    }
}
