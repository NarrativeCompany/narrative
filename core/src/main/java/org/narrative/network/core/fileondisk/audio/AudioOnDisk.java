package org.narrative.network.core.fileondisk.audio;

import org.narrative.network.core.content.base.UploadedAudioFileData;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.user.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:38:13 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@DiscriminatorValue(FileType.AUDIO_TYPE_STRING)
public class AudioOnDisk extends FileOnDisk<AudioMetaData> {

    /**
     * @deprecated for hibernate use only
     */
    public AudioOnDisk() {}

    public AudioOnDisk(UploadedAudioFileData audioFileData, User user) {
        super(audioFileData, user);
    }

    @Transient
    public FileType getFileType() {
        return FileType.AUDIO;
    }
}
