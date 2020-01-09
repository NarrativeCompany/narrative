package org.narrative.network.core.fileondisk.image;

import org.narrative.common.persistence.hibernate.EventListenerImpl;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.fileondisk.base.services.filesystem.NetworkPath;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.customizations.narrative.services.GoogleCloudStorageFileHandler;
import org.narrative.network.shared.util.NetworkLogger;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreInsertEvent;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Feb 20, 2006
 * Time: 9:23:33 AM
 */
public class ImageOnDiskEventListener extends EventListenerImpl {

    private static final NetworkLogger logger = new NetworkLogger(ImageOnDiskEventListener.class);

    @Override
    public void onEventListenerPreDelete(PreDeleteEvent event) {
        List<NetworkPath> filesToDelete = new LinkedList<>();
        ImageOnDisk iod = (ImageOnDisk) event.getEntity();
        ImageType primaryImageType = iod.getFileMetaData().getPrimaryImageType();
        ImageType primarySquareImageType = ImageType.getPrimarySquareImageType(primaryImageType);
        // first try to delete all of the resized images.  since we don't keep track of what resized images
        // that we have, try them all and gracefully swallow any errors.
        for (ImageType imageType : ImageType.values()) {
            // get the image type resolved based on the primary image type of this image.
            ImageType imageTypeResolved = imageType.getResolvedImageTypeForPrimaryImageType(primaryImageType);
            ImageType resolvedPrimaryImageType = imageTypeResolved.isSquareImageType() ? primarySquareImageType : primaryImageType;
            // skip the primary image type.  also skip any ImageTypes larger than the primary image type (handled via resolved image type)
            if (imageTypeResolved == resolvedPrimaryImageType) {
                continue;
            }

            //the square thumbnail always exists, regardless of image size
            assert imageTypeResolved.isSmallerThan(resolvedPrimaryImageType) : "Resolved ImageType wasn't smaller than the primary image type!  Shouldn't be possible! rit/" + imageTypeResolved + " pit/" + resolvedPrimaryImageType;
            filesToDelete.add(iod.getNetworkPathForImageType(imageType));
        }
        filesToDelete.add(iod.getNetworkPathForImageType(primarySquareImageType));
        filesToDelete.add(iod.getNetworkPathForImageType(primaryImageType));

        // bl: let's only delete the files once the transaction committed successfully. this will ensure we don't delete the files
        // prior to transaction commit.
        RequestResponseHandler reqResp = networkContext().getReqResp();
        PartitionGroup.addEndOfPartitionGroupRunnable(() -> {
            for (NetworkPath networkPath : filesToDelete) {
                try {
                    GoogleCloudStorageFileHandler.IMAGES.removeFile(networkPath);
                } catch (Throwable t) {
                    StatisticManager.recordException(t, false, reqResp);
                    logger.error("Failed removing image from GCP Storage. Ignoring. ImageType/" + networkPath.getImageType() + " oid/" + iod.getOid(), t);
                }
            }
        });
    }

    @Override
    public void onEventListenerPreInsert(PreInsertEvent event) {
        ImageOnDisk iod = (ImageOnDisk) event.getEntity();

        NetworkPath networkPath = iod.getNetworkPathForImageType(iod.getFileMetaData().getPrimaryImageType());

        // bl: the mimeType isn't guaranteed to be correct since it depends on if the image was run through imagemagick or not.
        // it should always be an image type at least, so i'm not sure how much it really matters. after all,
        // this is consistent with how we are serving images currently through DisplayImageAction.
        putFile(networkPath, iod.getTempFile(), iod.getMimeType());

        // store all of the resized images, too
        storeResizedImages(iod);
    }

    private void storeResizedImages(ImageOnDisk iod) {
        for (Map.Entry<ImageType, File> entry : iod.getImageTypeToResizedImage().entrySet()) {
            ImageType imageType = entry.getKey();

            NetworkPath networkPath = iod.getNetworkPathForImageType(imageType);

            File tempFile = entry.getValue();
            putFile(networkPath, tempFile, iod.getMimeType());
        }
    }

    private void putFile(NetworkPath networkPath, File tempFile, String mimeType) {
        GoogleCloudStorageFileHandler.IMAGES.putFile(networkPath, tempFile, mimeType);
        // bl: now that we put the file successfully, let's add an error handler to remove the file if the
        // transaction ends up failing for some reason
        PartitionGroup.addEndOfPartitionGroupRunnableForError(() -> {
            GoogleCloudStorageFileHandler.IMAGES.removeFile(networkPath);
        });
    }
}
