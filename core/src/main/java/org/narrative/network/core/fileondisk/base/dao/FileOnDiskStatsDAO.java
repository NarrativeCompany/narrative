package org.narrative.network.core.fileondisk.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.fileondisk.base.FileOnDiskStats;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: Apr 27, 2009
 * Time: 1:39:00 PM
 *
 * @author brian
 */
public class FileOnDiskStatsDAO extends GlobalDAOImpl<FileOnDiskStats, OID> {
    public FileOnDiskStatsDAO() {
        super(FileOnDiskStats.class);
    }
}
