package org.narrative.network.customizations.narrative.services;

import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.UnexpectedError;
import org.narrative.config.StaticConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.fileondisk.base.services.filesystem.FileHandler;
import org.narrative.network.core.fileondisk.base.services.filesystem.NetworkPath;
import lombok.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 9/6/18
 * Time: 10:20 PM
 *
 * @author brian
 */
public enum GoogleCloudStorageFileHandler implements FileHandler {
    IMAGES(true) {
        @Override
        protected NarrativeProperties.Storage getStorage() {
            return StaticConfig.getBean(NarrativeProperties.class).getStorage();
        }
    },
    KYC_FILES(false) {
        @Override
        protected NarrativeProperties.Storage getStorage() {
            return StaticConfig.getBean(NarrativeProperties.class).getKycStorage();
        }
    };

    private final boolean supportsPublicAccess;

    private volatile StorageDetails storageDetails;

    GoogleCloudStorageFileHandler(boolean supportsPublicAccess) {
        this.supportsPublicAccess = supportsPublicAccess;
    }

    protected abstract NarrativeProperties.Storage getStorage();

    @Override
    public String getFileUri(NetworkPath path) {
        assert supportsPublicAccess && !isEmpty(getStorageDetails().getBucketBaseUrl()) : "Should never attempt to get a file URI for a bucket that does not support public access!";
        return getStorageDetails().getBucketBaseUrl() + path.getRelativeFilePath(true);
    }

    public ObjectPair<String,File> getFile(NetworkPath path) {
        Blob blob = getStorageDetails().getStorage().get(getStorageDetails().getBucket(), getStorageDetails().getBlobPathPrefix() + path.getRelativeFilePath());
        // bl: Google deletes files after 30 days, so we need to handle when the file is no longer around
        if(blob==null) {
            return null;
        }
        ReadChannel reader = blob.reader();
        File tempFile = createTempFile(path.getFilename(), "tmp", true, true);
        try {
            FileOutputStream fileOuputStream = new FileOutputStream(tempFile);
            fileOuputStream.getChannel().transferFrom(reader, 0, Long.MAX_VALUE);
            fileOuputStream.close();
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Failed transferring file from GCP!", e);
        }
        return new ObjectPair<>(blob.getContentType(), tempFile);
    }

    @Override
    public void putFile(NetworkPath path, File f, String contentType) {
        BlobInfo blobInfo = BlobInfo.newBuilder(getStorageDetails().getBucket(), getStorageDetails().getBlobPathPrefix() + path.getRelativeFilePath())
                .setContentType(contentType)
                // bl: gzip encoding is an option to save on disk space. the files will be transcoded on-the-fly
                // when served to users:
                // https://cloud.google.com/storage/docs/transcoding#gzip-gzip
                // https://cloud.google.com/storage/docs/metadata#content-encoding
                // google recommends against using it on already-compressed files such as jpeg images , so
                // leaving this off for now. if we want to support it, we would just need to uncomment this
                // and then send the file through a gzip input stream before sending the file to gcp.
                //.setContentEncoding("gzip")
                .build();
        try {
            createCloudFile(f.toPath(), blobInfo);
        } catch(IOException|StorageException e) {
            throw UnexpectedError.getRuntimeException("Failed putting file onto GCP storage! ", e);
        }
    }

    private void createCloudFile(Path filePath, BlobInfo blobInfo) throws IOException {
        if (Files.size(filePath) > 1_000_000) {
            // When content is not available or large (1MB or more) it is recommended
            // to write it in chunks via the blob's channel writer.
            try (WriteChannel writer = getStorageDetails().getStorage().writer(blobInfo)) {
                byte[] buffer = new byte[1024];
                try (InputStream input = Files.newInputStream(filePath)) {
                    int limit;
                    while ((limit = input.read(buffer)) >= 0) {
                        writer.write(ByteBuffer.wrap(buffer, 0, limit));
                    }
                }
            }
        } else {
            byte[] bytes = Files.readAllBytes(filePath);
            // create the blob in one request.
            getStorageDetails().getStorage().create(blobInfo, bytes);
        }
    }

    @Override
    public void removeFile(NetworkPath path) {
        getStorageDetails().getStorage().delete(getStorageDetails().getBucket(), getStorageDetails().getBlobPathPrefix() + path.getRelativeFilePath());
    }

    private StorageDetails getStorageDetails() {
        // bl: double-checked lock (with volatile reference) to ensure that we only initialize
        // the StorageDetails object a single time.
        StorageDetails ret = storageDetails;
        if(ret==null) {
            synchronized(this) {
                ret = storageDetails;
                if(ret==null) {
                    storageDetails = ret = createStorageDetails();
                }
            }
        }
        return ret;
    }

    private StorageDetails createStorageDetails() {
        NarrativeProperties.Storage storageProps = getStorage();

        // first, load the credentials from a file
        Credentials credentials;
        try (FileInputStream serviceAccountStream = new FileInputStream(storageProps.getCredentialsPath())) {
            credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
        } catch(IOException e) {
            throw UnexpectedError.getRuntimeException("Failed to load Google Cloud Storage Credentials from " + storageProps.getCredentialsPath(), e);
        }

        String bucket = storageProps.getBucket();
        String blobPathPrefix = storageProps.getBlobPathPrefix();
        String bucketBaseUrl = supportsPublicAccess ? "https://" + bucket + "/" + blobPathPrefix : null;

        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        // create and return the StorageDetails
        return new StorageDetails(storage, bucket, blobPathPrefix, bucketBaseUrl);
    }

    @Data
    private static class StorageDetails {
        private final Storage storage;
        private final String bucket;
        private final String blobPathPrefix;
        private final String bucketBaseUrl;
    }
}
