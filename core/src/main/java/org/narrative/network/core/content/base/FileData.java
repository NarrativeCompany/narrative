package org.narrative.network.core.content.base;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.fileondisk.base.FileMetaData;
import org.narrative.network.core.fileondisk.base.FileMetaDataProvider;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.shared.posting.Scrubbable;

import java.util.Collection;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Mar 24, 2006
 * Time: 8:55:01 AM
 *
 * @author Brian
 */
public interface FileData<T extends FileMetaData> extends FileMetaDataProvider<T>, Scrubbable {

    public OID getFileUploadProcessOid();

    public String getFilename();

    public String getMimeType();

    public int getByteSize();

    /**
     * is this file new to this usage.  true if editing a piece of content
     * and this file data already exists on the content.  false if either
     * uploading a new file or selecting a pre-existing file from the user's
     * files list.
     *
     * @return true if the file is new to this usage.
     */
    public boolean isNew();

    /**
     * return true if any characters have changed for this file.  used
     * for determining significant edits.  should be false for uploaded
     * files, and may be true for existing files.
     *
     * @return true if any chars have changed
     */
    public boolean isCharsChanged();

    public FileType getFileType();

    public String getTitle();

    public void setTitle(String title);

    public String getDescription();

    public void setDescription(String description);

    public int getOrder();

    public void setOrder(int threadingOrder);

    public boolean isInclude();

    public void setInclude(boolean include);

    public FileUsageType getFileUsageType();

    public void setFileUsageType(FileUsageType fileUsageType);

    public FilePointer getFilePointer();

    public void doSetFilePointer(FilePointer filePointer);

    public OID getUniqueOid();

    public OID getFileOnDiskOid();

    public FileOnDisk getFileOnDisk();

    public boolean isExistingFile();

    public void deleteAllTempFiles();

    static void validateAttachment(ValidationHandler handler, FileData attachment) {
        if (attachment == null || !attachment.isInclude()) {
            return;
        }

        String fieldNamePrefix = newString("uploadedFilesMap['", attachment.getFileUsageType(), "'].uniqueOidToFileDataMap['", attachment.getUniqueOid(), "']");
        handler.validateString(attachment.getTitle(), FileOnDisk.MIN_TITLE_LENGTH, FileOnDisk.MAX_TITLE_LENGTH, fieldNamePrefix + ".title", "fieldName.attachment.title");
        handler.validateString(attachment.getDescription(), FileOnDisk.MIN_DESCRIPTION_LENGTH, FileOnDisk.MAX_DESCRIPTION_LENGTH, fieldNamePrefix + ".description", "fieldName.attachment.caption");
    }

    static int getTotalFileCount(Collection<? extends FileData> fileList) {
        int count = 0;
        if (fileList != null) {
            for (FileData fileData : fileList) {
                if (fileData.isInclude()) {
                    count++;
                }
            }
        }
        return count;
    }
}
