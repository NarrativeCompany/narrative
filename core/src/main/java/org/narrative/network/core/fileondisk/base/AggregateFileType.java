package org.narrative.network.core.fileondisk.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Date: May 13, 2008
 * Time: 8:38:18 AM
 *
 * @author brian
 */
public class AggregateFileType {
    private final Set<FileType> fileTypes;
    public static final AggregateFileType EMPTY_AGGREGATE_FILE_TYPE = new AggregateFileType();

    private AggregateFileType() {
        this.fileTypes = Collections.unmodifiableSet(new HashSet<>());
    }

    public AggregateFileType(Set<FileType> fileTypes) {
        assert fileTypes != null && !fileTypes.isEmpty() : "Can't create an AggregateFileType with no FileTypes supplied for music product!";
        this.fileTypes = Collections.unmodifiableSet(fileTypes);
    }

    public Set<FileType> getFileTypes() {
        return fileTypes;
    }

    public boolean isSingleFileType() {
        return fileTypes.size() == 1;
    }

    public FileType getLoneFileType() {
        assert isSingleFileType() : "Can't attempt to get the lone FileType if multiple file types are supported or if no file types are supported!";
        return fileTypes.iterator().next();
    }

    public boolean isOnlyVideoFiles() {
        return isSingleFileType() && getLoneFileType().isVideoFile();
    }

}
