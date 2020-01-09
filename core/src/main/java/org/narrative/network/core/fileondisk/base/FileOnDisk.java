package org.narrative.network.core.fileondisk.base;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.persistence.hibernate.StringEnumType;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.posting.FullTextProvider;
import org.narrative.network.core.content.base.UploadedFileData;
import org.narrative.network.core.fileondisk.base.dao.FileOnDiskDAO;
import org.narrative.network.core.fileondisk.base.services.filesystem.NetworkPath;
import org.narrative.network.core.fileondisk.image.ImageType;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.AuthZoneDataType;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.security.PrimaryRole;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Table;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.io.File;
import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:37:57 PM
 * <p>
 * NOTE: All FileOnDisk descendents must have a constructor that takes an UploadedFileData object of the proper type
 * based on the specification in FileType!
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = FileOnDisk.FIELD__FILE_TYPE__COLUMN)
@DiscriminatorValue(FileType.REGULAR_TYPE_STRING)
@Table(appliesTo = "FileOnDisk", indexes = {@Index(name = "fileOnDisk_authZone_fileUsageType_idx", columnNames = {FileOnDisk.FIELD__AUTH_ZONE__COLUMN, FileOnDisk.FIELD__FILE_USAGE_TYPE__COLUMN})})
public class FileOnDisk<T extends FileMetaData> extends FileBase<T, FileOnDiskDAO> implements FileConsumer, FullTextProvider {
    public static final String TEMP_FILE_TOKEN_PRIVATE_KEY = FileOnDisk.class.getName() + "-TempFile-special-magical-token-private-key-BqUTVKPUxmZGY,gMqoF2kMvjVAxqvVNHLx3Vmy7WsBonweco3M]N@tGicMVjoNta";

    private User user;
    private Timestamp creationDatetime;
    protected transient File tempFile;
    private FileOnDiskStatus status;

    private AuthZone authZone;
    private FileUsageType fileUsageType;

    // bl: in the UI, we may use title & description as artist & title for audio files.
    private String title;
    private String description;

    private FileOnDiskStats fileOnDiskStats;

    public static final String FIELD__AUTH_ZONE__NAME = "authZone";
    public static final String FIELD__FILE_USAGE_TYPE__NAME = "fileUsageType";

    public static final String FIELD__AUTH_ZONE__COLUMN = FIELD__AUTH_ZONE__NAME;
    public static final String FIELD__FILE_USAGE_TYPE__COLUMN = FIELD__FILE_USAGE_TYPE__NAME;

    /**
     * @deprecated for hibernate use only
     */
    public FileOnDisk() {}

    public FileOnDisk(UploadedFileData<T> fileData, User user) {
        this(fileData, fileData.getFileUsageType(), user, new Timestamp(System.currentTimeMillis()), FileOnDiskStatus.ACTIVE, fileData.getTempFile());
    }

    public FileOnDisk(FileOnDisk<T> copy, File tempFile) {
        this(copy, copy.getFileUsageType(), copy.getUser(), copy.getCreationDatetime(), copy.getStatus(), tempFile);
        setTitle(copy.getTitle());
        setDescription(copy.getDescription());
    }

    private FileOnDisk(FileMetaDataProvider<T> fileMetaDataProvider, FileUsageType fileUsageType, User user, Timestamp creationDatetime, FileOnDiskStatus status, File tempFile) {
        super(fileMetaDataProvider);
        this.user = user;
        this.authZone = exists(user) ? user.getAuthZone() : null;
        this.fileUsageType = fileUsageType;
        this.creationDatetime = creationDatetime;
        this.status = status;
        this.tempFile = tempFile;

        this.fileOnDiskStats = new FileOnDiskStats(this);
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
    @ForeignKey(name = "FKDAF838D87EB27D35")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void updateUser(User user) {
        // Unlike construction, the user should never be null here!
        setUser(user);
        setAuthZone(user.getAuthZone());
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @JoinColumn(name = FileOnDisk.FIELD__OID__NAME)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    public FileOnDiskStats getFileOnDiskStats() {
        return fileOnDiskStats;
    }

    public void setFileOnDiskStats(FileOnDiskStats fileOnDiskStats) {
        this.fileOnDiskStats = fileOnDiskStats;
    }

    @NotNull
    @Column(insertable = false, updatable = false)
    @Type(type = StringEnumType.TYPE)
    public FileType getFileType() {
        return FileType.REGULAR;
    }

    public void setFileType(FileType fileType) {

    }

    @NotNull
    //@Temporal(TemporalType.TIMESTAMP)
    public Timestamp getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Timestamp creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    @Transient
    public File getTempFile() {
        return tempFile;
    }

    // jw: this is needed for Site Syncing
    public void setTempFile(File tempFile) {
        this.tempFile = tempFile;
    }

    @Override
    @Transient
    public NetworkPath getNetworkPath() {
        return getNetworkPathForOid(this.getOid());
    }

    private static NetworkPath getNetworkPathForOid(OID fileOnDiskOid) {
        return new NetworkPath(FileBaseType.FILE_ON_DISK_FILE, fileOnDiskOid);
    }

    @Transient
    @org.jetbrains.annotations.NotNull
    public static FileOnDisk<? extends FileMetaData> getNewFileOnDisk(UploadedFileData fileData, User user) {
        try {
            return (FileOnDisk<? extends FileMetaData>) fileData.getFileType().getFileOnDiskClass().getConstructor(fileData.getFileType().getUploadedFileDataClass(), User.class).newInstance(fileData, user);
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("Failed getting constructor for FileOnDisk class.  Must take an UploadedFileData object!  Class: " + fileData.getFileType().getFileOnDiskClass(), t, true);
        }
    }

    @Override
    @Transient
    public FileOnDiskDAO getDAO() {
        return dao();
    }

    @Type(type = IntegerEnumType.TYPE)
    @Column(columnDefinition = "tinyint", nullable = false)
    public FileOnDiskStatus getStatus() {
        return status;
    }

    public void setStatus(FileOnDiskStatus status) {
        this.status = status;
    }

    @NotNull
    @Override
    @Type(type = AuthZoneDataType.TYPE)
    public AuthZone getAuthZone() {
        return authZone;
    }

    public void setAuthZone(AuthZone authZone) {
        this.authZone = authZone;
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    public FileUsageType getFileUsageType() {
        return fileUsageType;
    }

    public void setFileUsageType(FileUsageType fileUsageType) {
        this.fileUsageType = fileUsageType;
    }

    public static final int MIN_TITLE_LENGTH = 0;
    public static final int MAX_TITLE_LENGTH = 255;

    @Length(min = MIN_TITLE_LENGTH, max = MAX_TITLE_LENGTH)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Transient
    public String getTitleResolved() {
        // lets knock off the .* extension when guessing a title
        return isEmpty(title) ? (getFileType().isRegularFile() ? getFilename() : getDefaultTitleFromFilename(getFilename())) : title;
    }

    @Transient
    public void setTitleResolved(String title) {
        // bl: the title defaults to the filename.  since we have the whole "title resolved" notion now,
        // there is no need to set the title field if it is just set to the filename (or filename resolved).
        // bl: note that this should match getTitleResolved above or else you won't be able to remove
        // the file extension from regular files.
        if (isEqual(title, getFileType().isRegularFile() ? getFilename() : getDefaultTitleFromFilename(getFilename()))) {
            title = null;
        }
        setTitle(title);
    }

    public static String getDefaultTitleFromFilename(String filename) {
        return IPStringUtil.getStringBeforeLastIndexOf(filename, ".");
    }

    public static final int MIN_DESCRIPTION_LENGTH = 0;
    public static final int MAX_DESCRIPTION_LENGTH = 255;

    @Length(min = MIN_DESCRIPTION_LENGTH, max = MAX_DESCRIPTION_LENGTH)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Transient
    @org.jetbrains.annotations.NotNull
    @Override
    public String getFullText(boolean includeFileContents) {
        StringBuilder fullText = new StringBuilder();
        if (!IPStringUtil.isEmpty(getFilename())) {
            fullText.append(getFilename());
        }
        if (!IPStringUtil.isEmpty(title)) {
            fullText.append(" ");
            fullText.append(title);
        }
        if (!IPStringUtil.isEmpty(description)) {
            fullText.append(" ");
            fullText.append(description);
        }
        return fullText.toString();
    }

    @Transient
    public FileOnDisk getFileOnDisk() {
        return this;
    }

    @Transient
    public OID getFileOnDiskOid() {
        return getOid();
    }

    @Transient
    public FileConsumerType getFileConsumerType() {
        return FileConsumerType.FILE_ON_DISK;
    }

    @Transient
    public String getFileUrlBase() {
        return getAuthZone().getBaseUrl();
    }

    @Transient
    public FileOnDisk getFileMetaDataProvider(boolean isPrimaryPicture) {
        return this;
    }

    @Transient
    public FileConsumerFileInfo getFileInfo(PrimaryRole currentRole, OID filePointerOid, boolean primaryPicture) throws AccessViolation {
        // check security by making sure the requested file is "owned" by this user
        if (!exists(currentRole) || !currentRole.isRegisteredUser() || !isEqual(user, currentRole.getUser())) {
            return new FileConsumerFileInfo(new AccessViolation(), this);
        } else {
            return new FileConsumerFileInfo(this);
        }
    }

    @Transient
    public ImageType getPrimaryImageType() {
        throw UnexpectedError.getRuntimeException("This utility method is only here to expose some general behavior for ImageOnDisk and VideoOnDisk!");
    }

    public NetworkPath getNetworkPathForImageType(ImageType imageType) {
        throw UnexpectedError.getRuntimeException("This utility method is only here to expose some general behavior for ImageOnDisk and VideoOnDisk!");
    }

    @Transient
    public String getTempFileToken() {
        assert getStatus().isTempFile() : "Should only get tempFileToken for temp files! not/" + getStatus();
        return IPStringUtil.getMD5DigestFromObjects(getOid(), TEMP_FILE_TOKEN_PRIVATE_KEY);
    }

    public static FileOnDiskDAO dao() {
        return (FileOnDiskDAO) DAOImpl.getDAO(FileOnDisk.class);
    }
}
