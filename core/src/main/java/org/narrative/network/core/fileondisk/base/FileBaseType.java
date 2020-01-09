package org.narrative.network.core.fileondisk.base;

/**
 * Date: 3/28/14
 * Time: 4:51 PM
 *
 * @author brian
 */
public enum FileBaseType {
    FILE_ON_DISK_IMAGE("images"),
    FILE_ON_DISK_VIDEO("video"),
    FILE_ON_DISK_FILE("files"),
    CUSTOM_IMAGE("customImages"),
    THEME_CUSTOM_IMAGE("themeCustomImages"),
    AREA_EXPORT("areaExport"),
    AREA_DELETE("areaDelete"),
    THEME_EXPORT("themeExport"),
    COMMUNITY_HEALTH_REPORT_GRAPHS("communityHealthReportGraphs"),
    TEMPLATE("templates"),
    KYC_IMAGE("kycImages");

    private final String baseDirectory;

    FileBaseType(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public boolean isSupportsLocalFileCache() {
        // bl: due to the way templates are compiled, we don't want to use local file cache for template files.
        // with imports, we need to make sure that all files are available in the same directory.
        return !isTemplate();
    }

    public boolean isSupportsImageType() {
        // bl: videos support ImageType for the various thumbnail sizes
        return isFileOnDiskImage() || isFileOnDiskVideo();
    }

    public boolean isFileOnDiskFile() {
        return this == FILE_ON_DISK_FILE;
    }

    public boolean isFileOnDiskImage() {
        return this == FILE_ON_DISK_IMAGE;
    }

    public boolean isFileOnDiskVideo() {
        return this == FILE_ON_DISK_VIDEO;
    }

    public boolean isTemplate() {
        return this == TEMPLATE;
    }
}
