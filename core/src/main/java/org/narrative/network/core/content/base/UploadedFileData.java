package org.narrative.network.core.content.base;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.IPIOUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.fileondisk.base.AggregateFileType;
import org.narrative.network.core.fileondisk.base.FileBase;
import org.narrative.network.core.fileondisk.base.FileMetaData;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.shared.util.NetworkCoreUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Feb 2, 2006
 * Time: 11:43:20 AM
 */
public class UploadedFileData<T extends FileMetaData> implements FileData<T> {
    private File tempFile;
    private int byteSize;
    protected String mimeType;
    private final String filename;
    private String title;
    private String description;
    private int order;
    private boolean include;
    private T fileMetaData;
    private final OID fileUploadProcessOid;
    private FileUsageType fileUsageType;
    private OID fileOnDiskOid;
    protected final OID uniqueOid;
    protected String errorMessage;
    private FilePointer filePointer;
    private Set<File> oldTempFiles = new HashSet<File>();

    private boolean isValid;
    private boolean isProperType = true;
    private UploadedFileStatus status = UploadedFileStatus.PROCESSING;

    public UploadedFileData(OID fileUploadProcessOid, FileUsageType fileUsageType, File file, String mimeType, String filename) {
        this(fileUploadProcessOid, fileUsageType, file, mimeType, filename, null);
    }

    protected UploadedFileData(OID fileUploadProcessOid, FileUsageType fileUsageType, File file, String mimeType, String filename, OID uniqueOid) {
        setTempFile(file);
        this.mimeType = mimeType;
        Integer intVal;
        try {
            intVal = Integer.parseInt(filename);
        } catch (NumberFormatException nfe) {
            intVal = null;
        }
        if (intVal == null) {
            this.filename = filename;
        } else {
            this.filename = getFileType().name().toLowerCase() + intVal + "." + getFileType().getDefaultExtension();
        }
        this.fileUploadProcessOid = fileUploadProcessOid;
        this.fileUsageType = fileUsageType;
        this.include = true;
        this.title = getFileType().isRegularFile() ? filename : FileOnDisk.getDefaultTitleFromFilename(filename);
        // set the file meta data to null explicitly.  this will ensure isValid is set accordingly.
        setFileMetaData(null);
        this.uniqueOid = uniqueOid != null ? uniqueOid : OIDGenerator.getNextOID();
    }

    public static UploadedFileData getNewUploadedFileData(OID fileUploadProcessOid, FileUsageType fileUsageType, File file, String mimeType, String filename) {
        // bl: don't filter by allowed type here. instead, rely on the caller to do security to make sure that the
        // user can upload a file of this type. this way, if you can upload regular files, but not images, we won't
        // treat uploaded images as regular files (which was just weird).
        AggregateFileType aggregateFileType = fileUsageType.getAllowedUploadFileType();

        // jw: if we are only allowing a single explicit file type then just return the data for that.
        if (aggregateFileType.isSingleFileType() && !aggregateFileType.getLoneFileType().isRegularFile()) {
            return aggregateFileType.getLoneFileType().getNewInstance(fileUploadProcessOid, fileUsageType, file, mimeType, filename);
        }

        // jw: lets process all file types here and assume that the consuming code will handle if the type does not match.
        for (FileType fileType : FileType.ALL_FILE_TYPES_ORDERED) {
            UploadedFileData ret = fileType.getNewInstance(fileUploadProcessOid, fileUsageType, file, mimeType, filename);

            // jw: if the file data is valid, or the fileType is regular then return it.  Regular is always last so this
            //     should ensure that we are always providing file data from this method.
            if (ret.isValid() || fileType.isRegularFile()) {
                return ret;
            }
        }

        throw UnexpectedError.getRuntimeException("Should have returned REGULAR file data above!");
    }

    public final void postUploadProcess(OID fileUploadProcessOid) {
        if (status == null || status == UploadedFileStatus.PROCESSING) {
            try {
                postUploadSubProcess(fileUploadProcessOid);
                status = UploadedFileStatus.SUCCESS;
            } catch (Throwable t) {
                status = UploadedFileStatus.ERROR;
                throw UnexpectedError.getRuntimeException("Failed processing file/" + getTempFile().getAbsolutePath(), t);
            }
        }
    }

    public UploadedFileStatus getStatus() {
        return status;
    }

    protected void postUploadSubProcess(OID fileUploadProcessOid) {

        File outFile = NetworkCoreUtils.createTempFile(getTempFilenameForFileUploadProcessOid(fileUploadProcessOid), getTempFileExtension(), true);
        try {
            if (!IPIOUtil.doCopyFile(getTempFile(), outFile, false)) {
                throw UnexpectedError.getRuntimeException("Failed copying uploaded tempFile to temp directory! file/" + getTempFile().getAbsolutePath() + " outFile/" + outFile.getAbsolutePath(), true);
            }
        } catch (IOException ioe) {
            throw UnexpectedError.getRuntimeException("Failed copying uploaded tempFile to temp directory! file/" + getTempFile().getAbsolutePath() + " outFile/" + outFile.getAbsolutePath(), ioe, true);
        }
        setTempFile(outFile);

    }

    protected String getTempFileExtension() {
        return "tmp";
    }

    @Override
    public void scrub() {
        title = HtmlTextMassager.sanitizePlainTextString(title, false);
        description = HtmlTextMassager.sanitizePlainTextString(description, false);
    }

    public boolean isNew() {
        // uploaded files are always considered new
        return true;
    }

    public boolean isCharsChanged() {
        // uploaded files can't have any chars changed
        return false;
    }

    public boolean isValid() {
        return isValid;
    }

    protected void markInvalid() {
        this.isValid = false;
    }

    public final void deleteAllTempFiles() {
        deleteAllTempFilesSub();
        FileDataUtil.safeDeleteFile(tempFile);
        FileDataUtil.safeDeleteFileIterator(oldTempFiles.iterator());
    }

    protected void deleteAllTempFilesSub() {
    }

    public File getTempFile() {
        return tempFile;
    }

    protected void setTempFile(File tempFile) {
        if (this.tempFile != null && this.tempFile.exists()) {
            oldTempFiles.add(this.tempFile);
        }
        this.tempFile = tempFile;
        this.byteSize = (int) tempFile.length();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    protected void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getFilename() {
        return filename;
    }

    public String getFilenameAsJpg() {
        return FileBase.getFilenameAsJpg(filename);
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getByteSize() {
        return byteSize;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    protected void setFileMetaData(T fileMetaData) {
        this.fileMetaData = fileMetaData;
        Class<? extends FileMetaData> fileMetaDataClass = getFileType().getFileMetaDataClass();
        // if there isn't a specialized file meta data class, then assume the file is valid.
        // if there is a specialized file meta data class, then the file data is only valid
        // if the meta data was non-null.
        isValid = fileMetaDataClass == null || fileMetaData != null;
    }

    @Override
    public FileUsageType getFileUsageType() {
        return fileUsageType;
    }

    public void setFileUsageType(FileUsageType fileUsageType) {
        this.fileUsageType = fileUsageType;
    }

    public FilePointer getFilePointer() {
        return filePointer;
    }

    public void doSetFilePointer(FilePointer filePointer) {
        this.filePointer = filePointer;
    }

    public OID getFileOnDiskOid() {
        return fileOnDiskOid;
    }

    public void setFileOnDiskOid(OID fileOnDiskOid) {
        this.fileOnDiskOid = fileOnDiskOid;
    }

    public FileOnDisk getFileOnDisk() {
        return FileOnDisk.dao().get(fileOnDiskOid);
    }

    public OID getUniqueOid() {
        return uniqueOid;
    }

    public String getTempFilenameForFileUploadProcessOid(OID fileUploadProcessOid) {
        return getTempFilenameForFileUploadProcessOid(fileUploadProcessOid, uniqueOid);
    }

    public static String getTempFilenameForFileUploadProcessOid(OID fileUploadProcessOid, OID uniqueFileOid) {
        return newString(fileUploadProcessOid, "_", uniqueFileOid);
    }

    public ObjectPair<InputStream, Integer> getFileInputStreamAndByteSize() {
        try {
            return new ObjectPair<InputStream, Integer>(new FileInputStream(getTempFile()), (int) getTempFile().length());
        } catch (FileNotFoundException fnfe) {
            throw UnexpectedError.getRuntimeException("Failed lookup of uploaded file! file/" + getTempFile().getAbsolutePath(), fnfe, true);
        }
    }

    public boolean isProperType() {
        return isProperType;
    }

    public void setProperType(boolean properType) {
        isProperType = properType;
    }

    public boolean isExistingFile() {
        return false;
    }

    public final OID getFileUploadProcessOid() {
        return fileUploadProcessOid;
    }

    public boolean isTooBig() {
        return tempFile.length() > getFileUsageType().getMaxFileSize(getFileType());
    }

    public static enum UploadedFileStatus {
        PROCESSING,
        SUCCESS,
        ERROR;
    }
}
