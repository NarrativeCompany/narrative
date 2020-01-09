package org.narrative.network.core.fileondisk.image;

import org.narrative.network.core.content.base.UploadedImageFileData;
import org.narrative.network.core.fileondisk.base.FileBaseType;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.ImageFileMetaDataProvider;
import org.narrative.network.core.fileondisk.base.services.filesystem.NetworkPath;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.services.GoogleCloudStorageFileHandler;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 *
 * Time: 3:38:13 PM
 */
@Entity
@DiscriminatorValue(FileType.IMAGE_TYPE_STRING)
public class ImageOnDisk extends FileOnDisk<ImageMetaData> implements ImageFileMetaDataProvider {

    private transient final Map<ImageType, File> imageTypeToResizedImage = newHashMap();

    public static final ImageType DEFAULT_PRIMARY_IMAGE_TYPE = ImageType.LARGE;
    public static final Collection<ImageType> DEFAULT_ADDITIONAL_IMAGE_TYPES = Collections.unmodifiableCollection(Arrays.asList(ImageType.LARGE, ImageType.LARGE_SQUARE_THUMBNAIL));
    public static final Collection<ImageType> ADDITIONAL_IMAGE_TYPES_WITH_MEDIUM = Collections.unmodifiableCollection(Arrays.asList(ImageType.LARGE, ImageType.MEDIUM, ImageType.LARGE_SQUARE_THUMBNAIL));
    public static final Collection<ImageType> DEFAULT_WEBSPACE_IMAGE_TYPES = Collections.unmodifiableCollection(Arrays.asList(ImageType.SQUARE_THUMBNAIL, ImageType.ORIGINAL));

    /**
     * @deprecated for hibernate use only
     */
    public ImageOnDisk() {
    }

    public ImageOnDisk(UploadedImageFileData imageFileData, User user) {
        super(imageFileData, user);

        for (ImageType imageType : imageFileData.getFileUsageType().getSupportedImageTypes()) {
            // bl: only add the resized image if it is smaller than the primary image type
            File file = imageFileData.getFileForImageType(imageType);
            if ((imageType.isSmallerThan(getFileMetaData().getPrimaryImageType()) || imageType.isSquareImageType()) && file != null) {
                addResizedImage(imageType, file);
            }
        }
    }

    public ImageOnDisk(ImageOnDisk copy, File tempFile) {
        super(copy, tempFile);
    }

    @Transient
    public FileType getFileType() {
        return FileType.IMAGE;
    }

    @Transient
    public void addResizedImage(ImageType imageType, File resizedImageFile) {
        assert !imageType.isOriginal() : "Can't add a resized image that is the original image";
        imageTypeToResizedImage.put(imageType, resizedImageFile);
    }

    @Transient
    public Map<ImageType, File> getImageTypeToResizedImage() {
        return imageTypeToResizedImage;
    }

    @Transient
    public NetworkPath getNetworkPathForImageType(ImageType imageType) {
        return new NetworkPath(FileBaseType.FILE_ON_DISK_IMAGE, imageType, this.getOid(), getFilename());
    }

    @Transient
    public NetworkPath getNetworkPath() {
        return getNetworkPathForImageType(getFileMetaData().getPrimaryImageType());
    }

    @Override
    @Transient
    public ImageType getPrimaryImageType() {
        return getFileMetaData().getPrimaryImageType();
    }

    @Transient
    public NetworkPath getPrimaryImageNetworkPath() {
        return getImageNetworkPath(getPrimaryImageType());
    }

    @Transient
    public NetworkPath getImageNetworkPath(ImageType imageType) {
        // jw: We need to resolve the image type to the appropriate size, just in case the requested size is larger than
        //     anything we have stored.
        ImageType resolvedImageType = imageType.getResolvedImageTypeForPrimaryImageType(getPrimaryImageType());

        return getNetworkPathForImageType(resolvedImageType);
    }

    @Transient
    public String getImageUrl(ImageType imageType) {
        NetworkPath networkPath = getImageNetworkPath(imageType);

        return GoogleCloudStorageFileHandler.IMAGES.getFileUri(networkPath);
    }

    @Transient
    public String getPrimaryImageUrl() {
        return getImageUrl(getPrimaryImageType());
    }

    @Transient
    public ImageType getPrimarySquareAvatarImageType() {
        return ImageType.getPrimarySquareImageType(getPrimaryImageType());
    }

    @Transient
    public String getPrimarySquareAvatarImageUrl() {
        ImageType imageType = getPrimarySquareAvatarImageType();
        NetworkPath networkPath = getNetworkPathForImageType(imageType);

        return GoogleCloudStorageFileHandler.IMAGES.getFileUri(networkPath);
    }

}

