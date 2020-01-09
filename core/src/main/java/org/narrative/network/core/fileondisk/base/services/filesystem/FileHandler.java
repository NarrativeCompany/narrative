package org.narrative.network.core.fileondisk.base.services.filesystem;

import java.io.File;

/**
 * Date: 9/6/18
 * Time: 10:25 PM
 *
 * @author brian
 */
public interface FileHandler {
    String getFileUri(NetworkPath path);
    void putFile(NetworkPath path, File f, String contentType);
    void removeFile(NetworkPath path);
}
