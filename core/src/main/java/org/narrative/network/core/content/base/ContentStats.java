package org.narrative.network.core.content.base;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.composition.base.CompositionStats;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.content.base.dao.ContentStatsDAO;
import org.narrative.network.core.moderation.ModeratableStats;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.likes.LikeFields;
import org.narrative.network.shared.security.PrimaryRole;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Proxy;
import org.hibernate.validator.constraints.Range;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 4:21:18 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ContentStats implements DAOObject<ContentStatsDAO>, ModeratableStats {
    private OID oid;
    private Content content;

    private int areaPageViews;
    private int moderatedReplyCount;
    private int reportCount;

    private LikeFields areaLikeFields;
    private LikeFields globalLikeFields;

    //denorm
    private int replyCount;
    private OID lastReplyUserOid;
    private String lastReplyGuestName;

    public static final String FIELD__CONTENT__NAME = "content";
    public static final String FIELD__REPLY_COUNT__NAME = "replyCount";
    public static final String FIELD__MODERATED_REPLY_COUNT__NAME = "moderatedReplyCount";
    public static final String FIELD__AREA_LIKE_FIELDS__NAME = "areaLikeFields";

    @Deprecated
    public ContentStats() {}

    public ContentStats(Content content) {
        setContent(content);
        areaLikeFields = new LikeFields();
        globalLikeFields = new LikeFields();
    }

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__CONTENT__NAME)})
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = ContentStats.FIELD__OID__NAME, foreignKey = @javax.persistence.ForeignKey(name = "fk_contentstats_content"))
    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    @NotNull
    @Range(min = 0)
    public int getReplyCount() {
        return replyCount;
    }

    @Transient
    public int getLiveReplyCount() {
        return getReplyCount() - getModeratedReplyCount();
    }

    /**
     * @param replyCount
     * @deprecated use syncStats instead
     */
    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public OID getLastReplyUserOid() {
        return lastReplyUserOid;
    }

    public void setLastReplyUserOid(OID lastReplyUserOid) {
        this.lastReplyUserOid = lastReplyUserOid;
    }

    @Transient
    public User getLastReplyUser() {
        return User.dao().get(getLastReplyUserOid());
    }

    public String getLastReplyGuestName() {
        return lastReplyGuestName;
    }

    public void setLastReplyGuestName(String lastReplyGuestName) {
        this.lastReplyGuestName = lastReplyGuestName;
    }

    transient PrimaryRole lastReplyPrimaryRole;

    @Transient
    public PrimaryRole getLastReplyPrimaryRole() {
        if (lastReplyPrimaryRole == null) {
            lastReplyPrimaryRole = PrimaryRole.getPrimaryRole(getContent().getAuthZone(), getLastReplyUser(), getLastReplyGuestName());
        }

        return lastReplyPrimaryRole;
    }

    @NotNull
    @Range(min = 0)
    public int getAreaPageViews() {
        return areaPageViews;
    }

    public void setAreaPageViews(int pageViews) {
        this.areaPageViews = pageViews;
    }

    public static ContentStatsDAO dao() {
        return DAOImpl.getDAO(ContentStats.class);
    }

    @Override
    public void syncStats(CompositionStats compositionStats) {
        setReplyCount(Math.max(compositionStats.getReplyCount(), 0));
        if (exists(compositionStats.getLastReply())) {
            Reply lastReply = compositionStats.getLastReply();
            setLastReplyUserOid(lastReply.getUserOid());
            setLastReplyGuestName(lastReply.getGuestNameResolved());
        } else {
            setLastReplyUserOid(null);
            setLastReplyGuestName(null);
        }
        // bl: sync the CompositionStats to Content now that lastUpdateDatetime is on Content.
        // nb. will be relying on Content already having been 'locked' in the database prior to calling this.
        getContent().syncStats(compositionStats);
    }

    @NotNull
    @Range(min = 0)
    public int getModeratedReplyCount() {
        return moderatedReplyCount;
    }

    public void setModeratedReplyCount(int moderatedReplyCount) {
        this.moderatedReplyCount = moderatedReplyCount;
    }

    @NotNull
    @Range(min = 0)
    public int getReportCount() {
        return reportCount;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }

    @NotNull
    public LikeFields getAreaLikeFields() {
        return areaLikeFields;
    }

    public void setAreaLikeFields(LikeFields areaLikeFields) {
        this.areaLikeFields = areaLikeFields;
    }

    @NotNull
    public LikeFields getGlobalLikeFields() {
        return globalLikeFields;
    }

    public void setGlobalLikeFields(LikeFields globalLikeFields) {
        this.globalLikeFields = globalLikeFields;
    }

    @Transient
    public void addModeratedReply() {
        setModeratedReplyCount(Math.max(0, getModeratedReplyCount()) + 1);
    }

    @Transient
    @Override
    public void removeModeratedReply() {
        setModeratedReplyCount(Math.max(getModeratedReplyCount() - 1, 0));
    }

    @Transient
    @Override
    public void addReport() {
        setReportCount(Math.max(0, getReportCount()) + 1);
    }

    @Transient
    public void addToAreaPageViewCount(int count) {
        setAreaPageViews(getAreaPageViews() + count);
    }
}
