package org.narrative.network.core.content.base;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.images.ImageDimensions;
import org.narrative.common.util.images.ImageOrientationType;
import org.narrative.common.util.images.ImageProperties;
import org.narrative.common.util.images.ImageUtils;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.fileondisk.image.ImageMetaData;
import org.narrative.network.core.fileondisk.image.ImageType;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.util.NetworkCoreUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Mar 23, 2006
 * Time: 8:49:54 AM
 *
 * @author Brian
 */
public class UploadedImageFileData extends UploadedFileData<ImageMetaData> implements ImageFileData {

    private final Map<ImageType, File> imageTypeToFile = newHashMap();
    private final Map<ImageType, Integer> imageTypeToByteSize = newHashMap();

    private String extensionForFormat;
    private String originalFormatString;
    private boolean isAnimatedGif;
    private boolean isImageCoalesced = false;

    public UploadedImageFileData(OID fileUploadProcessOid, FileUsageType fileUsageType, File file, String mimeType, String filename) {
        this(fileUploadProcessOid, fileUsageType, file, mimeType, filename, null);
    }

    public UploadedImageFileData(OID fileUploadProcessOid, FileUsageType fileUsageType, File file, String mimeType, String filename, OID uniqueOid) {
        super(fileUploadProcessOid, fileUsageType, file, mimeType, filename, uniqueOid);

        ImageType maxNecessaryImageType;
        ImageProperties imageProperties;

        // first, make sure this is an image before we do anything
        imageProperties = ImageUtils.getImageProperties(file);
        // return if the file is not a valid image
        if (imageProperties == null) {
            errorMessage = wordlet("imageFile.invalidFormat");
            return;
        }

        maxNecessaryImageType = ImageType.getMaxNecessaryImageTypeForImageProperties(imageProperties, ImageType.LARGE, fileUsageType);

        if (imageProperties.getWidth() <= 0 || imageProperties.getHeight() <= 0) {
            errorMessage = wordlet("imageFile.invalidFormat");
            return;
        }

        // bl: make sure the fixed dimensions are correct.
        // jw: not sure why we were only enforcing this for favicons before, but if fixed dimensions are specified
        //     lets always enforce them
        if (fileUsageType.isHasFixedDimensions()) {
            ImageDimensions fixedDimensions = fileUsageType.getFixedDimensions();

            if (fileUsageType.isFixedDimensionsUpperLimit()) {
                if (!fileUsageType.isEqualOrLessThanFixedImageDimensions(imageProperties)) {
                    if (fileUsageType.isFixedDimensionsForWidthOnly()) {
                        errorMessage = wordlet("imageFile.invalidMaxWidth", fixedDimensions.getWidth());

                    } else if (fileUsageType.isFixedDimensionsForHeightOnly()) {
                        errorMessage = wordlet("imageFile.invalidMaxHeight", fixedDimensions.getHeight());

                    } else {
                        errorMessage = wordlet("imageFile.invalidMaxDimensions", fixedDimensions.getWidth(), fixedDimensions.getHeight());
                    }
                    return;
                }

            } else {
                assert !fileUsageType.isFixedDimensionsForWidthOnly() && !fileUsageType.isFixedDimensionsForHeightOnly() : "Should never use single direction fixed dimensions for equal limits. type/"+this;

                if (!imageProperties.eq(fixedDimensions)) {
                    errorMessage = wordlet("imageFile.invalidDimensions", fixedDimensions.getWidth(), fixedDimensions.getHeight());
                    return;
                }
            }
        } else if (fileUsageType.isSquareImageRequired()) {
            if (imageProperties.getWidth() != imageProperties.getHeight()) {
                errorMessage = wordlet("imageFile.notSquare");
                return;
            }
        }

        ImageMetaData imageMetaData = new ImageMetaData(imageProperties, maxNecessaryImageType);

        this.mimeType = imageProperties.getFormat().getMimeType();
        //mk: defaults to jpg for any non standard format
        extensionForFormat = imageProperties.getFormat().getDefaultFileExtension();
        originalFormatString = imageProperties.getOriginalFormatString();
        // bl: special handling for images with more than one image embedded.
        if (imageProperties.getNumberOfImages() > 1) {
            // bl: only support multi-image/animation for gifs
            this.isAnimatedGif = imageProperties.getFormat().isGif();
            // jw: multi sized ICO files should be supported as well.
            boolean isMultiSizeIco = imageProperties.getFormat().isIco();
            // if it's not a gif file (e.g. it could be a tif), then we can't process it, so we'll treat it as a regular file.
            if (!isAnimatedGif && !isMultiSizeIco) {
                errorMessage = wordlet("imageFile.invalidFormat");
                return;
            }
        }

        super.setFileMetaData(imageMetaData);

        assert !IPStringUtil.isEmpty(this.mimeType) : "MimeType should be supplied if we got valid ImageMetaData!";
    }

    public FileType getFileType() {
        return FileType.IMAGE;
    }

    protected void deleteAllTempFilesSub() {
        FileDataUtil.safeDeleteFileIterator(imageTypeToFile.values().iterator());
    }

    public File getFileForImageType(ImageType imageType) {
        return imageTypeToFile.get(imageType);
    }

    private String getTempFilenameForFileUploadProcessOid(OID fileUploadProcessOid, ImageType imageType) {
        return new StringBuilder(fileUploadProcessOid.toString()).append("_").append(uniqueOid).append("_").append(imageType).toString();
    }

    protected void postUploadSubProcess(OID fileUploadProcessOid) {
        FileUsageType fileUsageType = getFileUsageType();
        if (fileUsageType.isBypassFileProcessing()) {
            super.postUploadSubProcess(fileUploadProcessOid);
            return;
        }

        // bl: always strip location info for guest uploaded images
        // jw: dont strip the GPS info from web space images
        if (!NetworkRegistry.getInstance().isImporting()) {
            ImageUtils.stripGPS(getTempFile());
        }

        // bl: if ImageMagick's auto-orient will fip the image orientation, then we need to flip the width and height attributes here
        ImageOrientationType orientationType = ImageUtils.getImageOrientationType(getTempFile());
        if (orientationType.isSideways()) {
            int newHeight = getFileMetaData().getWidth();
            int newWidth = getFileMetaData().getHeight();
            getFileMetaData().setWidth(newWidth);
            getFileMetaData().setHeight(newHeight);
        }

        ImageType primaryImageType = getFileMetaData().getPrimaryImageType();
        ImageType primarySquareImageType = ImageType.getPrimarySquareImageType(primaryImageType);

        resizeImageIfNecessaryAndStoreTempFile(fileUploadProcessOid, primaryImageType, fileUsageType, true, orientationType);
        resizeImageIfNecessaryAndStoreTempFile(fileUploadProcessOid, primarySquareImageType, fileUsageType, false, ImageOrientationType.STANDARD);

        for (ImageType imageType : fileUsageType.getSupportedImageTypes()) {
            // get the resolved image type based on the primary image type
            ImageType imageTypeResolved = imageType.getResolvedImageTypeForPrimaryImageType(getFileMetaData().getPrimaryImageType());
            ImageType resolvedPrimaryImageType = imageTypeResolved.isSquareImageType() ? primarySquareImageType : primaryImageType;
            // skip the primary image type
            if (imageTypeResolved == resolvedPrimaryImageType) {
                continue;
            }
            assert imageTypeResolved.isSmallerThan(resolvedPrimaryImageType) : "Resolved ImageType wasn't smaller than the primary image type!  Shouldn't be possible! rit/" + imageTypeResolved + " pit/" + resolvedPrimaryImageType;
            // bl: we only want to pass in orientationType for the very first (primary) image (above). after that, the tempFile
            // is the primary image from which we do the other resizes, and the orientation should have already been fixed
            // so we do not want to flip the orientation in that case or else the image will not be sized properly.
            resizeImageIfNecessaryAndStoreTempFile(fileUploadProcessOid, imageType, fileUsageType, false, ImageOrientationType.STANDARD);
        }
    }

    @Override
    protected String getTempFileExtension() {
        return getExtensionForFormat();
    }

    public String getExtensionForFormat() {
        return extensionForFormat;
    }

    private File coalesceImageIfNecessary(File outFile) {
        if (isAnimatedGif && !isImageCoalesced) {
            //We coalesce the image into a a new file, delete the old file, and set the "temp" file to the new coalesced image
            outFile = NetworkCoreUtils.createTempFile(getTempFile().getName() + "coalesce", "gif");
            ObjectPair<Boolean, Throwable> conversionOutput = ImageUtils.coalesce(getTempFile(), outFile);
            if (!conversionOutput.getOne()) {
                throw UnexpectedError.getRuntimeException("Problem coalescing animated gif image!", conversionOutput.getTwo());
            }
            getTempFile().delete();
            setTempFile(outFile);
            isImageCoalesced = true;
        }

        return outFile;
    }

    private void resizeImageIfNecessaryAndStoreTempFile(OID fileUploadProcessOid, ImageType imageType, FileUsageType fileUsageType, boolean isPrimary, ImageOrientationType orientationType) {
        //Create temp file for resized image
        File outFile = NetworkCoreUtils.createTempFile(getTempFilenameForFileUploadProcessOid(fileUploadProcessOid, imageType), getExtensionForFormat());

        // jw: ensure that the image has been coalesced if necessary.
        outFile = coalesceImageIfNecessary(outFile);

        //Get dimensions to resize image to
        ImageDimensions imageDimensions = resizeImageIfNecessaryAndStore(getTempFile(), outFile, imageType, isAnimatedGif, getFileMetaData(), fileUsageType, orientationType, getExtensionForFormat());

        // if this is the primary image, then update the corresponding fields
        if (isPrimary) {
            getFileMetaData().setWidth(imageDimensions.getWidth());
            getFileMetaData().setHeight(imageDimensions.getHeight());
            setTempFile(outFile);
        }

        // always add it to our map, too
        imageTypeToFile.put(imageType, outFile);
        imageTypeToByteSize.put(imageType, (int) outFile.length());
    }

    public String getFilename() {
        if (!isJpgPngGifExtension(originalFormatString)) {
            return getFilenameAsJpg();
        } else {
            return super.getFilename();
        }
    }

    private static Pattern LETTERS_ONLY_PATTERN = Pattern.compile("[^a-z]");

    public static boolean isJpgPngGifExtension(String extension) {
        if (isEmpty(extension)) {
            return false;
        }
        //mk: format returned by ImageMagic can contain version, eg BMP3 so strip that.
        return FileType.IMAGE.getExtensions().contains(LETTERS_ONLY_PATTERN.matcher(extension.toLowerCase()).replaceAll(""));
    }

    // jw: this utility method allows us to generate new Images for existing ImageOnDisk records when necessary via patches
    //     and ensures that we will use the same logic used to originally generate the image.
    public static ImageDimensions resizeImageIfNecessaryAndStore(File inFile, File outFile, ImageType imageType, boolean isAnimatedGif, ImageMetaData imageMetaData, FileUsageType fileUsageType, ImageOrientationType orientationType, String extensionForFormat) {
        //Get dimensions to resize image to
        ImageDimensions imageDimensions;

        int edgeLength = -1;
        if (imageType.isSquareImageType()) {
            // bl: the edge length for a square thumbnail should be at most 100 pixels, which is the imageType.getMaxWidth.
            // additionally, the edge length should never be greater than the minimum edge width of the original file.
            edgeLength = Math.min(imageMetaData.getWidth(), Math.min(imageMetaData.getHeight(), imageType.getMaxWidth()));
            imageDimensions = new ImageDimensions(edgeLength, edgeLength);
        } else {
            imageDimensions = ImageUtils.getImageDimensionsForResizedImage(imageMetaData.getWidth(), imageMetaData.getHeight(), imageType.getMaxWidth(), imageType.getMaxHeight());
        }

        //Convert image via ImageMagick and store new file in temp dir
        ObjectPair<Boolean, Throwable> conversionOutput;

        boolean standardSize = imageMetaData.getWidth() <= ImageType.LARGE.getMaxWidth() && imageMetaData.getHeight() <= ImageType.LARGE.getMaxHeight();
        //mk: supported extensions to the format: jpg, jpeg, gif, png
        boolean supportedImageFormat = isJpgPngGifExtension(extensionForFormat);
        //mk: keep standard sized image as is
        // jw: the only time we can re-use the original image is if it's orientation has not been messed with.
        boolean keepAsIs = !isAnimatedGif && orientationType.isStandard() && imageType == imageMetaData.getPrimaryImageType() && standardSize && supportedImageFormat;
        // if the resized image is smaller than or equal to the fixed dimensions defined by the usage type, and the
        // original image has the same dimensions as our resize then lets just copy it
        if (keepAsIs || (imageType.isOriginal() && !isAnimatedGif) || (fileUsageType.isEqualOrLessThanFixedImageDimensions(imageDimensions) && imageDimensions.equals(imageMetaData.getImageDimensions()))) {
            IPUtil.doCopyFile(inFile, outFile);
            conversionOutput = newObjectPair(true, null);
        } else if (edgeLength != -1) {
            conversionOutput = ImageUtils.convertSquare(inFile, outFile, edgeLength);
        } else {
            // bl: if it's a flipped orientation image, then we need to reverse the width and height attributes
            // here in order for ImageMagick to create the image at the proper dimensions.
            int width = orientationType.isSideways() ? imageDimensions.getHeight() : imageDimensions.getWidth();
            int height = orientationType.isSideways() ? imageDimensions.getWidth() : imageDimensions.getHeight();
            conversionOutput = ImageUtils.convert(inFile, outFile, width, height);
        }

        if (!conversionOutput.getOne()) {
            throw UnexpectedError.getRuntimeException("Problem converting image!", conversionOutput.getTwo());
        }

        return imageDimensions;
    }

    public ObjectPair<InputStream, Integer> getFileInputStreamAndByteSizeForImageType(ImageType imageType) {
        ImageType imageTypeResolved = imageType.getResolvedImageTypeForPrimaryImageType(getFileMetaData().getPrimaryImageType());
        File imageFile = getFileForImageType(imageTypeResolved);

        // bl: hack to support title graphics which are ORIGINAL.
        if (imageFile == null) {
            imageTypeResolved = getFileMetaData().getPrimaryImageType();
            imageFile = getFileForImageType(imageTypeResolved);
        }

        try {
            if (imageFile != null) {
                return new ObjectPair<InputStream, Integer>(new FileInputStream(imageFile), imageTypeToByteSize.get(imageTypeResolved));
            }
        } catch (FileNotFoundException fnfe) {
            throw UnexpectedError.getRuntimeException("Failed lookup of uploaded file!", fnfe, true);
        }

        throw UnexpectedError.getRuntimeException("Failed lookup of uploaded file for ImageType /" + imageType + " imageTypeResolved/" + imageTypeResolved, true);
    }
}
