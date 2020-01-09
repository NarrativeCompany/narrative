package org.narrative.network.customizations.narrative.niches.referendum;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.GBigDecimal;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.customizations.narrative.niches.referendum.dao.ReferendumVoteDAO;
import org.narrative.network.customizations.narrative.reputation.UserReputation;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {ReferendumVote.FIELD__VOTER__COLUMN, ReferendumVote.FIELD__REFERENDUM__COLUMN})})
public class ReferendumVote implements DAOObject<ReferendumVoteDAO> {
    private OID oid;
    private Referendum referendum;
    private AreaUserRlm voter;
    private Timestamp voteDatetime;
    private Boolean votedFor;
    private int votePoints;
    private ReferendumVoteReason reason;
    private OID commentReplyOid;

    public static final String FIELD__REFERENDUM__NAME = "referendum";
    public static final String FIELD__VOTER__NAME = "voter";
    public static final String FIELD__VOTE_DATETIME__NAME = "voteDatetime";
    public static final String FIELD__VOTED_FOR__NAME = "votedFor";

    public static final String FIELD__REFERENDUM__COLUMN = FIELD__REFERENDUM__NAME + "_" + Referendum.FIELD__OID__NAME;
    public static final String FIELD__VOTER__COLUMN = FIELD__VOTER__NAME + "_" + AreaUserRlm.FIELD__OID__NAME;

    public static final int MAX_COMMENT_LENGTH = 250;

    @Deprecated
    public ReferendumVote() { }

    public ReferendumVote(Referendum referendum, AreaUserRlm voter, Boolean votedFor) {
        this.referendum = referendum;
        this.voter = voter;
        this.votedFor = votedFor;
        this.votePoints = voter.getUser().getVotePoints(referendum);
        this.voteDatetime = now();

        // jw: Only increment the count when we have a new vote.
        if (votedFor != null) {
            referendum.addVotePoints(votedFor, votePoints);
        }
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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_referendumVote_referendum")
    public Referendum getReferendum() {
        return referendum;
    }

    public void setReferendum(Referendum referendum) {
        this.referendum = referendum;
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_referendumVote_voter")
    public AreaUserRlm getVoter() {
        return voter;
    }

    public void setVoter(AreaUserRlm voterAreaUserRlm) {
        this.voter = voterAreaUserRlm;
    }

    @NotNull
    public Timestamp getVoteDatetime() {
        return voteDatetime;
    }

    public void setVoteDatetime(Timestamp voteDatetime) {
        this.voteDatetime = voteDatetime;
    }

    public Boolean getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(Boolean votedFor) {
        this.votedFor = votedFor;
    }

    public int getVotePoints() {
        return votePoints;
    }

    public void setVotePoints(int votePoints) {
        this.votePoints = votePoints;
    }

    private transient GBigDecimal votePointsAsGBigDecimal;

    @Transient
    public GBigDecimal getVotePointsAsGBigDecimal() {
        if (votePointsAsGBigDecimal == null) {
            votePointsAsGBigDecimal = new GBigDecimal(BigDecimal.valueOf(getVotePoints()).divide(UserReputation.MAX_POINTS_PER_VOTE_BD, 10, RoundingMode.UP));
        }
        return votePointsAsGBigDecimal;
    }

    @Transient
    public String getVotePointsFormattedForApi() {
        return getVotePointsAsGBigDecimal().getFormattedWithTwoDecimals();
    }

    public void changeVote(boolean votedFor) {
        // jw: if the vote did not change, there is nothing to do.
        if (votedFor == getVotedFor()) {
            return;
        }

        int votePoints = getVoter().getUser().getVotePoints(getReferendum());

        // jw: decrement the old count and increment the new one
        getReferendum().removeVotePoints(getVotedFor(), getVotePoints());
        getReferendum().addVotePoints(votedFor, votePoints);

        // jw: update the internal vote status!
        setVotedFor(votedFor);
        setVotePoints(votePoints);
        setVoteDatetime(now());
    }

    @Type(type = IntegerEnumType.TYPE)
    public ReferendumVoteReason getReason() {
        return reason;
    }

    public void setReason(ReferendumVoteReason reason) {
        this.reason = reason;
    }

    public OID getCommentReplyOid() {
        return commentReplyOid;
    }

    public void setCommentReplyOid(OID commentReplyOid) {
        this.commentReplyOid = commentReplyOid;
    }

    private transient Reply commentReply;

    @Transient
    public Reply getCommentReply() {
        return commentReply;
    }

    public void setupCommentReply(Reply commentReply) {
        this.commentReply = commentReply;
    }

    public static ReferendumVoteDAO dao() {
        return DAOImpl.getDAO(ReferendumVote.class);
    }
}