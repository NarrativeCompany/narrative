package org.narrative.network.core.composition.files;

import org.narrative.network.core.fileondisk.audio.AudioMetaData;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Date: Jul 28, 2006
 * Time: 10:07:27 AM
 *
 * @author Brian
 */
@Entity
@DiscriminatorValue(FileType.AUDIO_TYPE_STRING)
public class AudioFilePointer extends FilePointer<AudioMetaData> {

    /**
     * @deprecated for hibernate use only
     */
    public AudioFilePointer() {}

    public AudioFilePointer(FilePointerSet filePointerSet, FileOnDisk fileOnDisk) {
        super(filePointerSet, fileOnDisk);
        // don't need to set any specific fields since the extra data will be set in the FilePointer
        // constructor automatically.  pretty cool.
    }

    @Transient
    public FileType getFileType() {
        return FileType.AUDIO;
    }

    @Transient
    public boolean isMp3() {
        return getFilename().toLowerCase().endsWith("mp3");
    }
}
