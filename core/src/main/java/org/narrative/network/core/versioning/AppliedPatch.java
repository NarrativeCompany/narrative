package org.narrative.network.core.versioning;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernatePropertiesType;
import org.narrative.common.util.Debug;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.versioning.dao.AppliedPatchDAO;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotNull;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import java.sql.Timestamp;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 4:33:05 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {AppliedPatch.FIELD__NAME__COLUMN, AppliedPatch.FIELD__PARTITION__COLUMN}))
public class AppliedPatch implements DAOObject<AppliedPatchDAO> {

    private OID oid;
    private String name;
    private Partition partition;
    private Timestamp completeDatetime;
    private long runTimeMs;
    private String errorText;
    private Properties data;
    private String version;

    public static final String FIELD__NAME__NAME = "name";
    public static final String FIELD__PARTITION__NAME = "partition";
    public static final String FIELD__NAME__COLUMN = FIELD__NAME__NAME;
    public static final String FIELD__PARTITION__COLUMN = FIELD__PARTITION__NAME + "_" + FIELD__OID__NAME;

    /**
     * @deprecated for hibernate use only
     */
    public AppliedPatch() {
    }

    public AppliedPatch(Patch patch, Partition partition) {
        this.name = patch.getName();
        this.partition = partition;
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @NotNull
    @Length(min = 1)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "FKAE9419EB6D6C7C2C")
    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    //@Temporal(TemporalType.TIMESTAMP)
    public Timestamp getCompleteDatetime() {
        return completeDatetime;
    }

    public void setCompleteDatetime(Timestamp completeDatetime) {
        this.completeDatetime = completeDatetime;
    }

    public long getRunTimeMs() {
        return runTimeMs;
    }

    public void setRunTimeMs(long runTimeMs) {
        this.runTimeMs = runTimeMs;
    }

    @Basic(fetch = FetchType.EAGER, optional = true)
    @Lob
    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    @Basic(fetch = FetchType.EAGER, optional = true)
    @Lob
    @Type(type = HibernatePropertiesType.TYPE)
    public Properties getData() {
        return data;
    }

    public void setData(Properties data) {
        this.data = data;
    }

    @Length(min = 1, max = 100)
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Transient
    public boolean isComplete() {
        return completeDatetime != null;
    }

    @Transient
    public void markComplete() {
        this.completeDatetime = new Timestamp(System.currentTimeMillis());
        this.version = NetworkRegistry.getInstance().getVersionStringForPatches();
        this.errorText = null;
    }

    @Transient
    public void markFailedWithError(Throwable t) {
        this.completeDatetime = null;
        this.version = NetworkRegistry.getInstance().getVersionStringForPatches();
        this.errorText = Debug.stackTraceFromException(t);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AppliedPatch that = (AppliedPatch) o;

        if (!name.equals(that.name)) {
            return false;
        }
        if (!partition.getOid().equals(that.partition.getOid())) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = name.hashCode();
        result = 29 * result + partition.getOid().hashCode();
        return result;
    }

    public static AppliedPatchDAO dao() {
        return DAOImpl.getDAO(AppliedPatch.class);
    }

}
