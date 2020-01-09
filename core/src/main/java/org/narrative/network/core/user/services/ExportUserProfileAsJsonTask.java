package org.narrative.network.core.user.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.IPHttpUtil;
import org.narrative.common.util.IPIOUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.config.StaticConfig;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileOnDiskStats;
import org.narrative.network.core.fileondisk.base.services.filesystem.NetworkPath;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.service.api.model.PostDetailDTO;
import org.narrative.network.customizations.narrative.service.mapper.PostMapper;
import org.narrative.network.customizations.narrative.services.GoogleCloudStorageFileHandler;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.util.NetworkDateUtils;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 9/29/18
 * Time: 4:06 PM
 *
 * @author brian
 */
public class ExportUserProfileAsJsonTask extends GlobalTaskImpl<File> {
    private static final NetworkLogger logger = new NetworkLogger(ExportUserProfileAsJsonTask.class);
    public static final String FIELD_EMAIL_ADDRESS = "Email Address";
    private static final String FIELD_AVATAR_IMAGE = "Avatar Image";

    private static final String FILE_NAME_SUFFIX = "-profile-export.zip";

    private final User user;

    private final Map<String, Object> rootJsonMap = new LinkedHashMap<>();
    private final List<ObjectPair<String, File>> filesToSave = new ArrayList<>();

    public ExportUserProfileAsJsonTask(User user) {
        super(false);
        this.user = user;
    }

    @Override
    protected File doMonitoredTask() {
        try {
            return generateProfileData();
        } catch (Exception e) {
            throw UnexpectedError.getRuntimeException("Failed generating user profile JSON.", e);
        }
    }

    private File generateProfileData() throws Exception {
        addString("Name", user.getDisplayName());
        addString("Handle", "@" + user.getUsername());
        addString("Join Date", NetworkDateUtils.getIso8601DatetimeString(user.getUserFields().getRegistrationDate()));
        addString("Last Visit Date", NetworkDateUtils.getIso8601DatetimeString(user.getUserStats().getLastLoginDatetime()));
        addString("Reputation", Integer.toString(user.getReputation().getTotalScore()));
        NeoWallet neoWallet = user.getWallet().getNeoWallet();
        if(exists(neoWallet)) {
            addString("NEO Wallet Address", neoWallet.getNeoAddress());
        }
        addString(FIELD_EMAIL_ADDRESS, user.getEmailAddress());

        //mk: add avatar file
        if (user.isHasAvatarSet()) {
            ImageOnDisk avatar = user.getAvatar();
            NetworkPath networkPath = avatar.getNetworkPathForImageType(avatar.getPrimarySquareAvatarImageType());
            ObjectPair<String,File> typeAndFile = GoogleCloudStorageFileHandler.IMAGES.getFile(networkPath);
            if(typeAndFile==null) {
                if(logger.isWarnEnabled()) logger.warn("Failed to fetch image attachment from " + networkPath + "(URL: " + avatar.getPrimarySquareAvatarImageUrl() + "). Skipping.");
            } else {
                addFile(FIELD_AVATAR_IMAGE, networkPath.getFilename(), typeAndFile.getTwo());
            }
        }

        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        //mk: now create temp zip file to save everything to it
        File zipFileToReturn = createTempFile(user.getOid() + "-profile-export", "zip", false, true);

        om.enable(SerializationFeature.INDENT_OUTPUT);
        //mk: write json string
        String jsonString = om.writeValueAsString(rootJsonMap);
        assert !isEmpty(jsonString) : "Should have some data here";

        //mk: save json to a file
        File profileDataJson = createTempFile(user.getOid() + "-profile-data", "json", false, true);
        FileUtils.writeStringToFile(profileDataJson, jsonString, IPUtil.IANA_UTF8_ENCODING_NAME);

        //mk: add json file to the list of all files to be zipped
        filesToSave.add(new ObjectPair<>("profile-data.json", profileDataJson));

        Map<String, Object> postsJsonMap = getPostsJsonMap();
        if(!isEmptyOrNull(postsJsonMap)) {
            String postsJson = om.writeValueAsString(postsJsonMap);
            File postsJsonFile = createTempFile(user.getOid() + "-posts-data", "json", false, true);
            FileUtils.writeStringToFile(postsJsonFile, postsJson, IPUtil.IANA_UTF8_ENCODING_NAME);
            filesToSave.add(new ObjectPair<>("posts.json", postsJsonFile));
        }

        //mk: finally add json text and other files to the zip
        ZipOutputStream zippedData = null;
        try {
            zippedData = addCsvFilesToZip(filesToSave, zipFileToReturn);
        } finally {
            IPIOUtil.doSafeClose(zippedData);
        }
        return zipFileToReturn;
    }

    public static ZipOutputStream addCsvFilesToZip(Collection<ObjectPair<String, File>> csvFiles, File zipFile) throws IOException {
        ZipOutputStream zippedData = new ZipOutputStream(new FileOutputStream(zipFile));
        for (ObjectPair<String, File> files : csvFiles) {
            FileInputStream fis = new FileInputStream(files.getTwo());
            ZipEntry ze = new ZipEntry(files.getOne());
            zippedData.putNextEntry(ze);
            IPIOUtil.doStreamInputToOutputWithBuffering(fis, zippedData, false);
        }

        return zippedData;
    }

    private void addFile(String fieldName, String fileName, File file) {
        addString(fieldName, fileName);
        filesToSave.add(new ObjectPair<>(fileName, file));
    }

    private void addString(String fieldName, String fieldValue) {
        if (!isEmpty(fieldValue)) {
            assert !rootJsonMap.containsKey(fieldName) : "Should not use the same field name twice!";
            rootJsonMap.put(fieldName, fieldValue);
        }
    }

    public String getContentDisposition() {
        return IPHttpUtil.getFileDownloadContentDisposition(getFileNameForDownload(user));
    }

    public static String getFileNameForDownload(User user) {
        return user.getIdForUrl() + FILE_NAME_SUFFIX;
    }

    private Map<String, Object> getPostsJsonMap() {
        List<Content> contents = Content.dao().getAllCreatedByUser(user, ContentType.NARRATIVE_POST);
        if(isEmptyOrNull(contents)) {
            return null;
        }
        // bl: have to initialize the compositions so we can output the full PostDetailDTO.
        Composition.loadCompositions(contents, true);
        Map<String, List<OID>> contentIdToImageOnDiskOids = new LinkedHashMap<>();
        for (Content content : contents) {
            // bl: we don't need ratings, so just set them all to null so they won't be included in the DTOs
            content.setQualityRatingByCurrentUser(null);
            content.setAgeRatingByCurrentUser(null);
            // bl: avoid overhead of permission checks, which can result in a lot of one-off queries for ChannelUser records.
            // these editable/deletable flags are really unnecessary from an export standpoint.
            content.setIgnoreEditableDeletableChecksForDto();

            // bl: add the attachments if there are any
            FilePointerSet<FilePointer> filePointerSet = content.getCompositionCache().getComposition().getFilePointerSet();
            List<FilePointer> filePointers = filePointerSet==null ? null : filePointerSet.getFilePointerList();
            if(isEmptyOrNull(filePointers)) {
                continue;
            }
            List<OID> imageOnDiskOids = new ArrayList<>(filePointers.size());
            for (FilePointer filePointer : filePointers) {
                imageOnDiskOids.add(filePointer.getFileOnDiskOid());
            }
            contentIdToImageOnDiskOids.putIfAbsent(content.getIdForUrl(), imageOnDiskOids);
        }
        // bl: bulk load all of the FileOnDisk and FileOnDiskStats records for performance
        List<OID> allImageOnDiskOids = contentIdToImageOnDiskOids.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        FileOnDiskStats.dao().getObjectsFromIDsWithCache(allImageOnDiskOids);
        FileOnDisk.dao().getObjectsFromIDsWithCache(allImageOnDiskOids);
        for (Map.Entry<String, List<OID>> entry : contentIdToImageOnDiskOids.entrySet()) {
            String contentId = entry.getKey();
            List<OID> imageOnDiskOids = entry.getValue();
            for (OID imageOnDiskOid : imageOnDiskOids) {
                ImageOnDisk iod = cast(FileOnDisk.dao().get(imageOnDiskOid), ImageOnDisk.class);
                NetworkPath networkPath = iod.getPrimaryImageNetworkPath();
                ObjectPair<String,File> typeAndFile = GoogleCloudStorageFileHandler.IMAGES.getFile(networkPath);
                if(typeAndFile==null) {
                    if(logger.isWarnEnabled()) logger.warn("Failed to fetch image attachment from " + networkPath + "(URL: " + iod.getPrimaryImageUrl() + "). Skipping.");
                    continue;
                }
                // bl: put all attachments in a directory based on the content's pretty URL string
                filesToSave.add(new ObjectPair<>("attachments/" + contentId + "/" + iod.getOid() + "-" + networkPath.getFilename(), typeAndFile.getTwo()));
            }
        }
        Set<Channel> allPublishedChannels = contents.stream().map(Content::getPublishedToChannels).flatMap(Collection::stream).collect(Collectors.toSet());
        // bl: easiest path is to include the followed status of all channels
        FollowedChannel.dao().populateChannelsFollowedByCurrentUserField(networkContext().getUser(), allPublishedChannels);
        PostMapper mapper = StaticConfig.getBean(PostMapper.class);
        List<PostDetailDTO> postDetailDTOs = mapper.mapContentListToPostDetailDTOList(contents);
        return Collections.singletonMap("posts", postDetailDTOs);
    }
}
