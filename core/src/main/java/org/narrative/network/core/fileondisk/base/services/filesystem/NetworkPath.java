package org.narrative.network.core.fileondisk.base.services.filesystem;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPIOUtil;
import org.narrative.common.util.QuartzUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.fileondisk.base.FileBaseType;
import org.narrative.network.core.fileondisk.base.services.CleanUpLocalFilesCache;
import org.narrative.network.core.fileondisk.image.ImageType;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;

import java.io.File;
import java.io.Serializable;
import java.text.NumberFormat;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Feb 21, 2007
 * Time: 2:33:20 PM
 * This class represents a network file.  It encapsulates the ways to get the local and remote file handle
 */
public class NetworkPath implements Serializable {

    private static String filePath;
    private static String localFileCachePath;

    private final FileBaseType fileBaseType;
    private final ImageType imageType;
    private final OID dirOid;
    private final OID fileOid;
    private final String filename;
    private final String fileNameSuffix;

    private transient File file = null;
    private transient File localCacheFile = null;

    public static String getFilePath() {
        throw UnexpectedError.getRuntimeException("Should not use NetworkFileHandler any longer!");
        //return filePath;
    }

    public static String getLocalFileCachePath() {
        throw UnexpectedError.getRuntimeException("Should not use NetworkFileHandler any longer!");
        //return localFileCachePath;
    }

    public static void setFilePath(String filePath) {
        while (filePath.endsWith("/") || filePath.endsWith("\\")) {
            filePath = filePath.substring(0, filePath.length() - 2);
        }
        NetworkPath.filePath = filePath;
        File file = new File(filePath);
        if (!file.exists()) {
            IPIOUtil.mkdirs(file);
        } else {
            if (!file.isDirectory()) {
                throw UnexpectedError.getRuntimeException("The network local path " + filePath + " is not a directory.");
            }
        }
    }

    public static void setLocalFileCachePath(String localFileCachePath) {
        if (isEmpty(localFileCachePath)) {
            return;
        }
        while (localFileCachePath.endsWith("/") || localFileCachePath.endsWith("\\")) {
            localFileCachePath = localFileCachePath.substring(0, localFileCachePath.length() - 2);
        }
        NetworkPath.localFileCachePath = localFileCachePath;
        File file = new File(localFileCachePath);
        if (!file.exists()) {
            IPIOUtil.mkdirs(file);
        } else {
            if (!file.isDirectory()) {
                throw UnexpectedError.getRuntimeException("The network local path " + localFileCachePath + " is not a directory.");
            }
        }

        // bl: every day at 1:20am, run the local job (on this servlet only) to clean up the local files cache
        QuartzJobScheduler.LOCAL.schedule(QuartzJobScheduler.createRecoverableJobBuilder(CleanUpLocalFilesCache.class), QuartzUtil.makeDailyTrigger(1, 20));
    }

    public static File getLocalFileCacheDirectory() {
        if (!isLocalFileCacheSupportedByEnvironment()) {
            return null;
        }
        return new File(getLocalFileCachePath());
    }

    public static boolean isLocalFileCacheSupportedByEnvironment() {
        return getLocalFileCachePath() != null;
    }

    public NetworkPath(FileBaseType fileBaseType, OID dirOid, String filename) {
        this.fileBaseType = fileBaseType;
        this.dirOid = dirOid;
        this.filename = filename;

        this.imageType = null;
        this.fileOid = null;
        this.fileNameSuffix = null;
    }

    public NetworkPath(FileBaseType fileBaseType, OID fileOid) {
        this(fileBaseType, null, fileOid);
    }

    public NetworkPath(FileBaseType fileBaseType, ImageType imageType, OID fileOid) {
        this(fileBaseType, imageType, fileOid, null);
    }

    public NetworkPath(FileBaseType fileBaseType, ImageType imageType, OID fileOid, String fileNameSuffix) {
        assert imageType == null || fileBaseType.isSupportsImageType() : "Should only supply ImageType for FileBaseTypes that support ImageType!";

        this.fileBaseType = fileBaseType;
        this.imageType = imageType;
        this.fileOid = fileOid;
        this.fileNameSuffix = fileNameSuffix;

        this.dirOid = null;
        this.filename = null;
    }

    public OID getFileOid() {
        return fileOid;
    }

    public File getFile() {
        if (file == null) {
            file = new File(getFilePath(getFilePath()));
        }
        return file;
    }

    public String getAbsoluteFilePath() {
        return getFilePath(getFilePath());
    }

    public ImageType getImageType() {
        return imageType;
    }

    public boolean isSupportsLocalFileCache() {
        return isLocalFileCacheSupportedByEnvironment() && fileBaseType.isSupportsLocalFileCache();
    }

    public File getLocalCacheFile() {
        if (!isSupportsLocalFileCache()) {
            return null;
        }
        if (localCacheFile == null) {
            localCacheFile = new File(getFilePath(getLocalFileCachePath()));
        }
        return localCacheFile;
    }

    private String getFilePath(String basePath) {
        StringBuilder sb = new StringBuilder(basePath);
        appendRelativeFilePath(sb, false);
        return sb.toString();
    }

    public String getRelativeFilePath() {
        return getRelativeFilePath(false);
    }

    public String getRelativeFilePath(boolean forUrl) {
        StringBuilder sb = new StringBuilder();
        appendRelativeFilePath(sb, forUrl);
        return sb.toString();
    }

    private void appendRelativeFilePath(StringBuilder sb, boolean forUrl) {
        appendRelativeDirectoryPath(sb);
        appendFilename(sb, forUrl);
    }

    private void appendRelativeDirectoryPath(StringBuilder sb) {
        sb.append("/");
        sb.append(fileBaseType.getBaseDirectory());
        sb.append("/");
        if (dirOid != null) {
            assert !isEmpty(filename) : "When using a dirOid, you must specify a filename!";
            sb.append(dirOid);
        } else {
            // bl: now just use the fileOid in its own directory
            //sb.append(getOidDirectory(fileOid));
            sb.append(fileOid);
        }
        sb.append('/');
    }

    private void appendFilename(StringBuilder sb, boolean urlEncode) {
        if (dirOid != null) {
            sb.append(filename);
        } else {
            // bl: if there is an image type supplied, then we need to prepend the type onto the filename
            if (imageType != null) {
                sb.append(imageType.getFileExtension());
                sb.append("-");
            }
            // jw: if we have a fileNameSuffix then lets append that.
            // bl: this is used for the actual filename now.
            if (!isEmpty(fileNameSuffix)) {
                if(urlEncode) {
                    sb.append(IPHTMLUtil.getURLEncodedStringButDontEncodeSpacesToPlus(fileNameSuffix));
                } else {
                    sb.append(fileNameSuffix);
                }
            } else {
                sb.append(fileOid.toString());
            }
        }
    }

    public String getBaseDirectory() {
        StringBuilder sb = new StringBuilder();
        sb.append(isSupportsLocalFileCache() ? getLocalFileCachePath() : getFilePath());
        appendRelativeDirectoryPath(sb);
        return sb.toString();
    }

    public String getFilename() {
        StringBuilder sb = new StringBuilder();
        appendFilename(sb, false);
        return sb.toString();
    }

    private String getOidDirectory(OID oid) {
        // bl: we will now use 10k directories from 0000, 0001, 0002, up to 9997, 9998, 9999
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumIntegerDigits(4);
        numberFormat.setMinimumIntegerDigits(4);
        numberFormat.setGroupingUsed(false);
        String lastFour = numberFormat.format(Math.abs(oid.getValue()) % 10000);
        return lastFour.substring(0, 2) + "/" + lastFour.substring(2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NetworkPath that = (NetworkPath) o;

        if (fileBaseType != that.fileBaseType) {
            return false;
        }
        if (imageType != that.imageType) {
            return false;
        }
        if (dirOid != null ? !dirOid.equals(that.dirOid) : that.dirOid != null) {
            return false;
        }
        if (fileOid != null ? !fileOid.equals(that.fileOid) : that.fileOid != null) {
            return false;
        }
        if (filename != null ? !filename.equals(that.filename) : that.filename != null) {
            return false;
        }
        return fileNameSuffix != null ? fileNameSuffix.equals(that.fileNameSuffix) : that.fileNameSuffix == null;
    }

    @Override
    public int hashCode() {
        int result = fileBaseType.hashCode();
        result = 31 * result + (imageType != null ? imageType.hashCode() : 0);
        result = 31 * result + (dirOid != null ? dirOid.hashCode() : 0);
        result = 31 * result + (fileOid != null ? fileOid.hashCode() : 0);
        result = 31 * result + (filename != null ? filename.hashCode() : 0);
        result = 31 * result + (fileNameSuffix != null ? fileNameSuffix.hashCode() : 0);
        return result;
    }
}
