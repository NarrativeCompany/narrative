package org.narrative.network.core.content.base;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.fileondisk.base.FileBase;
import org.narrative.network.core.fileondisk.base.FileBaseProvider;
import org.narrative.network.core.fileondisk.base.FileMetaData;
import org.narrative.network.core.fileondisk.base.FileMetaDataProvider;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.shared.daobase.NetworkDAOImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Mar 24, 2006
 * Time: 8:55:20 AM
 *
 * @author Brian
 * <p>
 * NOTE: all subclasses must have a constructor that takes a FileOnDisk argument (typed based on FileType) and a FileUsageType argument
 */
public class ExistingFileData<T extends FileMetaData> implements FileData<T>, FileMetaDataProvider<T> {
    private final String filename;
    private final String mimeType;
    private final int byteSize;
    private boolean isNew = true;
    private Boolean charsChanged;
    private String originalTitle;
    private String title;
    private String originalDescription;
    private String description;
    private int order;
    private boolean include;
    protected T fileMetaData;
    private final OID fileUploadProcessOid;
    private FileUsageType fileUsageType;
    private final OID fileOnDiskOid;
    private final OID fileBaseOid;
    private final NetworkDAOImpl fileBaseDAO;
    private OID filePointerOid;

    public ExistingFileData(OID fileUploadProcessOid, FileBase fileBase, FileUsageType fileUsageType) {
        filename = fileBase.getFilename();
        mimeType = fileBase.getMimeType();
        byteSize = fileBase.getByteSize();
        this.fileUploadProcessOid = fileUploadProcessOid;
        this.fileUsageType = fileUsageType;
        fileOnDiskOid = fileBase.getFileOnDiskOid();
        fileBaseOid = fileBase.getOid();
        fileBaseDAO = fileBase.getDAO();
        if (fileBase instanceof FileOnDisk) {
            FileOnDisk fileOnDisk = (FileOnDisk) fileBase;
            setOriginalTitle(fileOnDisk.getTitle());
            setOriginalDescription(fileOnDisk.getDescription());
        }
        include = true;
        fileMetaData = (T) fileBase.getFileMetaData();
    }

    public static ExistingFileData getExistingFileData(OID fileUploadProcessOid, FileBase fileBase, FileUsageType fileUsageType, boolean isNew) {
        ExistingFileData ret;
        try {
            ret = fileBase.getFileType().getExistingFileDataClass().getConstructor(OID.class, FileBase.class, FileUsageType.class).newInstance(fileUploadProcessOid, fileBase, fileUsageType);
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("Failed getting an instance of ExistingFileData for the specified FileBase!  ExistingFileData subclasses must have a constructor with three arguments: fileUploadProcessOid, FileBase, and FileUsageType! class/" + fileBase.getFileType().getExistingFileDataClass(), t, true);
        }
        ret.isNew = isNew;
        return ret;
    }

    public static ExistingFileData getExistingFileData(OID fileUploadProcessOid, FileBaseProvider fileBaseProvider, FileUsageType fileUsageType) {
        // if we have a FilePointer, then that means this isn't a new file
        ExistingFileData ret = getExistingFileData(fileUploadProcessOid, fileBaseProvider.getFileBase(), fileUsageType, false);

        // update the fields from the FilePointer accordingly
        if (fileBaseProvider instanceof FilePointer) {
            FilePointer filePointer = (FilePointer) fileBaseProvider;

            ret.setOrder(filePointer.getThreadingOrder());
            ret.setOriginalTitle(filePointer.getTitleResolved());
            ret.setOriginalDescription(filePointer.getDescriptionResolved());
            ret.filePointerOid = filePointer.getOid();

        }

        return ret;
    }

    @Override
    public void scrub() {
        title = HtmlTextMassager.sanitizePlainTextString(title, false);
        description = HtmlTextMassager.sanitizePlainTextString(description, false);
    }

    public boolean isNew() {
        return isNew;
    }

    public boolean isCharsChanged() {
        if (charsChanged == null) {
            charsChanged = !isEqual(originalTitle, title) || !isEqual(originalDescription, description);
        }
        return charsChanged;
    }

    public String getFilename() {
        return filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getByteSize() {
        return byteSize;
    }

    public String getTitle() {
        return title;
    }

    private void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
        setTitle(originalTitle);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private void setOriginalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
        setDescription(originalDescription);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int threadingOrder) {
        this.order = threadingOrder;
    }

    public boolean isInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }

    public FileType getFileType() {
        return FileType.REGULAR;
    }

    public T getFileMetaData() {
        return fileMetaData;
    }

    @Override
    public FileUsageType getFileUsageType() {
        return fileUsageType;
    }

    @Override
    public void setFileUsageType(FileUsageType fileUsageType) {
        this.fileUsageType = fileUsageType;
    }

    public FilePointer getFilePointer() {
        return FilePointer.dao().get(filePointerOid);
    }

    public void doSetFilePointer(FilePointer filePointer) {
        this.filePointerOid = filePointer.getOid();
    }

    public OID getFileOnDiskOid() {
        return fileOnDiskOid;
    }

    public FileOnDisk getFileOnDisk() {
        return FileOnDisk.dao().get(fileOnDiskOid);
    }

    public OID getUniqueOid() {
        return fileBaseOid;
    }

    public <T extends FileBase> T getFileBase() {
        return (T) fileBaseDAO.get(fileBaseOid);
    }

    public boolean isExistingFile() {
        return true;
    }

    public OID getFileUploadProcessOid() {
        return fileUploadProcessOid;
    }

    public void deleteAllTempFiles() {
    }

}
