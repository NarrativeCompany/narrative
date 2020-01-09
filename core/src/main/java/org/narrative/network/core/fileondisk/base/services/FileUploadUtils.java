package org.narrative.network.core.fileondisk.base.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.QuartzUtil;
import org.narrative.common.util.SyncObjectManager;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.content.base.ExistingFileData;
import org.narrative.network.core.content.base.ExistingVideoFileData;
import org.narrative.network.core.content.base.FileData;
import org.narrative.network.core.content.base.FileDataUtil;
import org.narrative.network.core.content.base.UploadedFileData;
import org.narrative.network.core.fileondisk.base.FileBase;
import org.narrative.network.core.fileondisk.base.FileBaseProvider;
import org.narrative.network.core.fileondisk.base.FileBaseProviderCollectionContainer;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.fileondisk.base.UniqueOidToFileDataMapWrapper;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.system.ThreadBucketType;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Jul 5, 2006
 * Time: 9:55:39 AM
 *
 * @author Brian
 */
public class FileUploadUtils {
    private static final NetworkLogger logger = new NetworkLogger(FileUploadUtils.class);

    private static final Map<String, UploadedFileData> FILE_BY_FILE_ID_MAP = new ConcurrentHashMap<String, UploadedFileData>();

    static final SyncObjectManager<OID> PRIMARY_ROLE_OID_LOCK_MANAGER = new SyncObjectManager<OID>();
    static final Map<OID, Map> PRIMARY_ROLE_OID_TO_FILES_IN_USE = new ConcurrentHashMap<OID, Map>();

    public static void init() {

        // Cleanup all old temp files now
        final File[] tempFiles = NetworkRegistry.getInstance().getTempDir().listFiles();

        // bl: changing to run in the utility thread so that deleting the files will not hold up initialization.
        // nb. note that we got the list of files inline during initialization in order to ensure that we don't
        // delete any new files that may be created during/after initialization but before the utility thread
        // runnable has executed.
        ThreadBucketType.UTILITY.addRunnable(new Runnable() {
            @Override
            public void run() {
                for (File tempFile : tempFiles) {
                    if (!tempFile.exists() || !tempFile.isFile()) {
                        continue;
                    }
                    if (!tempFile.delete()) {
                        logger.warn("Could not delete temp file: " + tempFile.getAbsolutePath());
                    }
                }
            }
        });

        // run a cleanup process every half hour to clean up any files older than 2 hours
        QuartzJobScheduler.LOCAL.schedule(QuartzJobScheduler.createRecoverableJobBuilder(CleanUpOldFileDataJob.class), QuartzUtil.makeMinutelyTrigger(30));
    }

    /**
     * Gets a flash uploaded file data
     *
     * @param fileId the id of the file to get
     * @param remove true if we should remove the file ID from the map
     * @return the UploadedFileData
     */
    public static UploadedFileData getUploadedFileData(String fileId, boolean remove) {
        if (remove) {
            return FILE_BY_FILE_ID_MAP.remove(fileId);
        } else {
            return FILE_BY_FILE_ID_MAP.get(fileId);
        }
    }

    /**
     * Sets a flash file data object on the session for later retreival
     *
     * @param fileId   the id of the file to set
     * @param fileData the UploadedFileData
     */
    public static void setUploadedFileData(String fileId, UploadedFileData fileData) {
        FILE_BY_FILE_ID_MAP.put(fileId, fileData);
    }

    public static Map<OID, FileData> initUploadedFilesMapForFileUploadProcessOidAndType(OID fileUploadProcessOid, FileUsageType fileUsageType) {
        if (fileUploadProcessOid == null) {
            // fileUploadProcessOid not valid, so dummy map.
            return new HashMap<OID, FileData>();
        }
        return getUniqueOidToFileData(fileUploadProcessOid, fileUsageType, true);
    }

    public static void cleanUpFilesAfterFormCompletionAtEndOfPartitionGroup(OID fileUploadProcessOid) {
        cleanUpFilesAfterFormCompletionAtEndOfPartitionGroup(fileUploadProcessOid, false);
    }

    public static void cleanUpFilesAfterFormCompletionAtEndOfPartitionGroup(final OID fileUploadProcessOid, boolean evenWhenThreadInError) {
        if (fileUploadProcessOid == null) {
            return;
        }
        PrimaryRole primaryRole = networkContext().getPrimaryRole();
        final OID primaryRoleOid = primaryRole.getOid();

        Runnable runnable = new Runnable() {
            public void run() {
                FileUploadUtils.cleanUpAfterFormCompletion(primaryRoleOid, fileUploadProcessOid);
            }
        };

        if (evenWhenThreadInError) {
            PartitionGroup.addEndOfPartitionGroupRunnableForSuccessOrError(runnable);
        } else {
            PartitionGroup.addEndOfPartitionGroupRunnable(runnable);
        }
    }

    static void cleanUpAfterFormCompletion(OID primaryRoleOid, OID fileUploadProcessOid) {
        {
            Map<FileUsageType, UniqueOidToFileDataMapWrapper<FileData>> map = getUploadedFilesMap(fileUploadProcessOid, primaryRoleOid, true, false);
            if (map != null) {
                Collection<FileData> files = new HashSet<FileData>();
                for (UniqueOidToFileDataMapWrapper<FileData> wrapper : map.values()) {
                    files.addAll(wrapper.getUniqueOidToFileDataMap().values());
                }
                FileDataUtil.deleteTempUploadedFilesFromFileDataCollection(files);
            }
        }
        Map<OID, FileUploadProcessOidData<FileData>> map = getAllFormFileDataForUser(primaryRoleOid, false);
        if (map != null) {
            map.remove(fileUploadProcessOid);
        }
    }

    static <T extends FileData> Map<OID, FileUploadProcessOidData<T>> getAllFormFileDataForUser(final OID primaryRoleOid, boolean createIfNecessary) {
        Map<OID, FileUploadProcessOidData<T>> fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData = PRIMARY_ROLE_OID_TO_FILES_IN_USE.get(primaryRoleOid);
        if (fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData == null && createIfNecessary) {
            synchronized (PRIMARY_ROLE_OID_LOCK_MANAGER.getSyncObject(primaryRoleOid)) {
                fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData = PRIMARY_ROLE_OID_TO_FILES_IN_USE.get(primaryRoleOid);
                if (fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData == null) {
                    PRIMARY_ROLE_OID_TO_FILES_IN_USE.put(primaryRoleOid, fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData = new ConcurrentHashMap<OID, FileUploadProcessOidData<T>>());
                }
            }
        }
        return fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData;
    }

    public static <T extends FileData> Map<FileUsageType, UniqueOidToFileDataMapWrapper<T>> getUploadedFilesMap(OID fileUploadProcessOid, boolean canCreateNewFiles) {
        PrimaryRole primaryRole = networkContext().getPrimaryRole();
        return getUploadedFilesMap(fileUploadProcessOid, primaryRole.getOid(), canCreateNewFiles, true);
    }

    private static <T extends FileData> Map<FileUsageType, UniqueOidToFileDataMapWrapper<T>> getUploadedFilesMap(OID fileUploadProcessOid, OID primaryRoleOid, boolean canUploadNewFiles, boolean createIfNecessary) {
        if (fileUploadProcessOid == null) {
            return null;
        }
        Map<OID, FileUploadProcessOidData<T>> fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData = getAllFormFileDataForUser(primaryRoleOid, createIfNecessary);
        if (fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData == null) {
            return null;
        }
        FileUploadProcessOidData<T> fileUploadProcessOidData = fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData.get(fileUploadProcessOid);
        if (fileUploadProcessOidData == null && !createIfNecessary) {
            return null;
        }
        if (fileUploadProcessOidData == null) {
            Map<FileUsageType, UniqueOidToFileDataMapWrapper<T>> fileUsageTypeToUniqueOidToFileData = new HashMap<FileUsageType, UniqueOidToFileDataMapWrapper<T>>();
            fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData.put(fileUploadProcessOid, fileUploadProcessOidData = new FileUploadProcessOidData<T>(fileUploadProcessOid, canUploadNewFiles, fileUsageTypeToUniqueOidToFileData));
        }
        return fileUploadProcessOidData.fileUsageTypeToUniqueOidMap;
    }

    public static boolean isAllowNewFileUploads(OID fileUploadProcessOid, FileUsageType fileUsageType) {
        Map<OID, FileUploadProcessOidData<FileData>> fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData = getAllFormFileDataForUser(networkContext().getPrimaryRole().getOid(), false);
        if (fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData != null) {
            FileUploadProcessOidData<FileData> fileUploadProcessOidData = fileUploadProcessOidToFileUsageTypeToUniqueOidToFileData.get(fileUploadProcessOid);
            if (fileUploadProcessOidData != null) {
                return fileUploadProcessOidData.canUploadNewFiles;
            }
        }
        return true;
    }

    private static <T extends FileData> UniqueOidToFileDataMapWrapper<T> getUniqueOidToFileDataWrapper(OID fileUploadProcessOid, FileUsageType fileUsageType, boolean forceNewUploadsNotAllowed, boolean createIfNecessary) {
        Map<FileUsageType, UniqueOidToFileDataMapWrapper<T>> fileUsageTypeToUniqueOidToFileData = getUploadedFilesMap(fileUploadProcessOid, !forceNewUploadsNotAllowed);
        if (fileUsageTypeToUniqueOidToFileData == null) {
            return null;
        }
        UniqueOidToFileDataMapWrapper<T> wrapper = fileUsageTypeToUniqueOidToFileData.get(fileUsageType);
        if (wrapper == null && createIfNecessary) {
            fileUsageTypeToUniqueOidToFileData.put(fileUsageType, wrapper = new UniqueOidToFileDataMapWrapper<T>());
        }
        return wrapper;
    }

    public static <T extends FileData> Map<OID, T> getUniqueOidToFileData(OID fileUploadProcessOid, FileUsageType fileUsageType, boolean createIfNecessary) {
        return getUniqueOidToFileData(fileUploadProcessOid, fileUsageType, false, createIfNecessary);
    }

    public static <T extends FileData> Map<OID, T> getUniqueOidToFileData(OID fileUploadProcessOid, FileUsageType fileUsageType, boolean forceNewUploadsNotAllowed, boolean createIfNecessary) {
        if (fileUploadProcessOid == null) {
            return null;
        }
        UniqueOidToFileDataMapWrapper<T> wrapper = getUniqueOidToFileDataWrapper(fileUploadProcessOid, fileUsageType, forceNewUploadsNotAllowed, createIfNecessary);
        if (wrapper == null) {
            return null;
        }
        return wrapper.getUniqueOidToFileDataMap();
    }

    public static <T extends FileData> Map<OID, T> initializeUniqueOidToFileDataForFilePointerSetIfNecessary(OID fileUploadProcessOid, FileUsageType fileUsageType, FileBaseProviderCollectionContainer fileBaseProviderCollectionContainer) {
        return initializeUniqueOidToFileDataForFilePointerSetIfNecessary(fileUploadProcessOid, fileUsageType, fileBaseProviderCollectionContainer, false);
    }

    public static <T extends FileData> Map<OID, T> initializeUniqueOidToFileDataForFilePointerSetIfNecessary(OID fileUploadProcessOid, FileUsageType fileUsageType, FileBaseProviderCollectionContainer fileBaseProviderCollectionContainer, boolean forceNewUploadsNotAllowed) {
        if (fileUploadProcessOid == null) {
            return null;
        }
        Map<OID, T> uniqueOidToFileData = getUniqueOidToFileData(fileUploadProcessOid, fileUsageType, forceNewUploadsNotAllowed, false);
        // if we already have this map in the session, then use the map in the session.
        // if this is the first time we're rendering the form with this oid, then pre-populate the map with the existing file data.
        if (uniqueOidToFileData != null) {
            return uniqueOidToFileData;
        }

        // create the map
        UniqueOidToFileDataMapWrapper uniqueOidToFileDataWrapper = getUniqueOidToFileDataWrapper(fileUploadProcessOid, fileUsageType, forceNewUploadsNotAllowed, true);
        uniqueOidToFileData = uniqueOidToFileDataWrapper.getUniqueOidToFileDataMap();
        if (fileBaseProviderCollectionContainer == null) {
            return uniqueOidToFileData;
        }
        Collection<FileBaseProvider> fileBaseProviders = fileBaseProviderCollectionContainer.getFileBaseProviderCollection();
        if (isEmptyOrNull(fileBaseProviders)) {
            return uniqueOidToFileData;
        }
        for (FileBaseProvider fileBaseProvider : fileBaseProviders) {
            ExistingFileData existingFileData = ExistingFileData.getExistingFileData(fileUploadProcessOid, fileBaseProvider, fileUsageType);
            uniqueOidToFileData.put(existingFileData.getUniqueOid(), (T) existingFileData);
            uniqueOidToFileDataWrapper.setMaxOrder(Math.max(uniqueOidToFileDataWrapper.getMaxOrder(), existingFileData.getOrder()));
        }
        return uniqueOidToFileData;
    }

    public static <T extends FileData> Map<OID, T> initializeUniqueOidToFileDataForFileBaseIfNecessary(OID fileUploadProcessOid, FileUsageType fileUsageType, FileBase fileBase) {
        Map<OID, T> uniqueOidToFileData = getUniqueOidToFileData(fileUploadProcessOid, fileUsageType, false);
        // if we already have this map in the session, then use the map in the session.
        // if this is the first time we're rendering the form with this oid, then pre-populate the map with the existing file data.
        if (uniqueOidToFileData != null) {
            return uniqueOidToFileData;
        }

        // create the map
        UniqueOidToFileDataMapWrapper uniqueOidToFileDataWrapper = getUniqueOidToFileDataWrapper(fileUploadProcessOid, fileUsageType, false, true);
        uniqueOidToFileData = uniqueOidToFileDataWrapper.getUniqueOidToFileDataMap();
        if (!exists(fileBase)) {
            return uniqueOidToFileData;
        }
        ExistingFileData existingFileData = ExistingFileData.getExistingFileData(fileUploadProcessOid, fileBase, fileUsageType, false);
        uniqueOidToFileData.put(existingFileData.getUniqueOid(), (T) existingFileData);
        uniqueOidToFileDataWrapper.setMaxOrder(Math.max(uniqueOidToFileDataWrapper.getMaxOrder(), existingFileData.getOrder()));
        return uniqueOidToFileData;
    }

    public static void changeVideoOnDiskOid(OID originalVideoOnDiskOid, OID newVideoOnDiskOid) {

        for (Map filesInUseMap : PRIMARY_ROLE_OID_TO_FILES_IN_USE.values()) {
            for (Object fileUploadProcessOidDataObj : filesInUseMap.values()) {
                FileUploadProcessOidData fileUploadProcessOidData = (FileUploadProcessOidData) (fileUploadProcessOidDataObj);
                Collection<UniqueOidToFileDataMapWrapper> uniqueOidToFileDataMapWrapperMap = (Collection<UniqueOidToFileDataMapWrapper>) fileUploadProcessOidData.fileUsageTypeToUniqueOidMap.values();

                for (UniqueOidToFileDataMapWrapper uniqueOidToFileDataMapWrapper : uniqueOidToFileDataMapWrapperMap) {
                    for (Object filedataObj : uniqueOidToFileDataMapWrapper.getUniqueOidToFileDataMap().values()) {
                        if (filedataObj instanceof ExistingVideoFileData) {
                            ExistingVideoFileData existingVideoFileData = (ExistingVideoFileData) filedataObj;
                            if (CoreUtils.isEqual(existingVideoFileData.getFileOnDiskOid(), originalVideoOnDiskOid)) {
                                existingVideoFileData.setFileOnDiskOid(newVideoOnDiskOid);
                            }
                        }
                    }
                }
            }
        }
    }

    static class FileUploadProcessOidData<T extends FileData> {
        final OID fileUploadProcessOid;
        final long fileUploadProcessOidCreatedDatetime;
        final boolean canUploadNewFiles;
        private final Map<FileUsageType, UniqueOidToFileDataMapWrapper<T>> fileUsageTypeToUniqueOidMap;

        public FileUploadProcessOidData(OID fileUploadProcessOid, boolean canUploadNewFiles, Map<FileUsageType, UniqueOidToFileDataMapWrapper<T>> fileUsageTypeToUniqueOidMap) {
            this.fileUploadProcessOid = fileUploadProcessOid;
            this.fileUploadProcessOidCreatedDatetime = System.currentTimeMillis();
            this.canUploadNewFiles = canUploadNewFiles;
            this.fileUsageTypeToUniqueOidMap = fileUsageTypeToUniqueOidMap;
        }
    }

    public static String getMimeTypeFromFile(File file, OID debugOid) {
        try {
            FileInputStream fis = new FileInputStream(file);
            Metadata metadata = new Metadata();
            try {
                new AutoDetectParser().parse(fis, new DefaultHandler(), metadata, new ParseContext());
            } finally {
                fis.close();
            }
            String contentType = metadata.get(Metadata.CONTENT_TYPE);
            if (!isEmpty(contentType)) {
                return contentType;
            }
        } catch (Throwable t) {
            StatisticManager.recordException(UnexpectedError.getIgnorableRuntimeException("Failed extracting MimeType from file/" + file.getAbsolutePath(), t), false, networkContext().getReqResp());
            logger.error("Failed parsing content type for File. Ignoring for oid/" + debugOid + " file/" + file.getAbsolutePath(), t);
        }

        return null;
    }

    private static final List<Property> PROPERTIES_TO_INDEX = Collections.unmodifiableList(Arrays.asList(TikaCoreProperties.TITLE, TikaCoreProperties.DESCRIPTION, TikaCoreProperties.KEYWORDS, TikaCoreProperties.COMMENTS, TikaCoreProperties.CREATOR, TikaCoreProperties.CONTRIBUTOR));

    public static String getIndexContentFromInputStream(InputStream stream, String debugInfo) {
        StringBuilder sb = new StringBuilder();
        try {
            Metadata metadata = new Metadata();
            StringWriter writer = new StringWriter();
            try {
                new AutoDetectParser().parse(stream, new BodyContentHandler(writer), metadata, new ParseContext());
            } finally {
                stream.close();
            }
            // bl: include all of the properties first
            for (Property property : PROPERTIES_TO_INDEX) {
                String prop = metadata.get(property);
                if (!isEmpty(prop)) {
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(prop);
                }
            }
            if (sb.length() > 0) {
                sb.append('\n');
            }
            // bl: then include the body
            sb.append(writer.toString());
        } catch (Throwable t) {
            StatisticManager.recordException(UnexpectedError.getIgnorableRuntimeException("Failed extracting index content from file/" + debugInfo, t), false, networkContext().getReqResp());
            logger.error("Failed parsing content for File. Ignoring for file/" + debugInfo, t);
        }
        return sb.toString();
    }

}
