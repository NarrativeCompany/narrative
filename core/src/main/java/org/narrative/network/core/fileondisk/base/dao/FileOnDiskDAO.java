package org.narrative.network.core.fileondisk.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.fileondisk.audio.AudioOnDisk;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileOnDiskStatus;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.fileondisk.video.VideoOnDisk;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 2, 2005
 * Time: 4:31:33 PM
 */
public class FileOnDiskDAO extends GlobalDAOImpl<FileOnDisk, OID> {
    public FileOnDiskDAO() {
        super(FileOnDisk.class);
    }

    private static final Collection<Class<? extends FileOnDisk>> FILE_ON_DISK_DESCENDENTS;

    static {
        Set<Class<? extends FileOnDisk>> descendents = new HashSet<Class<? extends FileOnDisk>>();
        descendents.add(ImageOnDisk.class);
        descendents.add(AudioOnDisk.class);
        descendents.add(VideoOnDisk.class);
        FILE_ON_DISK_DESCENDENTS = Collections.unmodifiableSet(descendents);
    }

    public Collection<Class<? extends FileOnDisk>> getDAOObjectDescendents() {
        return FILE_ON_DISK_DESCENDENTS;
    }

    @Nullable
    public <T extends FileOnDisk> T get(OID oid, Class<T> cls) {
        if (oid == null) {
            return null;
        }
        return getGSession().load(cls, oid);
    }

    public List<OID> getExpiredTempFileOids(Timestamp olderThan) {
        return getGSession().getNamedQuery("fileOnDisk.getExpiredTempFileOids")
                .setParameter("tempFileStatus", FileOnDiskStatus.TEMP_FILE)
                .setParameter("olderThan", olderThan)
                .list();
    }
}
