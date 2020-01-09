package org.narrative.network.customizations.narrative.niches.referendum;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernatePropertiesType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.GBigDecimal;
import org.narrative.common.util.IPDateUtil;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.security.area.community.advanced.AreaCirclePermission;
import org.narrative.network.core.security.area.community.advanced.services.GlobalSecurable;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.channels.DeletedChannel;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.referendum.dao.ReferendumDAO;
import org.narrative.network.customizations.narrative.niches.referendum.metadata.NicheDetailChangeReferendumMetadata;
import org.narrative.network.customizations.narrative.niches.referendum.services.ReferendumCompositionConsumer;
import org.narrative.network.customizations.narrative.niches.services.DatetimeCountdownProvider;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.reputation.UserReputation;
import org.narrative.network.customizations.narrative.service.mapper.ReferendumMapper;
import org.narrative.network.customizations.narrative.services.ReactRoute;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Referendum extends ReferendumCompositionConsumer implements DAOObject<ReferendumDAO>, DatetimeCountdownProvider {
    public static final String FIELD__PUBLICATION__NAME = "publication";

    private OID oid;
    private Timestamp startDatetime;
    private Timestamp endDatetime;
    private ReferendumType type;
    private Niche niche;
    // jw: to improve DTO generation performance we need an optional reference to publication.
    private Publication publication;
    private DeletedChannel deletedChannel;
    private TribunalIssue tribunalIssue;
    private boolean open;

    private OID compositionPartitionOid;

    // jw: I think we're going to need to have denormlized counts for the votes for, and the votes against
    // jw: due to locking, we may need to move this into a stats table that hangs off of this.
    private int votePointsFor;
    private int votePointsAgainst;

    // jw: there are a few types that require ancillary data, so let's use a properties object to store that, so the table does not have to grow wider to support it.
    private Properties properties;

    private List<ReferendumVote> referendumVotes;

    private transient boolean isNew;

    @Deprecated
    public Referendum() {}

    public Referendum(ChannelConsumer channelConsumer, ReferendumType type, Partition compositionPartition) {
        assert exists(channelConsumer) : "Should always be provided a channel consumer!";
        assert channelConsumer.getChannelType() == type.getChannelType() : "The channelConsumer provided should always be of the expected type";

        if (channelConsumer.getChannelType().isNiche()) {
            this.niche = (Niche) channelConsumer;
        } else {
            assert type.isPublicationRelated() : "Expected publication related type, not/"+type;
            this.publication = (Publication) channelConsumer;
        }

        this.type = type;
        this.startDatetime = now();
        this.endDatetime = new Timestamp(startDatetime.getTime() + type.getInitialVotingPeriodInHours() * IPDateUtil.HOUR_IN_MS);
        this.open = true;

        assert exists(compositionPartition) && compositionPartition.getPartitionType().isComposition() : "Must provide Composition Partition when creating a Referendum!";
        this.compositionPartitionOid = compositionPartition.getOid();

        this.isNew = true;

        // jw: if the type has metadata, let's assume that the properties are going to be affected right after creation!
        if (type.getMetadataClass() != null) {
            this.properties = new Properties();
        }
    }

    @Override
    @Transient
    public boolean isNew() {
        return isNew;
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

    @NotNull
    public Timestamp getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(Timestamp startDatetime) {
        this.startDatetime = startDatetime;
    }

    @NotNull
    public Timestamp getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(Timestamp endDatetime) {
        this.endDatetime = endDatetime;
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    @Override
    public ReferendumType getType() {
        return type;
    }

    public void setType(ReferendumType type) {
        this.type = type;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_referendum_niche")
    public Niche getNiche() {
        return niche;
    }

    public void setNiche(Niche niche) {
        this.niche = niche;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_referendum_publication")
    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_referendum_deletedChannel")
    public DeletedChannel getDeletedChannel() {
        return deletedChannel;
    }

    public void setDeletedChannel(DeletedChannel deletedChannel) {
        this.deletedChannel = deletedChannel;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_referendum_tribunalIssue")
    public TribunalIssue getTribunalIssue() {
        return tribunalIssue;
    }

    public void setTribunalIssue(TribunalIssue tribunalIssue) {
        this.tribunalIssue = tribunalIssue;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void checkCanRoleVote(AreaRole areaRole) {
        areaRole.checkRegisteredCommunityUser();

        if (getType().isTribunalReferendum()) {
            areaRole.getPrimaryRole().checkCanParticipateInTribunalIssues();
        } else {
            areaRole.getPrimaryRole().checkCanVoteOnNiches();
        }

        if (!isOpen()) {
            if (getType().isTribunalReferendum()) {
                throw new AccessViolation(wordlet("nicheVoteAjaxAction.appealClosed"));
            } else {
                throw new AccessViolation(wordlet("nicheVoteAjaxAction.approvalClosed"));
            }
        }
    }

    public boolean isCanAreaRoleVote(AreaRole role) {
        try {
            checkCanRoleVote(role);
            return true;
        } catch(AccessViolation av) {
            return false;
        }
    }

    @NotNull
    public OID getCompositionPartitionOid() {
        return compositionPartitionOid;
    }

    public void setCompositionPartitionOid(OID compositionPartitionOid) {
        this.compositionPartitionOid = compositionPartitionOid;
    }

    @Override
    @Transient
    public Partition getCompositionPartition() {
        return Partition.dao().get(getCompositionPartitionOid());
    }

    private transient Integer totalVotes;

    @Transient
    public int getTotalVotes() {
        if (totalVotes == null) {
            totalVotes = ReferendumVote.dao().getCount(this);
        }

        return totalVotes;
    }

    private void setTotalVotes(Integer totalVotes) {
        this.totalVotes = totalVotes;
    }

    public int getVotePointsFor() {
        return votePointsFor;
    }

    public void setVotePointsFor(int votePointsFor) {
        this.votePointsFor = votePointsFor;
    }

    private transient GBigDecimal votePointsForAsGBigDecimal;

    @Transient
    private GBigDecimal getVotePointsForAsGBigDecimal() {
        if (votePointsForAsGBigDecimal == null) {
            votePointsForAsGBigDecimal = new GBigDecimal(BigDecimal.valueOf(getVotePointsFor()).divide(UserReputation.MAX_POINTS_PER_VOTE_BD, 2, RoundingMode.UNNECESSARY));
        }

        return votePointsForAsGBigDecimal;
    }

    @Transient
    public String getVotePointsForFormatted() {
        return getVotePointsForAsGBigDecimal().getFormattedWithGroupingsAndTwoDecimals();
    }

    @Transient
    public int getTribunalVotesFor() {
        assert getType().isTribunalReferendum() : "Should only get Tribunal vote count for Tribunal referendums!";
        return BigDecimal.valueOf(getVotePointsFor()).divide(UserReputation.MAX_POINTS_PER_VOTE_BD, 0, RoundingMode.UNNECESSARY).intValueExact();
    }

    @Transient
    public int getTribunalVotesAgainst() {
        assert getType().isTribunalReferendum() : "Should only get Tribunal vote count for Tribunal referendums!";
        return BigDecimal.valueOf(getVotePointsAgainst()).divide(UserReputation.MAX_POINTS_PER_VOTE_BD, 0, RoundingMode.UNNECESSARY).intValueExact();
    }

    @Transient
    public String getVotePointsForFormattedForApi() {
        return getVotePointsForAsGBigDecimal().getFormattedWithTwoDecimals();
    }

    public int getVotePointsAgainst() {
        return votePointsAgainst;
    }

    public void setVotePointsAgainst(int votePointsAgainst) {
        this.votePointsAgainst = votePointsAgainst;
    }

    private transient GBigDecimal votePointsAgainstAsGBigDecimal;

    @Transient
    private GBigDecimal getVotePointsAgainstAsGBigDecimal() {
        if (votePointsAgainstAsGBigDecimal == null) {
            votePointsAgainstAsGBigDecimal = new GBigDecimal(BigDecimal.valueOf(getVotePointsAgainst()).divide(UserReputation.MAX_POINTS_PER_VOTE_BD, 2, RoundingMode.UNNECESSARY));
        }

        return votePointsAgainstAsGBigDecimal;
    }

    @Transient
    public String getVotePointsAgainstFormatted() {
        return getVotePointsAgainstAsGBigDecimal().getFormattedWithGroupingsAndTwoDecimals();
    }

    @Transient
    public String getVotePointsAgainstFormattedForApi() {
        return getVotePointsAgainstAsGBigDecimal().getFormattedWithTwoDecimals();
    }

    @Transient
    public int getTotalVotePoints() {
        return getVotePointsFor() + getVotePointsAgainst();
    }

    @Transient
    public int getTotalTribunalVotes() {
        assert getType().isTribunalReferendum() : "Should only ever call this for tribunal referendums.";

        // jw: because all tribunal votes are worth 100 points, this should always result in a whole number.
        return BigDecimal.valueOf(getTotalVotePoints()).divide(UserReputation.MAX_POINTS_PER_VOTE_BD, 0, RoundingMode.UNNECESSARY).intValueExact();
    }

    private transient GBigDecimal approvalPercentage;

    @Transient
    public GBigDecimal getApprovalPercentage() {
        if (approvalPercentage == null) {
            approvalPercentage = calculateApprovalPercentage();
        }

        return approvalPercentage;
    }

    private transient GBigDecimal againstPercentage;

    @Transient
    public GBigDecimal getAgainstPercentage() {
        if (againstPercentage == null) {
            againstPercentage = new GBigDecimal(BigDecimal.valueOf(100).subtract(getApprovalPercentage().getValue()));
        }

        return againstPercentage;
    }

    private GBigDecimal calculateApprovalPercentage() {
        if (getVotePointsFor() <= 0) {
            return GBigDecimal.ZERO;
        }
        return GBigDecimal.calculatePercentage(getVotePointsFor(), getTotalVotePoints());
    }

    void addVotePoints(boolean votedFor, int votePoints) {
        if (votedFor) {
            setVotePointsFor(getVotePointsFor() + votePoints);
        } else {
            setVotePointsAgainst(getVotePointsAgainst() + votePoints);
        }
    }

    void removeVotePoints(boolean votedFor, int votePoints) {
        if (votedFor) {
            setVotePointsFor(Math.max(0, getVotePointsFor() - votePoints));
        } else {
            setVotePointsAgainst(Math.max(0, getVotePointsAgainst() - votePoints));
        }
    }

    @Transient
    public boolean isWasPassed() {
        return getType().wasReferendumPassed(BigDecimal.valueOf(getTotalVotePoints()), BigDecimal.valueOf(getVotePointsFor()));
    }

    @Transient
    public boolean isWasStatusAffirmed() {
        assert !isOpen() && getType().isTribunalReferendum() : "Should only be called on finished tribunal referendum";
        //mk: the logic for Tribunal Status Affirmation is reversed in case of referendum for reject approved niche (ratify), since members are actually voting for to keep the niche
        // jw: we now support ratification of multiple Channel types, so abstracting this a bit to support all.
        return getType().isRatifyStatus() == getType().wasReferendumPassed(BigDecimal.valueOf(getTotalVotePoints()), BigDecimal.valueOf(getVotePointsFor()));
    }

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Type(type = HibernatePropertiesType.TYPE)
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private transient ReferendumMetadata metadata;

    @Transient
    public <T extends ReferendumMetadata> T getMetadata() {
        // jw: only try and saturate the metadata if the type has a metadata class
        if (metadata == null && getType().getMetadataClass() != null) {
            metadata = getType().getMetadata(getProperties());
        }

        return (T) metadata;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = ReferendumVote.FIELD__REFERENDUM__NAME)
    @OrderBy(ReferendumVote.FIELD__VOTE_DATETIME__NAME)
    public List<ReferendumVote> getReferendumVotes() {
        return referendumVotes;
    }

    public void setReferendumVotes(List<ReferendumVote> referendumVoteList) {
        this.referendumVotes = referendumVoteList;
    }

    @Override
    @Transient
    public Timestamp getLiveDatetime() {
        return getStartDatetime();
    }

    @Override
    @Transient
    public boolean isAllowRepliesResolved() {
        return isOpen();
    }

    @Override
    @Transient
    public Portfolio getPortfolio() {
        return Area.getAreaRlm(Area.dao().getNarrativePlatformArea()).getDefaultPortfolio();
    }

    @Override
    public boolean hasReplyRight(AreaRole areaRole) {
        // jw: anyone who can vote, can participate in the comments!
        return isCanAreaRoleVote(areaRole);
    }

    @Override
    @Transient
    public String getTitle() {
        if (getType().isTribunalReferendum()) {
            return getType().getNameForDisplay();
        }

        assert getType().isNicheRelated() : "All publication related types should be handled by the tribunal referendum check above.";
        // jw: if this is not a tribunal issue, then link the name of the niche, since that is where the user will be taken
        return getNiche().getName();
    }

    @Transient
    public String getDetailsUrl() {
        if (getType().isTribunalReferendum()) {
            return ReactRoute.APPEAL_DETAILS.getUrl(getTribunalIssue().getOid().toString());
        }

        return ReactRoute.APPROVAL_DETAILS.getUrl(getOid().toString());
    }

    @Override
    @Transient
    public String getDisplayUrl() {
        return getDetailsUrl();
    }

    @Transient
    public String getDisplayVotesUrl() {
        return getDetailsUrl();
    }

    // jw: I am not sure where this should belong... It is always used when testing a single referendum, so that is why
    //     I am caching it here.
    private transient Integer tribunalMemberCount;

    @Transient
    public int getTribunalMemberCount() {
        assert getType().isTribunalReferendum() : "Should only ever call this method for tribunal referendums!";
        if (tribunalMemberCount == null) {
            if (isOpen()) {
                tribunalMemberCount = AreaCirclePermission.dao().getCountWithPermissionForAreaResource(getPortfolio().getArea().getAreaResource(), GlobalSecurable.PARTICIPATE_IN_TRIBUNAL_ACTIONS, ReferendumVote.dao().getVoterAreaUserOids(this));
            } else {
                // jw: once the referendum is closed, we create null "votedFor" records for all inactive tribunal members
                //     this allows us to know all Tribunal Members who could have voted at the time that the referendum ended.
                tribunalMemberCount = ReferendumVote.dao().getCount(this);
            }
        }

        return tribunalMemberCount;
    }

    public int setupTribunalMemberCountForPreviewEmail() {
        assert getType().isTribunalReferendum() : "Should only ever call this method for tribunal referendums!";
        if (tribunalMemberCount == null) {
            tribunalMemberCount = AreaCirclePermission.dao().getCountWithPermissionForAreaResource(getPortfolio().getArea().getAreaResource(), GlobalSecurable.PARTICIPATE_IN_TRIBUNAL_ACTIONS, Collections.emptyList());
        }

        return tribunalMemberCount;
    }

    @Transient
    public boolean isWasVotedForByAllTribunalMembers() {
        assert getType().isTribunalReferendum() : "This method should only ever be called for tribunal referendums!";

        if (getVotePointsAgainst() > 0) {
            return false;
        }

        return getTribunalMemberCount() == getTotalTribunalVotes();
    }

    @Transient
    public boolean isWasVotedAgainstByAllTribunalMembers() {
        assert getType().isTribunalReferendum() : "This method should only ever be called for tribunal referendums!";

        if (getVotePointsFor() > 0) {
            return false;
        }

        return getTribunalMemberCount() == getTotalTribunalVotes();
    }

    @Transient
    public boolean isWasVotedOnByAllTribunalMembers() {
        assert getType().isTribunalReferendum() : "This method should only ever be called for tribunal referendums!";

        return getTribunalMemberCount() == getTotalTribunalVotes();
    }

    @Transient
    public boolean isWasUnanimousTribunalVote() {
        return isWasVotedForByAllTribunalMembers() || isWasVotedAgainstByAllTribunalMembers();
    }

    @Override
    @Transient
    public Date getCountdownTarget() {
        return getEndDatetime();
    }

    @Override
    @Transient
    public Date getCountdownEndingSoonTarget() {
        return DatetimeCountdownProvider.getCountdownEndingSoonTarget(this);
    }

    @Override
    @Transient
    public boolean isCountdownTargetLocked() {
        // jw: until the Referendum is closed, the endDatetime could be changed at any time.
        return !isOpen();
    }

    @Override
    @Transient
    public String getCountdownRefreshUrl() {
        return null;
    }

    private transient Integer commentCount;

    @Transient
    public int getCommentCount() {
        if (commentCount == null) {
            // jw: this should only be used from a few pages, and only for a single referendum. So either the the
            //     composition partition will already be in scope, or it will be for a single partition.
            commentCount = networkContext().doCompositionTask(getCompositionPartition(), new CompositionTaskImpl<Integer>(false) {
                protected Integer doMonitoredTask() {
                    Composition composition = Composition.dao().get(getOid());

                    return composition.getCompositionStats().getReplyCount();
                }
            });
        }

        return commentCount;
    }

    private transient ReferendumVote currentUserVote;
    private transient boolean hasSetCurrentUserVote;

    @Transient
    public ReferendumVote getCurrentUserVote() {
        // bl: if this is being used and we didn't pre-populate, then JIT initialize it. note that we should
        // never do this to iterate over a list of Referendums. use ReferendumDAO.populateReferendumVotesByCurrentUser
        // when doing that.
        if(!hasSetCurrentUserVote) {
            ReferendumVote vote = null;
            if(areaContext().getPrimaryRole().isRegisteredUser()) {
                vote = ReferendumVote.dao().getForReferendumAndVoter(this, areaContext().getAreaUserRlm());
            }
            setCurrentUserVote(vote);
        }
        return currentUserVote;
    }

    public void setCurrentUserVote(ReferendumVote vote) {
        this.currentUserVote = exists(vote) ? vote : null;
        this.hasSetCurrentUserVote = true;
    }

    @Transient
    public String getResultWordletSuffix() {
        assert !isOpen() : "This function should only ever be called when the referendum is closed!";

        // jw: let's start with the referendum type
        StringBuilder suffix = new StringBuilder(".");
        suffix.append(getType());

        // jw: finally, let's add an additional suffix based on the status.
        boolean isTribunalReferendum = getType().isTribunalReferendum();
        if (isWasPassed()) {
            suffix.append(".passed");
        } else {
            suffix.append(".notPassed");
        }
        if (isTribunalReferendum && isWasUnanimousTribunalVote()) {
            suffix.append(".unanimous");
        }

        return suffix.toString();
    }

    @Transient
    public List<ReferendumVote> getRecentVotesFor() {
        return ReferendumVote.dao().getRecentVotes(this, true, null, null, ReferendumMapper.VOTES_PER_PAGE);
    }

    private transient List<ReferendumVote> recentVotesAgainst;

    @Transient
    public List<ReferendumVote> getRecentVotesAgainst() {
        if (recentVotesAgainst == null) {
            recentVotesAgainst = ReferendumVote.dao().getRecentVotes(this, false, null, null, ReferendumMapper.VOTES_PER_PAGE);

            setupReferendumVoteComments(recentVotesAgainst);
        }
        return recentVotesAgainst;
    }

    public void setupReferendumVoteComments(Collection<ReferendumVote> referendumVotes) {
        // jw: where this is used, we want to output any comments that are associated with these negative votes, so
        //     let's gather those from this Referendums composition partition.
        Map<OID, ReferendumVote> commentReplyOidToVote = new HashMap<>();
        for (ReferendumVote vote : referendumVotes) {
            if (vote.getCommentReplyOid() != null) {
                commentReplyOidToVote.put(vote.getCommentReplyOid(), vote);
            }
        }

        if (!isEmptyOrNull(commentReplyOidToVote)) {
            networkContext().doCompositionTask(getCompositionPartition(), new CompositionTaskImpl<Object>(false) {
                @Override
                protected Object doMonitoredTask() {
                    for (Reply reply : Reply.dao().getObjectsFromIDsWithCache(commentReplyOidToVote.keySet())) {
                        ReferendumVote vote = commentReplyOidToVote.get(reply.getOid());

                        if(exists(reply)) {
                            vote.setupCommentReply(reply);
                        }
                    }

                    return null;
                }
            });
        }
    }

    /**
     * This is a wrapper to allow for easy Mapstruct mapping and being nullable when not a tribunal referendum
     * */
    @Transient
    public List<ReferendumVote> getTribunalMembersYetToVote() {
        if (getType().isTribunalReferendum()) {
            return getTribunalMembersWhoHaventVoted();
        }

        return null;
    }

    @Transient
    public List<ReferendumVote> getTribunalMembersWhoHaventVoted() {
        assert getType().isTribunalReferendum() : "This method should only ever be called for tribunal referendums!";

        // jw: if the referendum is still open, we need to juse use the derived list of tribunal members who have not participated
        //     in this referendum.
        if (isOpen()) {
            return createVotesForInactiveTribunalMembers();
        }

        // jw: otherwise, let's get the list from the database
        return ReferendumVote.dao().getRecentVotes(this, null, null, null, Integer.MAX_VALUE);
    }

    public List<ReferendumVote> createVotesForInactiveTribunalMembers() {
        // jw: in order to create ReferendumVote objects for Tribunal Members who have not voted on this referendum, we
        //     need to get a list of all AreaUsers who have tribunal rights, but have not voted.
        List<OID> areaUserOids = AreaCirclePermission.dao().getAreaUserOidsWithPermissionForAreaResource(getPortfolio().getArea().getAreaResource(), GlobalSecurable.PARTICIPATE_IN_TRIBUNAL_ACTIONS, ReferendumVote.dao().getVoterAreaUserOids(this));

        List<AreaUserRlm> areaUserRlms = AreaUserRlm.dao().getObjectsFromIDsWithCache(areaUserOids);
        areaUserRlms.sort(AreaUserRlm.DISPLAYNAME_COMPARATOR);

        List<ReferendumVote> votes = new ArrayList<>(areaUserRlms.size());
        for (AreaUserRlm areaUserRlm : areaUserRlms) {
            votes.add(new ReferendumVote(this, areaUserRlm, null));
        }

        return votes;
    }

    @Transient
    public ChannelConsumer getChannelConsumer() {
        // jw: using not null check instead of exists because this is used for email previews.
        if (getNiche()!=null) {
            return getNiche();
        }

        assert getType().isTribunalRatifyPublication() : "The only time we should not have a niche is for ratification of a publication! not/"+getType();
        return getPublication();
    }

    @Transient
    public Channel getChannel() {
        ChannelConsumer consumer = getChannelConsumer();
        // jw: There is a chance that the channel consumer could have been deleted from the system!
        if (exists(consumer)) {
            return consumer.getChannel();
        }

        return null;
    }

    @Transient
    @Override
    public AgeRating getAgeRating() {
        // jw: let's treat all Referendums and their comments as rated G, for everyone.
        return AgeRating.GENERAL;
    }

    private static final int TOTAL_VOTE_POINTS_FOR_PREVIEW = 66 * UserReputation.MAX_POINTS_PER_VOTE;
    private static final int LEADING_VOTE_POINTS_FOR_PREVIEW = 44 * UserReputation.MAX_POINTS_PER_VOTE;
    private static final int TRAILING_VOTE_POINTS_FOR_PREVIEW = TOTAL_VOTE_POINTS_FOR_PREVIEW - LEADING_VOTE_POINTS_FOR_PREVIEW;

    public static Referendum getInstanceForPreviewEmail(ReferendumType type, Boolean passed, Boolean unanimous) {
        Niche niche = Niche.getNicheForPreviewEmail(type.isTribunalApproveRejectedNiche() ? NicheStatus.REJECTED : NicheStatus.ACTIVE);

        TribunalIssue issue = null;

        // jw: first, let's setup the tribunal referendum
        if (type.isTribunalReferendum()) {
            TribunalIssueType issueType = TribunalIssueType.BY_TRIBUNAL_REFERENDUM_TYPE.get(type);

            issue = TribunalIssue.getIssueForPreviewEmail(niche, issueType);
            Referendum tribunalReferendum = issue.getReferendum();

            if (type.isTribunalApproveNicheDetails()) {
                NicheDetailChangeReferendumMetadata metadata = tribunalReferendum.getMetadata();

                metadata.setup(niche, "Edited Niche Name", "This is the edited description for preview!");

                // jw: let's place the edited values onto the niche if the referendum is flagged as passed.
                if (passed != null && passed) {
                    niche.setName(metadata.getNewName());
                    niche.setDescription(metadata.getNewDescription());
                }
            }

            // jw: this referendum is over if we are processing for a community referendum that spawned from this, or
            //     the parameter for resolution state is not null.
            if (passed != null) {
                tribunalReferendum.setOpen(false);
                issue.setStatus(null);

                // jw: it is only unanimous if this is for the tribunal referendum type, and
                if (unanimous != null && unanimous) {
                    int totalTribunalMembers = tribunalReferendum.setupTribunalMemberCountForPreviewEmail();
                    tribunalReferendum.setTotalVotes(totalTribunalMembers);

                    if (passed) {
                        tribunalReferendum.setVotePointsFor(totalTribunalMembers * UserReputation.MAX_POINTS_PER_VOTE);

                        niche.setStatus(NicheStatus.FOR_SALE);
                        NicheAuction auction = new NicheAuction(niche);
                        niche.setActiveAuction(auction);

                    } else {
                        tribunalReferendum.setVotePointsAgainst(totalTribunalMembers * UserReputation.MAX_POINTS_PER_VOTE);
                        niche.setStatus(NicheStatus.REJECTED);
                    }

                } else {
                    setupReferendumVoteCounts(tribunalReferendum, passed);
                }
            }

            return tribunalReferendum;
        }

        // jw: Now that all of the TribunalIssue Referendum craziness is handled above, let's setup the community referendum.
        return createCommunityReferendum(niche, type, passed);
    }

    private static Referendum createCommunityReferendum(Niche niche, ReferendumType type, Boolean passed) {
        assert !type.isTribunalReferendum() : "Should only ever call this with non tribunal referendum types. not/" + type;

        Referendum referendum = new Referendum(niche, type, PartitionType.COMPOSITION.getBalancedPartition());

        // jw: only apply votes to the community referendum if it was ended by the community, otherwise the referendum
        //     was just opened and it should not have any votes yet.
        if (passed != null) {
            referendum.setOpen(false);
            setupReferendumVoteCounts(referendum, passed);
        }

        return referendum;
    }

    private static void setupReferendumVoteCounts(Referendum referendum, boolean wasPassed) {
        if (wasPassed) {
            referendum.setVotePointsFor(LEADING_VOTE_POINTS_FOR_PREVIEW);
            referendum.setVotePointsAgainst(TRAILING_VOTE_POINTS_FOR_PREVIEW);

        } else {
            referendum.setVotePointsFor(TRAILING_VOTE_POINTS_FOR_PREVIEW);
            referendum.setVotePointsAgainst(LEADING_VOTE_POINTS_FOR_PREVIEW);
        }
    }

    public static ReferendumDAO dao() {
        return DAOImpl.getDAO(Referendum.class);
    }
}
