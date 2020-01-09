package org.narrative.network.core.fileondisk.base;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.fileondisk.base.dao.FileOnDiskStatsDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * Date: Apr 27, 2009
 * Time: 1:38:50 PM
 *
 * @author brian
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class FileOnDiskStats implements DAOObject<FileOnDiskStatsDAO> {

    private OID oid;
    private FileOnDisk fileOnDisk;

    private int streamCount;
    private int downloadCount;

    public static final String FIELD__FILE_ON_DISK__NAME = "fileOnDisk";
    public static final String FIELD__STREAM_COUNT__NAME = "streamCount";

    /**
     * @deprecated for hibernate use only
     */
    public FileOnDiskStats() {}

    public FileOnDiskStats(FileOnDisk fileOnDisk) {
        this.fileOnDisk = fileOnDisk;
    }

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__FILE_ON_DISK__NAME)})
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = FileOnDiskStats.FIELD__OID__NAME, foreignKey = @javax.persistence.ForeignKey(name = "fk_fileondiskstats_fileondisk"))
    public FileOnDisk getFileOnDisk() {
        return fileOnDisk;
    }

    public void setFileOnDisk(FileOnDisk fileOnDisk) {
        this.fileOnDisk = fileOnDisk;
    }

    public int getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(int streamCount) {
        this.streamCount = streamCount;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    @Transient
    public void addStreamCount() {
        streamCount++;
    }

    @Transient
    public void addDownloadCount() {
        downloadCount++;
    }

    public static FileOnDiskStatsDAO dao() {
        return NetworkDAOImpl.getDAO(FileOnDiskStats.class);
    }
}
