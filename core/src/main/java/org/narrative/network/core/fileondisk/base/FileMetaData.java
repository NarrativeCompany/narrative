package org.narrative.network.core.fileondisk.base;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * Date: Jul 28, 2006
 * Time: 1:58:45 PM
 *
 * @author Brian
 */
public interface FileMetaData extends Serializable {
    public void setMetaData(byte[] data);

    public byte[] serialize();

    @JsonIgnore
    public FileType getFileType();
}
