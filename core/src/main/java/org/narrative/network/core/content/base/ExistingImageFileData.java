package org.narrative.network.core.content.base;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.fileondisk.base.FileBase;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.fileondisk.image.ImageMetaData;

/**
 * Date: Mar 24, 2006
 * Time: 8:59:45 AM
 *
 * @author Brian
 */
public class ExistingImageFileData extends ExistingFileData<ImageMetaData> implements ImageFileData {

    public ExistingImageFileData(OID fileUploadProcessOid, FileBase fileOnDisk, FileUsageType fileUsageType) {
        super(fileUploadProcessOid, fileOnDisk, fileUsageType);
    }

    public FileType getFileType() {
        return FileType.IMAGE;
    }
}
