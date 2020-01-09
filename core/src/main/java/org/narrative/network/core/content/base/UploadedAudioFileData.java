package org.narrative.network.core.content.base;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.fileondisk.audio.AudioMetaData;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.shared.util.NetworkLogger;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.File;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Mar 23, 2006
 * Time: 8:49:54 AM
 *
 * @author Brian
 */
public class UploadedAudioFileData extends UploadedFileData<AudioMetaData> implements AudioFileData {
    private static final NetworkLogger logger = new NetworkLogger(UploadedAudioFileData.class);

    private ID3v1Tag id3 = null;
    private ID3v24Tag id3v2 = null;

    public UploadedAudioFileData(OID fileUploadProcessOid, FileUsageType fileUsageType, File file, String mimeType, String filename) {
        super(fileUploadProcessOid, fileUsageType, file, mimeType, filename);

        boolean isValidExtension = false;

        for (String validFileExtension : getFileType().getExtensions()) {
            if (filename.endsWith(validFileExtension)) {
                isValidExtension = true;
                break;
            }
        }

        if (!isValidExtension) {
            return;
        }

        markInvalid();
        setErrorMessage(wordlet("audio.invalidFormat"));
    }

    public FileType getFileType() {
        return FileType.AUDIO;
    }

    @Override
    protected void postUploadSubProcess(OID fileUploadProcessOid) {
        // bl: if we allow files of this type to be unconverted, then just let it bypass straight through without further conversion.
        FileUsageType fileUsageType = getFileUsageType();
        if (fileUsageType.isBypassFileProcessing()) {
            super.postUploadSubProcess(fileUploadProcessOid);
            return;
        }
        File outFile = createTempFile(getTempFilenameForFileUploadProcessOid(fileUploadProcessOid), getTempFileExtension(), true);
        markInvalid();
    }

    public ID3v24Tag getID3v24Tag() {
        if (id3v2 == null && getTempFile() != null && getTempFile().exists()) {
            MP3File mp3;
            try {
                mp3 = new MP3File(getTempFile());
            } catch (Exception e) {
                logger.warn("Unable to get ID3 tag for temp file " + getTempFile().getAbsolutePath());
                return null;
            }

            AbstractID3v2Tag tag = mp3.getID3v2Tag();
            if (!(tag instanceof ID3v24Tag)) {
                tag = new ID3v24Tag(tag);
            }
            id3v2 = (ID3v24Tag) tag;
        }
        return id3v2;

    }

    public ID3v1Tag getID3v1() {
        if (id3 == null && getTempFile() != null && getTempFile().exists()) {
            MP3File mp3;
            try {
                mp3 = new MP3File(getTempFile());
            } catch (Exception e) {
                logger.warn("Unable to get ID3 tag for temp file " + getTempFile().getAbsolutePath());
                return null;
            }

            id3 = mp3.getID3v1Tag();
        }
        return id3;
    }

    public String getID3Tag(String frameName) {

        //try v2 first
        AbstractID3v2Tag tag = getID3v24Tag();
        if (tag != null) {
            AbstractID3v2Frame frame = (AbstractID3v2Frame) tag.getFrame(frameName);
            if (frame != null) {
                AbstractTagFrameBody body = frame.getBody();
                if (body != null) {
                    String val = (String) body.getObjectValue(DataTypes.OBJ_TEXT);
                    if (val != null) {
                        return val.replaceAll("\u0000", "");
                    }
                }
            }
        }

        //v2 didn't work so lets do v1
        ID3v1Tag v1 = getID3v1();
        if (v1 != null) {
            if (frameName.equals(ID3v24Frames.FRAME_ID_ALBUM)) {
                return v1.getFirst(FieldKey.ALBUM);
            } else if (frameName.equals(ID3v24Frames.FRAME_ID_ARTIST)) {
                return v1.getFirst(FieldKey.ARTIST);
            } else if (frameName.equals(ID3v24Frames.FRAME_ID_TITLE)) {
                return v1.getFirstTitle();
            } else if (frameName.equals(ID3v24Frames.FRAME_ID_GENRE)) {
                return v1.getFirstGenre();
            } else if (frameName.equals(ID3v24Frames.FRAME_ID_YEAR)) {
                return v1.getFirstYear();
            }
        }

        return null;
    }
}
