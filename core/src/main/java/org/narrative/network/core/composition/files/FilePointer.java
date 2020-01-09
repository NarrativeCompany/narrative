package org.narrative.network.core.composition.files;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.persistence.hibernate.StringEnumType;
import org.narrative.common.util.OrderedObject;
import org.narrative.common.util.posting.FullTextProvider;
import org.narrative.network.core.composition.files.dao.FilePointerDAO;
import org.narrative.network.core.fileondisk.base.FileBase;
import org.narrative.network.core.fileondisk.base.FileBaseProvider;
import org.narrative.network.core.fileondisk.base.FileMetaData;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileOnDiskStatus;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.fileondisk.base.services.filesystem.NetworkPath;
import org.narrative.network.core.user.AuthZone;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 5:20:30 PM
 * <p>
 * NOTE: All subclasses must have a constructor of the form:
 * (FilePointerSet filePointerSet, FileOnDisk fileOnDisk)
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = FilePointer.FIELD__FILE_TYPE__COLUMN)
@DiscriminatorValue(FileType.REGULAR_TYPE_STRING)
public class FilePointer<T extends FileMetaData> extends FileBase<T, FilePointerDAO> implements FullTextProvider, FileBaseProvider, OrderedObject {
    private OID fileOnDiskOid;
    private FilePointerSet filePointerSet;
    private int threadingOrder;
    private FileOnDiskStatus status;

    public static final String FIELD__FILE_POINTER_SET__NAME = "filePointerSet";
    public static final String FIELD__THREADING_ORDER__NAME = "threadingOrder";

    /**
     * @deprecated for hibernate use only
     */
    public FilePointer() {}

    public FilePointer(FilePointerSet filePointerSet, FileOnDisk fileOnDisk) {
        super(fileOnDisk);
        this.fileOnDiskOid = fileOnDisk.getOid();
        this.filePointerSet = filePointerSet;
        // maxThreadingOrder returns -1 when empty, so +1 here will always work.  it means we'll start at 0 for new/empty file pointer sets.
        this.threadingOrder = filePointerSet.getMaxThreadingOrder() + 1;
        this.status = fileOnDisk.getStatus();
    }

    public void updateFileData(FileOnDisk fileOnDisk) {
        super.updateFileData(fileOnDisk);
        this.status = fileOnDisk.getStatus();
    }

    @Override
    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return super.getOid();
    }

    @Override
    public void setOid(OID oid) {
        super.setOid(oid);
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_filePointer_filePointerSet")
    public FilePointerSet getFilePointerSet() {
        return filePointerSet;
    }

    public void setFilePointerSet(FilePointerSet filePointerSet) {
        this.filePointerSet = filePointerSet;
    }

    @Transient
    public String getTitle() {
        return getFileOnDisk().getTitle();
    }

    @Transient
    public String getTitleResolved() {
        return getFileOnDisk().getTitleResolved();
    }

    @Transient
    public String getDescriptionResolved() {
        return getFileOnDisk().getDescription();
    }

    @NotNull
    public int getThreadingOrder() {
        return threadingOrder;
    }

    public void setThreadingOrder(int threadingOrder) {
        this.threadingOrder = threadingOrder;
    }

    @NotNull
    @Column(insertable = false, updatable = false)
    @Type(type = StringEnumType.TYPE)
    public FileType getFileType() {
        return FileType.REGULAR;
    }

    public void setFileType(FileType fileType) {

    }

    @Override
    @Transient
    public FileUsageType getFileUsageType() {
        return getFileOnDisk().getFileUsageType();
    }

    @NotNull
    @Index(name = "filePointer_fileOnDiskOid_idx")
    public OID getFileOnDiskOid() {
        return fileOnDiskOid;
    }

    public void setFileOnDiskOid(OID fileOnDiskOid) {
        this.fileOnDiskOid = fileOnDiskOid;
    }

    @Transient
    public FileOnDisk getFileOnDisk() {
        return FileOnDisk.dao().get(getFileOnDiskOid());
    }

    @Override
    @Transient
    public FileBase getFileBase() {
        return getFileOnDisk();
    }

    @Type(type = IntegerEnumType.TYPE)
    @Column(columnDefinition = "tinyint", nullable = false)
    public FileOnDiskStatus getStatus() {
        return status;
    }

    public void setStatus(FileOnDiskStatus status) {
        this.status = status;
    }

    @Transient
    public void updateByteSize(int byteSize) {
        int byteSizeDiff = byteSize - getByteSize();
        setByteSize(byteSize);
        FilePointerSet filePointerSet = getFilePointerSet();
        filePointerSet.setTotalByteSize(filePointerSet.getTotalByteSize() + byteSizeDiff);
    }

    @Transient
    @org.jetbrains.annotations.NotNull
    @Override
    public String getFullText(boolean includeFileContents) {
        FileOnDisk fileOnDisk = getFileOnDisk();
        if (exists(fileOnDisk)) {
            return getFileOnDisk().getFullText(includeFileContents);
        }
        return "";
    }

    public static <T extends FilePointer> void sortFilePointersByThreadingOrder(List<T> filePointers) {
        OrderedObject.sort(filePointers);
    }

    @Override
    @Transient
    public AuthZone getAuthZone() {
        return getFileOnDisk().getAuthZone();
    }

    @Override
    @Transient
    public NetworkPath getNetworkPath() {
        return getFileOnDisk().getNetworkPath();
    }

    @Override
    @Transient
    public FilePointerDAO getDAO() {
        return dao();
    }

    public static FilePointerDAO dao() {
        return (FilePointerDAO) DAOImpl.getDAO(FilePointer.class);
    }
}
