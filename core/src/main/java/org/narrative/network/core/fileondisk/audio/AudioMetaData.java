package org.narrative.network.core.fileondisk.audio;

import org.narrative.network.core.fileondisk.base.FileType;

/**
 * Date: Jul 28, 2006
 * Time: 11:15:10 AM
 *
 * @author Brian
 */
public class AudioMetaData extends BaseAudioMetaData {

    /**
     * @deprecated for hibernate use only (actually, for use in FileMetaDataType)
     */
    public AudioMetaData() {
        super(FileType.AUDIO);
    }

    public AudioMetaData(int lengthSeconds, int bitRateKbps) {
        super(FileType.AUDIO, lengthSeconds, bitRateKbps);
    }

    public AudioMetaData(byte[] data) {
        super(FileType.AUDIO, data);
    }
}
