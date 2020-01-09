package org.narrative.network.core.fileondisk.base;

import org.narrative.common.persistence.DAO;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.network.core.fileondisk.base.services.filesystem.NetworkPath;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.io.File;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Jul 28, 2006
 * Time: 8:52:26 AM
 *
 * @author Brian
 */
@MappedSuperclass
public abstract class FileBase<T extends FileMetaData, D extends DAO> implements FileMetaDataProvider<T>, DAOObject<D> {
    private OID oid;
    private String filename;
    private String mimeType;
    private int byteSize;

    protected T metaData;

    public static final String FIELD__FILE_TYPE__NAME = "fileType";
    public static final String FIELD__FILE_TYPE__COLUMN = FIELD__FILE_TYPE__NAME;

    public static final String FIELD__FILENAME__NAME = "filename";
    public static final String FIELD__BYTE_SIZE__NAME = "byteSize";

    /**
     * @deprecated for hibernate use only
     */
    public FileBase() {}

    private FileBase(String filename, String mimeType, int byteSize, T fileMetaData) {
        this.filename = filename;
        this.mimeType = mimeType;
        this.byteSize = byteSize;
        this.metaData = fileMetaData;
    }

    public FileBase(FileMetaDataProvider<T> fileData) {
        this(fileData.getFilename(), fileData.getMimeType(), fileData.getByteSize(), fileData.getFileMetaData());
    }

    @Transient
    public void updateFileData(FileMetaDataProvider<T> fileData) {
        this.filename = fileData.getFilename();
        this.mimeType = fileData.getMimeType();
        this.byteSize = fileData.getByteSize();
        this.metaData = fileData.getFileMetaData();
    }

    @Transient
    public OID getOid() {
        return oid;
    }

    @Transient
    public void setOid(OID oid) {
        this.oid = oid;
    }

    @Transient
    public abstract FileType getFileType();

    @Transient
    public abstract AuthZone getAuthZone();

    @Transient
    public abstract NetworkPath getNetworkPath();

    @Transient
    public File getFile() {
        throw UnexpectedError.getRuntimeException("Not supported!");
    }

    public static final int MIN_FILENAME_LENGTH = 1;
    public static final int MAX_FILENAME_LENGTH = 255;

    @NotNull
    @Length(min = MIN_FILENAME_LENGTH, max = MAX_FILENAME_LENGTH)
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public static String getFilenameAsMp4(String filename) {
        return getFilenameAsExt(filename, "mp4");
    }

    public static String getFilenameAsJpg(String filename) {
        return getFilenameAsExt(filename, "jpg");
    }

    public static String getFilenameAsExt(String filename, String ext) {
        assert !isEmpty(ext) : "Must have extension";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot != -1) {
            filename = filename.substring(0, lastDot);
        }
        return filename + "." + ext;
    }

    /**
     * get a filename for display in html.  we store the filename in its original form representing
     * the file stored on disk.  note that the filename can contain
     *
     * @param filename the filename to modify for display in raw html output
     * @return the filename for display in html
     */
    @Transient
    public static String getFilenameForDisplayInHtml(String filename) {
        return HtmlTextMassager.disableHtml(filename);
    }

    public static final int MIN_MIME_TYPE_LENGTH = 1;
    public static final int MAX_MIME_TYPE_LENGTH = 80;

    @NotNull
    @Length(min = MIN_MIME_TYPE_LENGTH, max = MAX_MIME_TYPE_LENGTH)
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @NotNull
    @Range(min = 0)
    public int getByteSize() {
        return byteSize;
    }

    public void setByteSize(int byteSize) {
        this.byteSize = byteSize;
    }

    /**
     * hibernate doesn't like these being parameterized, so deprecating them and having
     * separate parameterized getters and setters
     *
     * @deprecated use getFileMetaData() instead
     */
    @Basic(fetch = FetchType.EAGER, optional = true)
    @Lob
    @Type(type = FileMetaDataType.TYPE)
    public FileMetaData getMetaData() {
        return metaData;
    }

    /**
     * hibernate doesn't like these being parameterized, so deprecating them and having
     * separate parameterized getters and setters
     *
     * @deprecated use setFileMetaData() instead
     */
    public void setMetaData(FileMetaData metaData) {
        this.metaData = (T) metaData;
    }

    @Transient
    public T getFileMetaData() {
        return (T) getMetaData();
    }

    public void setFileMetaData(T fileMetaData) {
        setMetaData(fileMetaData);
    }

    @Transient
    public abstract NetworkDAOImpl getDAO();
}
