package org.narrative.network.core.versioning;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.versioning.dao.AppVersionDAO;
import javax.validation.constraints.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 31, 2008
 * Time: 2:21:31 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class AppVersion implements DAOObject<AppVersionDAO> {

    private OID oid;
    private String version;
    private Timestamp startDatetime;
    private Timestamp completeDatetime;

    public static final String FIELD__VERSION__NAME = "version";
    public static final String FIELD__START_DATETIME__NAME = "startDatetime";
    public static final String FIELD__COMPLETE_DATETIME__NAME = "completeDatetime";

    public static final String FIELD__VERSION__COLUMN = FIELD__VERSION__NAME;
    public static final String FIELD__START_DATETIME__COLUMN = FIELD__START_DATETIME__NAME;
    public static final String FIELD__COMPLETE_DATETIME__COLUMN = FIELD__COMPLETE_DATETIME__NAME;

    /**
     * @deprecated for hibernate use only
     */
    public AppVersion() {
    }

    public AppVersion(String version) {
        this.version = version;
        this.startDatetime = new Timestamp(System.currentTimeMillis());
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
    @Column(unique = true)
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @NotNull
    public Timestamp getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(Timestamp startDatetime) {
        this.startDatetime = startDatetime;
    }

    public Timestamp getCompleteDatetime() {
        return completeDatetime;
    }

    public void setCompleteDatetime(Timestamp completeDatetime) {
        this.completeDatetime = completeDatetime;
    }

    public static AppVersionDAO dao() {
        return DAOImpl.getDAO(AppVersion.class);
    }

}
