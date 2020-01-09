package org.narrative.network.customizations.narrative.service.impl.narrativepost;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.FileData;
import org.narrative.network.core.content.base.UploadedFileData;
import org.narrative.network.core.content.base.services.UpdateFilePointerSet;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.services.CreateUpdateFiles;
import org.narrative.network.core.fileondisk.base.services.FileUploadUtils;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.customizations.narrative.controller.PostController;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.services.RestUploadUtils;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.jooq.lambda.function.Consumer4;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-01-11
 * Time: 09:34
 *
 * @author jonmark
 */
public class UploadPostAttachmentTask extends AreaTaskImpl<ImageOnDisk> {
    private final OID postOid;
    private final MultipartFile file;

    public UploadPostAttachmentTask(OID postOid, MultipartFile file) {
        this.postOid = postOid;
        this.file = file;
    }

    @Override
    protected ImageOnDisk doMonitoredTask() {
        getAreaContext().getAreaRole().checkNarrativeRight(NarrativePermissionType.POST_CONTENT);

        Content content = Content.dao().getForApiParam(postOid, PostController.POST_OID_PARAM);

        assert content.getContentType().isNarrativePost() : "Should only ever provide narrative posts!";

        // jw: ensure that the current user has the right to edit this post (that they are the author)
        content.checkEditRightForCurrentUser();

        // create the UploadedFileData from the provided file.
        UploadedFileData uploadedFileData = RestUploadUtils.getUploadedFileData(file, content.getAttachmentFileUsageType());

        Consumer4<NetworkContext, UploadedFileData, FileType, ValidationContext> validateAvatarUploadFn = RestUploadUtils::validateUploadedFileData;

        //Pass the validation function into the task implementation but curry all but the last argument which is the validation context that will be injected by the task
        getAreaContext().doAreaTask(new AreaTaskImpl<Object>(true, validateAvatarUploadFn.acceptPartially(getNetworkContext(), uploadedFileData, FileType.IMAGE)) {
            @Override
            protected Object doMonitoredTask() {
                OID fileUploadProcessOid = uploadedFileData.getFileUploadProcessOid();
                // bl: process the file synchronously now
                uploadedFileData.postUploadProcess(fileUploadProcessOid);

                // jw: before we move on to the composition side of things, let's process this uploaded file data and allow
                //     our code to create the FileOnDisk for it.
                getNetworkContext().doGlobalTask(new CreateUpdateFiles(Collections.singletonList(uploadedFileData), getNetworkContext().getUser()));

                // jw: at this point, the uploaded file data should have a FileOnDisk, so we are clear to proceed.
                getNetworkContext().doCompositionTask(content.getCompositionPartition(), new CompositionTaskImpl<Object>() {
                    @Override
                    protected Object doMonitoredTask() {
                        // jw: now that we are in context of the appropriate composition partition, we can load it and add
                        //     the uploaded file data in with the rest of the file pointers
                        Composition composition = content.getComposition();

                        Map<OID, FileData> uniqueOidToFileData = FileUploadUtils.initializeUniqueOidToFileDataForFilePointerSetIfNecessary(
                                fileUploadProcessOid,
                                content.getAttachmentFileUsageType(),
                                composition.getFilePointerSet()
                        );
                        uniqueOidToFileData.put(uploadedFileData.getUniqueOid(), uploadedFileData);

                        // bl: in order to avoid issues with updating FilePointerSet stats, we need to lock
                        // on the Composition before we start the updates. this will ensure that we only ever
                        // process and update a single FilePointer at a time per Composition.
                        // note that on the first file upload, we won't even have a FilePointerSet yet.
                        Composition.dao().refreshForLock(composition);

                        FilePointerSet<FilePointer> fps = composition.getFilePointerSet();
                        if (exists(fps)) {
                            // bl: now that we have locked the Composition, we also need to refresh the FilePointerSet
                            // to ensure we have the latest data from any other committed transactions
                            FilePointerSet.dao().refresh(fps);
                            // bl: in order to avoid reordering threadingOrders for all other attachments with every new
                            // file that is uploaded, let's set the order of the new file to be at the end of the current
                            // list. that way, it should just be tacked onto the end and all existing FilePointers
                            // should remain unaffected by this upload process.
                            uploadedFileData.setOrder(fps.getMaxThreadingOrder()+1);
                        }
                        fps = getNetworkContext().doGlobalTask(new UpdateFilePointerSet(composition, fps, newLinkedList(uniqueOidToFileData.values())));
                        composition.setFilePointerSet(exists(fps) ? fps : null);
                        // bl: clean up even when in error. we don't want this sticking around beyond the duration of this request at all.
                        FileUploadUtils.cleanUpFilesAfterFormCompletionAtEndOfPartitionGroup(fileUploadProcessOid, true);

                        return null;
                    }
                });

                // bl: flush all sessions so that we save the files to GCP before we delete them all
                PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

                // bl: now that we are done, let's delete all of the temp files created internally in the UploadedImageFileData
                uploadedFileData.deleteAllTempFiles();

                return null;
            }
        });

        return (ImageOnDisk) uploadedFileData.getFileOnDisk();
    }
}
