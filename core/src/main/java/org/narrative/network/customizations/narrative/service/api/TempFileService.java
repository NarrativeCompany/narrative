package org.narrative.network.customizations.narrative.service.api;

import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.customizations.narrative.service.api.model.TempFileDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Date: 2019-08-19
 * Time: 15:46
 *
 * @author brian
 */
public interface TempFileService {
    /**
     * Upload a file to be stored temporarily. These files should be associated with other data within 24 hours
     * or else they will be permanently deleted.
     *
     * @param fileUsageType the {@link FileUsageType} to associate with the uploaded file, which controls how/where the file is used
     * @param file the file being uploaded
     * @return a {@link TempFileDTO} containing the OID and URL to the temp file
     */
    TempFileDTO uploadTempFile(FileUsageType fileUsageType, MultipartFile file);
}
