package org.narrative.network.core.content.base;

import com.opensymphony.xwork2.util.CreateIfNull;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.core.fileondisk.base.services.FileUploadUtils;
import org.narrative.network.core.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class ContentWithAttachmentsFields<T extends ContentWithAttachmentsConsumer> extends ContentFields<T> {
    @CreateIfNull(false)
    private final List<? extends FileData> attachmentList;

    /**
     * use this constructor when creating new content
     */
    protected ContentWithAttachmentsFields(User user, OID fileUploadProcessOid) {
        super(user, fileUploadProcessOid);
        Map<OID, FileData> uniqueOidToFileData = FileUploadUtils.getUniqueOidToFileData(fileUploadProcessOid, getContentType().getAttachmentFileUsageType(), true);
        attachmentList = FileDataUtil.getSortedFileDataFromMap(uniqueOidToFileData);
    }

    /**
     * use this constructor when displaying content
     *
     * @param content the content to be displayed
     */
    protected ContentWithAttachmentsFields(Content content, T attachmentContent, OID fileUploadProcessOid) {
        super(content, fileUploadProcessOid);

        FilePointerSet fps = attachmentContent.getComposition().getFilePointerSet();
        Map<OID, FileData> uniqueOidToFileData = FileUploadUtils.initializeUniqueOidToFileDataForFilePointerSetIfNecessary(fileUploadProcessOid, content.getAttachmentFileUsageType(), fps);
        attachmentList = FileDataUtil.getSortedFileDataFromMap(uniqueOidToFileData);
    }

    public final List<? extends FileData> getAttachmentList() {
        return attachmentList;
    }

    public final Collection<? extends FileData> getFileData() {
        return getAttachmentList();
    }

}
