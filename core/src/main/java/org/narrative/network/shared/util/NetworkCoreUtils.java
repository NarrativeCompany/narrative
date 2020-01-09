package org.narrative.network.shared.util;

import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.IPIOUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.images.ImageDimensions;
import org.narrative.common.util.images.ImageUtils;
import org.narrative.common.web.struts.MethodPropertiesUtil;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.content.base.FileData;
import org.narrative.network.core.fileondisk.base.DimensionMetaData;
import org.narrative.network.core.fileondisk.base.FileMetaData;
import org.narrative.network.core.fileondisk.base.FileMetaDataProvider;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.image.ImageType;
import org.narrative.network.core.settings.global.services.translations.NetworkResourceBundle;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.baseactions.NetworkAction;
import org.narrative.network.shared.context.AreaContext;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.context.NetworkContextHolder;
import org.narrative.network.shared.context.NetworkContextImplBase;
import org.narrative.network.shared.interceptors.ActionSetupInterceptor;
import org.narrative.network.shared.interceptors.NetworkContextInterceptor;
import org.narrative.network.shared.security.PrimaryRole;
import com.opensymphony.xwork2.ActionContext;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.jsp.PageContext;

import java.io.File;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 29, 2005
 * Time: 10:36:00 PM
 */
public class NetworkCoreUtils {

    private static final NetworkLogger logger = new NetworkLogger(NetworkCoreUtils.class);

    public static boolean isNetworkContextSet() {
        return NetworkContextImplBase.isNetworkContextSet();
    }

    @NotNull
    public static NetworkContext networkContext() {
        return NetworkContextImplBase.current();
    }

    @Nullable
    public static AreaContext areaContext() {
        NetworkContextImplBase ret = NetworkContextImplBase.current();
        if (!(ret instanceof AreaContext)) {
            return null;
        }
        return (AreaContext) ret;
    }

    @Nullable
    public static Area currentArea() {
        if (areaContext() == null) {
            return null;
        } else {
            return areaContext().getArea();
        }
    }

    public static AreaUserRlm getAreaUserRlm(AreaUser areaUser) {
        return AreaUser.getAreaUserRlm(areaUser);
    }

    public static AreaUser getAreaUser(AreaUserRlm areaUserRlm) {
        return AreaUserRlm.getAreaUser(areaUserRlm);
    }

    public static AreaRlm getAreaRlm(Area area) {
        return Area.getAreaRlm(area);
    }

    public static Area getArea(AreaRlm areaRlm) {
        return AreaRlm.getArea(areaRlm);
    }

    public static String wordlet(String key) {
        return networkContext().getResourceBundle().getString(key);
    }

    public static String wordlet1Arg(String key, Object arg1) {
        return networkContext().getResourceBundle().getString(key, arg1);
    }

    public static String wordlet2Arg(String key, Object arg1, Object arg2) {
        return networkContext().getResourceBundle().getString(key, arg1, arg2);
    }

    public static String wordlet3Arg(String key, Object arg1, Object arg2, Object arg3) {
        return networkContext().getResourceBundle().getString(key, arg1, arg2, arg3);
    }

    public static String wordlet4Arg(String key, Object arg1, Object arg2, Object arg3, Object arg4) {
        return networkContext().getResourceBundle().getString(key, arg1, arg2, arg3, arg4);
    }

    public static String wordlet5Arg(String key, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        return networkContext().getResourceBundle().getString(key, arg1, arg2, arg3, arg4, arg5);
    }

    public static String wordlet6Arg(String key, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        return networkContext().getResourceBundle().getString(key, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    public static String wordlet(String key, Object... args) {
        return networkContext().getResourceBundle().getString(key, args);
    }

    /**
     * get a default wordlet value based on the current Locale and current RequestType
     *
     * @param key the key for which to get the wordlet value
     * @return the wordlet value for the given key based on the current Locale and current RequestType
     */
    public static String defaultWordlet(String key) {
        return NetworkResourceBundle.getResourceBundle(networkContext()).getString(key);
    }

    public static String formatDate(Date date, String format) {
        if (date == null || IPStringUtil.isEmpty(format)) {
            return "";
        }
        FastDateFormat sdf = FastDateFormat.getInstance(format, networkContext().getFormatPreferences().getTimeZone(), networkContext().getLocale());
        return sdf.format(date);
    }

    public static String formatNumber(long val) {
        return networkContext().formatNumber(val);
    }

    public static String formatNumberForAuthZone(long val) {
        return networkContext().formatNumber(val);
    }

    public static String formatDecimalNumber(double val) {
        return networkContext().formatDecimalNumber(val);
    }

    public static String formatShortDecimalNumber(double val) {
        return networkContext().formatShortDecimalNumber(val);
    }

    public static String getFileSizeForDisplay(int byteSize) {
        // format the number in the user's locale
        NumberFormat numberFormat = NumberFormat.getNumberInstance(networkContext().getLocale());
        if (byteSize < IPUtil.BYTES_PER_KB) {
            numberFormat.setMaximumFractionDigits(0);
            return wordlet("bytes", numberFormat.format(Math.max(0, byteSize)));
        }
        if (byteSize < (IPUtil.BYTES_PER_MB)) {
            numberFormat.setMaximumFractionDigits(0);
            return wordlet("kilobytes", numberFormat.format((int) ((double) byteSize / (double) IPUtil.BYTES_PER_KB)));
        }
        numberFormat.setMaximumFractionDigits(2);
        return wordlet("megabytes", numberFormat.format((double) byteSize / (double) (IPUtil.BYTES_PER_MB)));
    }

    public static String getElapsedTimeSinceForDisplay(long sinceMs) {
        return getElapsedTimeForDisplay(System.currentTimeMillis() - sinceMs);
    }

    public static String getElapsedTimeForDisplay(long elapsedMs) {
        return getDurationTimeForDisplay(elapsedMs / IPDateUtil.SECOND_IN_MS);
    }

    public static String getDurationTimeForDisplay(long durationSeconds) {
        // format the number in the user's locale
        NumberFormat numberFormat = NumberFormat.getNumberInstance(networkContext().getLocale());
        numberFormat.setMaximumFractionDigits(0);
        if (durationSeconds < IPDateUtil.MINUTE_IN_SECONDS) {
            return wordlet("seconds", numberFormat.format(Math.max(0, durationSeconds)));
        }
        numberFormat.setMinimumIntegerDigits(2);
        StringBuilder sb = new StringBuilder();
        if (durationSeconds > IPDateUtil.DAY_IN_SECONDS) {
            sb.append(wordlet("days", durationSeconds / IPDateUtil.DAY_IN_SECONDS));
            sb.append(", ");
            durationSeconds %= IPDateUtil.DAY_IN_SECONDS;
        }
        StringBuilder sb2 = new StringBuilder();
        if (durationSeconds < IPDateUtil.HOUR_IN_SECONDS) {
            long minutes = durationSeconds / IPDateUtil.MINUTE_IN_SECONDS;
            long seconds = durationSeconds % IPDateUtil.MINUTE_IN_SECONDS;
            sb2.append(numberFormat.format(minutes));
            sb2.append(":");
            sb2.append(numberFormat.format(seconds));
            sb.append(wordlet("minutes", sb2.toString()));
        } else {
            long hours = durationSeconds / IPDateUtil.HOUR_IN_SECONDS;
            long minutes = (durationSeconds / IPDateUtil.MINUTE_IN_SECONDS) % IPDateUtil.HOUR_IN_MINUTES;
            sb2.append(numberFormat.format(hours));
            sb2.append(":");
            sb2.append(numberFormat.format(minutes));
            sb.append(wordlet("hours", sb2.toString()));
        }
        return sb.toString();
    }

    public static File createTempFile(String prefix, String suffix) {
        return createTempFile(null, prefix, suffix, false, false);
    }

    public static File createTempFile(String prefix, String suffix, boolean useUniqueGenerator) {
        return createTempFile(null, prefix, suffix, useUniqueGenerator, false);
    }

    public static File createTempFile(String prefix, String suffix, boolean useUniqueGenerator, boolean cleanUpAtEndOfPartitionGroup) {
        return createTempFile(null, prefix, suffix, useUniqueGenerator, cleanUpAtEndOfPartitionGroup);
    }

    public static File createTempFile(final String subDir, String prefix, String suffix, boolean useUniqueGenerator, boolean cleanUpAtEndOfPartitionGroup) {
        String fileName;

        if (useUniqueGenerator) {
            fileName = new StringBuilder(CoreUtils.seqOid().toString()).append("_").append(prefix).append(".").append(suffix).toString();
        } else {
            fileName = new StringBuilder(prefix).append(".").append(suffix).toString();
        }

        final File baseDir = NetworkRegistry.getInstance().getTempDir();

        File dir = baseDir;
        if (!isEmpty(subDir)) {
            dir = new File(dir, subDir);
            if (!dir.exists()) {
                IPIOUtil.mkdirs(dir);
            }
        }
        final File file = new File(dir, fileName);
        if (cleanUpAtEndOfPartitionGroup) {
            PartitionGroup.addEndOfPartitionGroupRunnableForSuccessOrError(new Runnable() {
                public void run() {
                    if (!isEmpty(subDir)) {
                        File dir = new File(baseDir, subDir);
                        // if we could delete the directory, then we're done.  if not, then we'll just try
                        // to delete the indiidual file.
                        if (!dir.exists() || IPIOUtil.deleteDirectory(dir)) {
                            return;
                        }
                    }
                    if (file != null && file.exists()) {
                        if (!file.delete()) {
                            logger.warn("Error cleaning up file: " + file.getAbsolutePath());
                        } else if (file.exists()) {
                            logger.error("Error cleaning up file, even though java said it worked: " + file.getAbsolutePath());
                        }
                    }
                }
            });
        }

        return file;
    }

    public static ImageDimensions getImageDimensionsForContainedSquare(FileMetaDataProvider fileMetaDataProvider, ImageType imageType, Integer square) {
        ImageDimensions dimensions = getImageDimensionsForResizedImage(fileMetaDataProvider, imageType);

        return getImageDimensionsForContainedSquare(dimensions.getWidth(), dimensions.getHeight(), square);
    }

    public static ImageDimensions getImageDimensionsForContainedSquare(Integer width, Integer height, Integer square) {
        if (width.equals(height)) {
            return new ImageDimensions(square, square);
        }

        float widthF = width.floatValue();
        float heightF = height.floatValue();
        if (height > width) {
            return new ImageDimensions(square, (int) (square * (heightF / widthF)));
        }

        return new ImageDimensions((int) (square * (widthF / heightF)), square);
    }

    public static ImageDimensions getImageDimensionsResolved(FileMetaDataProvider fileMetaDataProvider, ImageType imageType, Integer fixedImageDimensionOverride) {
        if (imageType.isFixedImageDimensions()) {
            if (fixedImageDimensionOverride != null && fixedImageDimensionOverride > 0) {
                return getFixedSquareImageDimensions(fixedImageDimensionOverride);
            }
            return getFixedSquareImageDimensions(imageType.getMaxWidth());
        }
        if (fixedImageDimensionOverride != null && fixedImageDimensionOverride > 0) {
            return getImageDimensionsForResizedImage(fileMetaDataProvider, fixedImageDimensionOverride);
        }
        return getImageDimensionsForResizedImage(fileMetaDataProvider, imageType);
    }

    public static ImageDimensions getImageDimensionsForResizedImage(FileMetaDataProvider fileMetaDataProvider, ImageType imageType) {
        assert !imageType.isFixedImageDimensions() : "Should not attempt to get resized image dimensions for square thumbnails! Just use fixed image dimensions instead!";
        return getImageDimensionsForResizedImage(fileMetaDataProvider, imageType.getMaxWidth(), imageType.getMaxHeight());
    }

    public static ImageDimensions getImageDimensionsForResizedImage(FileMetaDataProvider fileMetaDataProvider, int targetWidthHeightDimension) {
        return getImageDimensionsForResizedImage(fileMetaDataProvider, targetWidthHeightDimension, targetWidthHeightDimension);
    }

    public static ImageDimensions getImageDimensionsForResizedImageWidthOnly(FileMetaDataProvider fileMetaDataProvider, int targetWidthDimension) {
        return getImageDimensionsForResizedImage(fileMetaDataProvider, targetWidthDimension, Integer.MAX_VALUE);
    }

    public static ImageDimensions getImageDimensionsFromDimensionMetaData(DimensionMetaData dimensionMetaData, int targetWidthHeightDimension) {
        return getImageDimensionsFromDimensionMetaData(dimensionMetaData, targetWidthHeightDimension, targetWidthHeightDimension);
    }

    public static ImageDimensions getImageDimensionsForResizedImage(FileMetaDataProvider fileMetaDataProvider, int targetWidth, int targetHeight) {
        FileMetaData metaData = fileMetaDataProvider.getFileMetaData();
        if (!(metaData instanceof DimensionMetaData)) {
            throw UnexpectedError.getRuntimeException("Got a non-image/video FileMetaDataProvider when trying to calculate image dimensions! o/" + fileMetaDataProvider + " metadata/" + metaData + " fod/" + fileMetaDataProvider.getFileOnDiskOid());
        }

        return getImageDimensionsFromDimensionMetaData((DimensionMetaData) metaData, targetWidth, targetHeight);
    }

    private static ImageDimensions getImageDimensionsFromDimensionMetaData(DimensionMetaData dimensionMetaData, int targetWidth, int targetHeight) {
        return ImageUtils.getImageDimensionsForResizedImage(dimensionMetaData.getWidth(), dimensionMetaData.getHeight(), targetWidth, targetHeight);
    }

    public static ImageDimensions getFixedSquareImageDimensions(int imageDimension) {
        return getFixedImageDimensions(imageDimension, imageDimension);
    }

    public static ImageDimensions getFixedImageDimensions(int width, int height) {
        return new ImageDimensions(width, height);
    }

    public static AreaContext getAreaContextFromPageContext(PageContext pageContext) {
        if (pageContext == null) {
            return null;
        }
        NetworkContext networkContext;
        {
            NetworkAction action = (NetworkAction) pageContext.getRequest().getAttribute(ActionSetupInterceptor.REQUEST_ATTRIBUTE_ACTION);
            if (action != null) {
                networkContext = action.getNetworkContext();
            } else {
                networkContext = (NetworkContext) pageContext.getRequest().getAttribute(NetworkContextInterceptor.REQUEST_ATTRIBUTE_NETWORK_CONTEXT);
            }
        }
        if (networkContext == null) {
            NetworkContextHolder holder = (NetworkContextHolder) pageContext.getRequest().getAttribute(ActionSetupInterceptor.REQUEST_ATTRIBUTE_CONTEXT_HOLDER);

            if (holder != null && holder.getNetworkContext() != null) {
                networkContext = holder.getNetworkContext();
            } else {
                return null;
            }
        }
        if (!(networkContext instanceof AreaContext)) {
            return null;
        }
        return (AreaContext) networkContext;
    }

    public static List<FileData> getIncludedFileDatasOfType(Collection<FileData> fileDatas, FileType fileType) {
        if (fileDatas == null || fileDatas.isEmpty() || fileType == null) {
            return Collections.emptyList();
        }
        List<FileData> ret = new LinkedList<FileData>();
        for (FileData fileData : fileDatas) {
            if (fileData != null && fileData.isInclude() && fileData.getFileType() == fileType) {
                ret.add(fileData);
            }
        }
        return ret;
    }

}
