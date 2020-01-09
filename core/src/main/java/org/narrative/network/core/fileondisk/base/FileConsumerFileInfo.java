package org.narrative.network.core.fileondisk.base;

import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.shared.security.AccessViolation;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Apr 16, 2007
 * Time: 10:14:57 AM
 * This class encapulates the return information required to retreive a file.
 */
public class FileConsumerFileInfo {
    private final FileOnDisk fileOnDisk;
    private final FilePointer filePointer;
    private final AccessViolation accessViolation;

    /**
     * Use this constructor if you want to specify th fileOnDisk to use, and also want to increment a file pointer's
     * download count.
     */
    public FileConsumerFileInfo(FilePointer filePointer) {
        this.fileOnDisk = filePointer.getFileOnDisk();
        this.filePointer = filePointer;
        this.accessViolation = null;
    }

    /**
     * Use this constructor if you want to specify th fileOnDisk to use, but do *not* want to increment a file pointer's
     * download count.
     */
    public FileConsumerFileInfo(FileOnDisk fileOnDisk) {
        this.fileOnDisk = fileOnDisk;
        this.filePointer = null;
        this.accessViolation = null;
    }

    /**
     * Use this constructor if the role was not able to view the file, and it cause an access violation.  Note that
     * you still need to return the proper fileOnDisk that they were trying to access.
     */
    public FileConsumerFileInfo(AccessViolation accessViolation, FileOnDisk fileOnDisk) {
        this.accessViolation = accessViolation;
        this.fileOnDisk = fileOnDisk;
        this.filePointer = null;
    }

    public FileConsumerFileInfo(AccessViolation accessViolation, FilePointer filePointer) {
        this.accessViolation = accessViolation;
        this.filePointer = filePointer;
        this.fileOnDisk = filePointer.getFileOnDisk();
    }

    public FileOnDisk getFileOnDisk() {
        return fileOnDisk;
    }

    public FilePointer getFilePointer() {
        return filePointer;
    }

    public AccessViolation getAccessViolation() {
        return accessViolation;
    }
}

