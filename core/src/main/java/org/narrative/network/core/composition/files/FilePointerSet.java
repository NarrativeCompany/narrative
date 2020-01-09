package org.narrative.network.core.composition.files;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.posting.FullTextProvider;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.composition.files.dao.FilePointerSetDAO;
import org.narrative.network.core.fileondisk.base.FileBaseProviderCollectionContainer;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.validator.constraints.Range;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 5:20:37 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class FilePointerSet<T extends FilePointer> implements DAOObject<FilePointerSetDAO>, FullTextProvider, FileBaseProviderCollectionContainer<FilePointer> {
    private OID oid;
    private int fileCount;
    private int totalByteSize;

    private Composition composition;
    private Set<Reply> replies;

    private List<T> filePointers;

    public static final String FIELD__COMPOSITION__NAME = "composition";
    public static final String FIELD__COMPOSITION__COLUMN = FIELD__COMPOSITION__NAME + "_" + FIELD__OID__NAME;

    /**
     * @deprecated for hibernate use only
     */
    public FilePointerSet() {
    }

    public FilePointerSet(Composition composition) {
        this.composition = composition;
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @NotNull
    @Range(min = 0)
    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    @NotNull
    @Range(min = 0)
    public int getTotalByteSize() {
        return totalByteSize;
    }

    public void setTotalByteSize(int totalByteSize) {
        this.totalByteSize = totalByteSize;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    //@Index(name = "FKCADC68753393333F")
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    public Composition getComposition() {
        return composition;
    }

    public void setComposition(Composition composition) {
        this.composition = composition;
    }

    @Transient
    public OID getCompositionOid() {
        return getComposition().getOid();
    }

    @Transient
    public boolean isCompositionPrimaryFilePointerSet() {
        return isEqual(this, getComposition().getFilePointerSet());
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = Reply.FIELD__FILE_POINTER_SET__NAME)
    public Set<Reply> getReplies() {
        return replies;
    }

    public void setReplies(Set<Reply> replies) {
        this.replies = replies;
    }

    @Transient
    public Reply getReply() {
        assert !isCompositionPrimaryFilePointerSet() : "Should only attempt to get the Reply for a FilePointerSet if it is NOT the composition's primary file pointer set!  Likely a logic bug somewhere.";
        return Reply.dao().getForFilePointerSet(this);
    }

    /**
     * hibernate doesn't like these being parameterized, so deprecating them and having
     * separate parameterized getters and setters
     *
     * @deprecated use getFilePointerList() instead
     * This will return an unordered list.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = FilePointer.FIELD__FILE_POINTER_SET__NAME, cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public List<FilePointer> getFilePointers() {
        return (List<FilePointer>) filePointers;
    }

    /**
     * hibernate doesn't like these being parameterized, so deprecating them and having
     * separate parameterized getters and setters
     *
     * @deprecated use setFilePointerList() instead
     */
    public void setFilePointers(List<FilePointer> filePointers) {
        this.filePointers = (List<T>) filePointers;
    }

    @Transient
    public List<T> getFilePointerList() {
        List<T> filePointers = (List<T>) getFilePointers();
        if (filePointers == null) {
            return null;
        }
        Collections.sort(filePointers, new Comparator<T>() {
            public int compare(T o1, T o2) {
                int ret = Integer.valueOf(o1.getThreadingOrder()).compareTo(o2.getThreadingOrder());
                if (ret != 0) {
                    return ret;
                }
                return OID.compareOids(o1.getOid(), o2.getOid());
            }
        });
        return filePointers;
    }

    @Override
    @Transient
    public Collection<FilePointer> getFileBaseProviderCollection() {
        return (Collection<FilePointer>) getFilePointerList();
    }

    @Transient
    public List<T> getAudioFilePointers() {
        return getFilePointersOfType(FileType.AUDIO);
    }

    @Transient
    public List<T> getRegularFilePointers() {
        return getFilePointersOfType(FileType.REGULAR);
    }

    @Transient
    private List<T> getFilePointersOfType(FileType fileType) {
        List<T> filePointers = getFilePointerList();
        List<T> ret = new LinkedList<T>();
        for (T filePointer : filePointers) {
            if (filePointer.getFileType().equals(fileType)) {
                ret.add(filePointer);
            }
        }
        return ret;
    }

    @Transient
    public Map<FileType, Integer> getFileTypeToFilePointerCount() {
        List<T> filePointers = getFilePointerList();
        Map<FileType, Integer> ret = new HashMap<FileType, Integer>();
        for (FileType fileType : FileType.values()) {
            int count = 0;
            for (T filePointer : filePointers) {
                if (filePointer.getFileType().equals(fileType)) {
                    count++;
                }
            }
            ret.put(fileType, count);
        }
        return ret;
    }

    @Transient
    public Map<FileType, List<T>> getFileTypeToFilePointers() {
        Map<FileType, List<T>> ret = newHashMap();
        for (FileType fileType : FileType.values()) {
            ret.put(fileType, getFilePointersOfType(fileType));
        }
        return ret;
    }

    public void setFilePointerList(List<T> filePointers) {
        this.filePointers = filePointers;
    }

    @Transient
    public FilePointer getFirstFilePointerByType(FileType fileType) {
        List<T> filePointers = getFilePointerList();
        for (T filePointer : filePointers) {
            if (filePointer.getFileType() == fileType) {
                return filePointer;
            }
        }
        return null;
    }

    @Transient
    public FilePointer getFilePointerByFilename(String filename) {
        List<T> filePointers = getFilePointerList();
        for (T filePointer : filePointers) {
            if (IPStringUtil.isStringEqualIgnoreCase(filePointer.getFilename(), filename)) {
                return filePointer;
            }
        }
        return null;
    }

    @Transient
    @org.jetbrains.annotations.NotNull
    @Override
    public String getFullText(boolean includeFileContents) {
        StringBuilder fullText = new StringBuilder();
        List<T> filePointers = getFilePointerList();
        if (filePointers == null) {
            return fullText.toString();
        }
        for (T filePointer : filePointers) {
            fullText.append(filePointer.getFullText(includeFileContents));
            fullText.append(" ");
        }
        return fullText.toString();
    }

    @Transient
    @org.jetbrains.annotations.NotNull
    public final FilePointer addFilePointer(FileOnDisk fileOnDisk) {
        T ret;

        try {
            ret = (T) fileOnDisk.getFileType().getFilePointerClass().getConstructor(FilePointerSet.class, FileOnDisk.class).newInstance(this, fileOnDisk);
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("Failed instantiating object of type: " + fileOnDisk.getFileType().getFilePointerClass() + " - the class must have a constructor that takes a FilePointerSet and a FileOnDisk!", t, true);
        }
        List<T> filePointers = getFilePointerList();
        if (filePointers == null) {
            setFilePointerList(filePointers = new LinkedList<T>());
        }
        filePointers.add(ret);

        // update stats
        this.setTotalByteSize(this.getTotalByteSize() + ret.getByteSize());
        this.setFileCount(this.getFileCount() + 1);

        return ret;
    }

    @Transient
    public void removeFilePointerFromSetAndDeleteSetIfPossible(T filePointer) {
        assert CoreUtils.isEqual(filePointer.getFilePointerSet(), this) : "FilePointer to delete is not part of this FilePointerSet";

        if (getFilePointerList().size() > 1) {
            removeFilePointer(filePointer);
        } else {
            deleteFilePointerSet();
        }
    }

    @Transient
    public void deleteFilePointerSet() {
        if (isCompositionPrimaryFilePointerSet()) {
            Composition composition = getComposition();
            composition.setFilePointerSet(null);
        } else {
            Reply reply = getReply();
            reply.setFilePointerSet(null);
        }
        FilePointerSet.dao().delete(this);
    }

    /**
     * remove a file pointer from this file pointer set
     *
     * @param filePointer the file pointer to remove from this set
     * @return true if the file pointer was removed.  false if it was not (no work to be done).
     */
    @Transient
    public final boolean removeFilePointer(T filePointer) {
        if (filePointer == null) {
            return false;
        }

        assert CoreUtils.isEqual(filePointer.getFilePointerSet(), this) : "FilePointer to delete is not part of this FilePointerSet";
        List<T> filePointers = getFilePointerList();
        assert filePointers.size() > 1 : "Trying to delete last FilePointer from set, you must delete via owning content.";

        if (!filePointers.contains(filePointer)) {
            return false;
        }

        this.setTotalByteSize(Math.max(0, this.getTotalByteSize() - filePointer.getByteSize()));
        this.setFileCount(Math.max(0, this.getFileCount() - 1));
        filePointers.remove(filePointer);
        filePointer.setFilePointerSet(null);
        FilePointer.dao().delete(filePointer);

        return true;
    }

    @Transient
    public int getMaxThreadingOrder() {
        List<T> filePointers = getFilePointerList();
        if (filePointers == null || filePointers.isEmpty()) {
            return -1;
        }
        FilePointer.sortFilePointersByThreadingOrder(filePointers);
        return filePointers.get(filePointers.size() - 1).getThreadingOrder();
    }

    public void optimizeThreadingOrder() {
        List<T> filePointers = getFilePointerList();
        if (filePointers == null || filePointers.isEmpty()) {
            return;
        }
        FilePointer.sortFilePointersByThreadingOrder(filePointers);
        int i = 0;
        for (T filePointer : filePointers) {
            filePointer.setThreadingOrder(i);
            i++;
        }
    }

    @Transient
    public FilePointer getFilePointerByFileOnDiskOid(OID fileOnDiskOid) {
        List<T> filePointers = getFilePointerList();
        if (filePointers == null) {
            return null;
        }
        for (T filePointer : filePointers) {
            if (IPUtil.isEqual(filePointer.getFileOnDiskOid(), fileOnDiskOid)) {
                return filePointer;
            }
        }
        return null;
    }

    public static FilePointerSetDAO dao() {
        return (FilePointerSetDAO) DAOImpl.getDAO(FilePointerSet.class);
    }
}
