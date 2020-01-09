package org.narrative.network.core.content.base;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.fileondisk.audio.AudioMetaData;
import org.narrative.network.core.fileondisk.base.FileBase;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;

/**
 * Date: Mar 24, 2006
 * Time: 8:59:45 AM
 *
 * @author Brian
 */
public class ExistingAudioFileData extends ExistingFileData<AudioMetaData> implements AudioFileData {

    public ExistingAudioFileData(OID fileUploadProcessOid, FileBase fileOnDisk, FileUsageType fileUsageType) {
        super(fileUploadProcessOid, fileOnDisk, fileUsageType);
    }

    public FileType getFileType() {
        return FileType.AUDIO;
    }
}
