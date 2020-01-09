package org.narrative.network.core.fileondisk.base;

import org.narrative.common.persistence.OID;

/**
 * Date: Jul 28, 2006
 * Time: 3:17:18 PM
 *
 * @author Brian
 */
public interface FileMetaDataProvider<T extends FileMetaData> {
    public String getFilename();

    public String getMimeType();

    public int getByteSize();

    public T getFileMetaData();

    public FileOnDisk getFileOnDisk();

    public OID getFileOnDiskOid();

    public FileType getFileType();

    public FileUsageType getFileUsageType();
}
