package org.narrative.network.core.composition.files;

import org.narrative.common.util.CoreUtils;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.ImageFileMetaDataProvider;
import org.narrative.network.core.fileondisk.image.ImageMetaData;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;

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
@DiscriminatorValue(FileType.IMAGE_TYPE_STRING)
public class ImageFilePointer extends FilePointer<ImageMetaData> implements ImageFileMetaDataProvider {

    /**
     * @deprecated for hibernate use only
     */
    public ImageFilePointer() {}

    public ImageFilePointer(FilePointerSet filePointerSet, FileOnDisk fileOnDisk) {
        super(filePointerSet, fileOnDisk);
        // don't need to set any specific fields since the extra data will be set in the FilePointer
        // constructor automatically.  pretty cool.
    }

    @Transient
    public FileType getFileType() {
        return FileType.IMAGE;
    }

    @Transient
    public ImageOnDisk getImageOnDisk() {
        return CoreUtils.cast(getFileOnDisk(), ImageOnDisk.class);
    }
}
