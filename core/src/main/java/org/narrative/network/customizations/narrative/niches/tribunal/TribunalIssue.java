package org.narrative.network.customizations.narrative.niches.tribunal;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.BitIntegerEnumType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.metadata.NicheDetailChangeReferendumMetadata;
import org.narrative.network.customizations.narrative.niches.tribunal.dao.TribunalIssueDAO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
@Table(uniqueConstraints = {
        // jw: this index will give us search performance for lookups by type, and ensure we only ever have a single active
        //     type per niche, since the status column is nullable and only has a value when it is open.
        @UniqueConstraint(name = "tribunalIssue_channel_type_status_uidx", columnNames = {TribunalIssue.FIELD__CHANNEL__COLUMN, TribunalIssue.FIELD__TYPE__COLUMN, TribunalIssue.FIELD__STATUS__COLUMN})
})
public class TribunalIssue implements DAOObject<TribunalIssueDAO> {
    private OID oid;
    private Channel channel;
    private TribunalIssueType type;
    private TribunalIssueStatus status;
    private Timestamp creationDatetime;
    private Referendum referendum;
    private List<TribunalIssueReport> tribunalIssueReports;

    private TribunalIssueReport lastReport;
    private TribunalIssueReport lastReport2;
    private TribunalIssueReport lastReport3;

    public static final String FIELD__CHANNEL__NAME = "channel";
    public static final String FIELD__TYPE__NAME = "type";
    public static final String FIELD__STATUS__NAME = "status";

    public static final String FIELD__CHANNEL__COLUMN = FIELD__CHANNEL__NAME + "_" + Channel.FIELD__OID__NAME;
    public static final String FIELD__TYPE__COLUMN = FIELD__TYPE__NAME;
    public static final String FIELD__STATUS__COLUMN = FIELD__STATUS__NAME;

    @Deprecated
    public TribunalIssue() { }

    public TribunalIssue(Channel channel, TribunalIssueType type) {
        this.channel = channel;
        this.type = type;
        // jw: default new issues open.
        this.status = TribunalIssueStatus.OPEN;
        creationDatetime = now();
        tribunalIssueReports = new ArrayList<>();
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

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_tribunalIssue_channel")
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    public TribunalIssueType getType() {
        return type;
    }

    public void setType(TribunalIssueType issueType) {
        this.type = issueType;
    }

    @Type(type = BitIntegerEnumType.TYPE)
    public TribunalIssueStatus getStatus() {
        return status;
    }

    public void setStatus(TribunalIssueStatus status) {
        this.status = status;
    }

    @NotNull
    public Timestamp getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Timestamp creationTimestamp) {
        this.creationDatetime = creationTimestamp;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_tribunalIssue_referendum")
    public Referendum getReferendum() {
        return referendum;
    }

    public void setReferendum(Referendum referendum) {
        this.referendum = referendum;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = TribunalIssueReport.FIELD_TRIBUNAL_ISSUE_NAME, cascade = CascadeType.ALL)
    @OrderBy(TribunalIssueReport.FIELD_CREATION_DATE_NAME + " DESC")
    public List<TribunalIssueReport> getTribunalIssueReports() {
        return tribunalIssueReports;
    }

    public void setTribunalIssueReports(List<TribunalIssueReport> tribunalIssueReports) {
        this.tribunalIssueReports = tribunalIssueReports;
    }

    @Transient
    public NicheDetailChangeReferendumMetadata getNicheEditDetail() {
        if (getType().isApproveNicheDetailChange()) {
            return (NicheDetailChangeReferendumMetadata) getReferendum().getMetadata();
        }

        return null;
    }

    @Transient
    public String getDisplayUrl() {
        return getReferendum().getDetailsUrl();
    }

    public static TribunalIssue getIssueForPreviewEmail(Niche nicheForPreview, TribunalIssueType type) {
        TribunalIssue issue = new TribunalIssue(nicheForPreview.getChannel(), type);

        Referendum referendumForTribunal = new Referendum(nicheForPreview, type.getReferendumTypeForTribunal(), PartitionType.COMPOSITION.getBalancedPartition());
        issue.setReferendum(referendumForTribunal);
        referendumForTribunal.setTribunalIssue(issue);
        referendumForTribunal.setupTribunalMemberCountForPreviewEmail();

        return issue;
    }

    @OneToOne
    @ForeignKey(name = "fk_tribunalIssue_lastReport")
    public TribunalIssueReport getLastReport() {
        return lastReport;
    }

    public void setLastReport(TribunalIssueReport report) {
        lastReport = report;
    }

    @OneToOne
    @ForeignKey(name = "fk_tribunalIssue_lastReport2")
    public TribunalIssueReport getLastReport2() {
        return lastReport2;
    }

    public void setLastReport2(TribunalIssueReport report) {
        lastReport2 = report;
    }

    @OneToOne
    @ForeignKey(name = "fk_tribunalIssue_lastReport3")
    public TribunalIssueReport getLastReport3() {
        return lastReport3;
    }

    public void setLastReport3(TribunalIssueReport report) {
        lastReport3 = report;
    }

    public void updateLastReportForNewReport(TribunalIssueReport report) {
        TribunalIssueReport lastReport = report;
        for (LastTribunalIssueReportType lastReportType : LastTribunalIssueReportType.values()) {
            // bl: first, get the oldLastReport that is currently in this slot
            TribunalIssueReport oldLastReport = lastReportType.getForTribunalIssue(this);
            // bl: then, set the new lastReport
            lastReportType.setForTribunalIssue(this, lastReport);
            // bl: finally, update the lastReport to the oldLastReport before we loop through so that it
            // can cycle down to the next
            lastReport = oldLastReport;
        }
    }

    private transient List<TribunalIssueReport> lastReports;

    @Transient
    public List<TribunalIssueReport> getLastReports() {
        if (lastReports == null) {
            List<LastTribunalIssueReportType> lastReportTypes = Arrays.asList(LastTribunalIssueReportType.values());
            List<TribunalIssueReport> reports = new ArrayList<>(lastReportTypes.size());

            for (LastTribunalIssueReportType lastReportType : lastReportTypes) {
                TribunalIssueReport report = lastReportType.getForTribunalIssue(this);
                // bl: as soon as we don't find a report, we can quit, as we shouldn't ever be "skipping" a last report slot.
                if (!exists(report)) {
                    break;
                }
                reports.add(report);
            }

            lastReports = Collections.unmodifiableList(reports);
        }
        return lastReports;
    }

    @Transient
    public String getReportsUrl() {
        return null;
    }

    public static TribunalIssueDAO dao() {
        return DAOImpl.getDAO(TribunalIssue.class);
    }
}