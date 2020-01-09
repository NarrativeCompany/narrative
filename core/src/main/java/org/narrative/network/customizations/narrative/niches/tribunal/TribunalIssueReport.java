package org.narrative.network.customizations.narrative.niches.tribunal;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.niches.tribunal.dao.TribunalIssueReportDAO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: Feb 12, 2018
 * Time: 11:37:03 AM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TribunalIssueReport implements DAOObject<TribunalIssueReportDAO> {

    public static final int HOURS_BETWEEN_APPEALS = 24;

    public static final String FIELD_TRIBUNAL_ISSUE_NAME = "tribunalIssue";
    public static final String FIELD_CREATION_DATE_NAME = "creationDatetime";

    private OID oid;
    private AreaUserRlm reporter;
    private String comments;
    private Timestamp creationDatetime;
    private TribunalIssue tribunalIssue;

    @Deprecated
    public TribunalIssueReport() {
    }

    public TribunalIssueReport(String comments, AreaUserRlm reporter, TribunalIssue tribunalIssue) {
        this.comments = comments;
        this.reporter = reporter;
        this.tribunalIssue = tribunalIssue;
        creationDatetime = now();
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    @Override
    public void setOid(OID oid) {
        this.oid = oid;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_tribunalIssueReport_reporter")
    public AreaUserRlm getReporter() {
        return reporter;
    }

    public void setReporter(AreaUserRlm reporter) {
        this.reporter = reporter;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "mediumtext")
    @Lob
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @NotNull
    public Timestamp getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Timestamp creationTimestamp) {
        this.creationDatetime = creationTimestamp;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_tribunalIssueReport_tribunalIssue")
    public TribunalIssue getTribunalIssue() {
        return tribunalIssue;
    }

    public void setTribunalIssue(TribunalIssue tribunalIssue) {
        this.tribunalIssue = tribunalIssue;
    }

    public static TribunalIssueReportDAO dao() {
        return DAOImpl.getDAO(TribunalIssueReport.class);
    }
}