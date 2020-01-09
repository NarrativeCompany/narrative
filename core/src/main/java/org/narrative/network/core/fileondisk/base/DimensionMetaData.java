package org.narrative.network.core.fileondisk.base;

import org.narrative.network.core.fileondisk.image.ImageType;

/**
 * Date: Sep 4, 2008
 * Time: 11:06:35 AM
 *
 * @author brian
 */
public interface DimensionMetaData extends FileMetaData {
    public int getWidth();

    public void setWidth(int width);

    public int getHeight();

    public void setHeight(int height);

    public ImageType getPrimaryImageType();
}
