package org.narrative.network.core.composition.files.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.files.AudioFilePointer;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.composition.files.ImageFilePointer;
import org.narrative.network.core.composition.files.VideoFilePointer;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.shared.daobase.CompositionDAOImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Date: Dec 2, 2005
 * Time: 3:57:14 PM
 *
 * @author Brian
 */
public class FilePointerDAO extends CompositionDAOImpl<FilePointer, OID> {
    public FilePointerDAO() {
        super(FilePointer.class);
    }

    private static final Collection<Class<? extends FilePointer>> FILE_POINTER_DESCENDENTS;

    static {
        Set<Class<? extends FilePointer>> descendents = new HashSet<Class<? extends FilePointer>>();
        descendents.add(ImageFilePointer.class);
        descendents.add(AudioFilePointer.class);
        descendents.add(VideoFilePointer.class);
        FILE_POINTER_DESCENDENTS = Collections.unmodifiableSet(descendents);
    }

    public Collection<Class<? extends FilePointer>> getDAOObjectDescendents() {
        return FILE_POINTER_DESCENDENTS;
    }

    public List<FilePointer> getAllForFileOnDisk(FileOnDisk fileOnDisk) {
        return getGSession().getNamedQuery("filePointer.getAllForFileOnDiskOid").setParameter("fileOnDiskOid", fileOnDisk.getOid()).list();
    }

}
