package org.narrative.network.core.content.base;

import org.narrative.common.core.services.interceptors.SubPropertySettable;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPUtil;
import org.narrative.common.web.struts.AfterPrepare;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.fileondisk.base.services.FileUploadUtils;
import com.opensymphony.xwork2.conversion.impl.InstantiatingNullHandler;

import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Struts can't know what type of FileData object to create.  Thus, we have
 * the FileDataWrapper object which can wrap the behavior of properly instantiating
 * either an ExistingFileData or an UploadedFileData.
 * <p>
 * Date: Mar 27, 2006
 * Time: 12:20:04 PM
 *
 * @author Brian
 */
public class FileDataWrapper {

    private String fileId;
    private OID fileUploadProcessOid;
    private FileUsageType fileUsageType;
    private FileOnDisk fileOnDisk;
    private FileData fileData;
    private static final String FILE_DATA_FIELD_NAME = "fileData";
    private boolean isFileDataSet = false;

    private boolean autoInsertIntoPostBody = false;

    public static class FileDataWrapperNullHandler extends InstantiatingNullHandler {
        public Object nullPropertyValue(Map context, Object target, Object property) {
            String propertyName = property.toString();
            // bl: don't want to instantiate the fileData property.
            if (IPUtil.isEqual(FILE_DATA_FIELD_NAME, propertyName)) {
                return null;
            }
            return super.nullPropertyValue(context, target, property);
        }
    }

    /**
     * @deprecated for struts use only.  these objects shouldn't ever need to be instantiated directly, should they?
     */
    public FileDataWrapper() {}

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public OID getFileUploadProcessOid() {
        return fileUploadProcessOid;
    }

    public void setFileUploadProcessOid(OID fileUploadProcessOid) {
        this.fileUploadProcessOid = fileUploadProcessOid;
    }

    public FileUsageType getFileUsageType() {
        return fileUsageType;
    }

    public void setFileUsageType(FileUsageType fileUsageType) {
        this.fileUsageType = fileUsageType;
    }

    public FileOnDisk getFileOnDisk() {
        return fileOnDisk;
    }

    public void setFileOnDisk(FileOnDisk fileOnDisk) {
        this.fileOnDisk = fileOnDisk;
    }

    @SubPropertySettable
    @AfterPrepare
    public FileData getFileData() {
        // since this is set AfterPrepare and fileOnDisk is not, we know
        // that by now, the fileOnDisk will have been set if it existed
        // in the form.  if the fileOnDisk exists, then this FileData
        // is an ExistingFileData.  if it does not exist, then this must
        // be an UploadedFileData.
        if (!isFileDataSet) {
            // selected an existing file?
            if (exists(fileOnDisk)) {
                // bl: since we are no longer supporting existing file selection, commenting this out.  thus, we will
                // only support files that have already been put into the in-memory file map.  thus, there are no
                // security implications here since the user must have already been authenticated in order for the file
                // to get into the map in the first place.
                //fileData = ExistingFileData.getExistingFileData(fileUploadProcessOid, fileOnDisk, fileUsageType, true);
                Map<OID, FileData> fileMap = FileUploadUtils.getUniqueOidToFileData(fileUploadProcessOid, fileUsageType, false);
                if (fileMap != null) {
                    fileData = fileMap.get(fileOnDisk.getOid());
                }
            } else if (!isEmpty(fileId)) {
                UploadedFileData ufd = FileUploadUtils.getUploadedFileData(fileId, true);
                if (ufd != null && ufd.isProperType() && ufd.isValid()) {
                    fileData = ufd;
                }
            }
            isFileDataSet = true;
        }
        return fileData;
    }

    public boolean isAutoInsertIntoPostBody() {
        return autoInsertIntoPostBody;
    }

    public void setAutoInsertIntoPostBody(boolean autoInsertIntoPostBody) {
        this.autoInsertIntoPostBody = autoInsertIntoPostBody;
    }
}
